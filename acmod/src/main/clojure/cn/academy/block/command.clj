(ns cn.academy.block.command
  (:require [cn.academy.block.security :as security]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.env :as env]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Command registry
(def command-registry
  (atom {:commands {}
         :aliases {}}))

;; Command definition
(defprotocol ICommand
  (execute! [this player args])
  (can-execute? [this player])
  (get-usage [this])
  (get-description [this]))

;; Base command record
(defrecord Command [name permission execute-fn usage description]
  ICommand
  (execute! [_ player args]
    (error/with-safe-execution name :command
      (execute-fn player args)))
  
  (can-execute? [_ player]
    (or (mcmod.player/is-op? player)
        (security/has-permission? player permission)))
  
  (get-usage [_] usage)
  
  (get-description [_] description))

;; Command registration
(defn register-command! [name {:keys [permission execute usage description aliases]}]
  (let [cmd (->Command name permission execute usage description)]
    (swap! command-registry assoc-in [:commands name] cmd)
    (doseq [alias aliases]
      (swap! command-registry assoc-in [:aliases alias] name))))

;; Command execution
(defn execute-command! [player command-str]
  (let [[cmd & args] (clojure.string/split command-str #" ")
        cmd-name (or (get-in @command-registry [:aliases cmd]) cmd)]
    (if-let [command (get-in @command-registry [:commands cmd-name])]
      (if (can-execute? command player)
        (execute! command player args)
        (mcmod.player/send-message player "You don't have permission to use this command."))
      (mcmod.player/send-message player (str "Unknown command: " cmd)))))

;; Default commands
(def default-commands
  {"info" {:permission :view
           :execute (fn [player [block-id]]
                     (when-let [block (mcmod.block/get-block-by-id block-id)]
                       (let [stats (stats/generate-block-report block)]
                         (mcmod.player/send-message player 
                           (str "Block Info:\n"
                                "Type: " (:type block) "\n"
                                "Energy: " (get-in @(:state block) [:energy]) "\n"
                                "Efficiency: " (:efficiency stats))))))
           :usage "/ac info <block-id>"
           :description "Display information about a block"
           :aliases ["i" "status"]}
   
   "config" {:permission :configure
             :execute (fn [player [block-id key value]]
                       (when-let [block (mcmod.block/get-block-by-id block-id)]
                         (when (security/check-permission! player block :configure)
                           (swap! (:state block) assoc (keyword key) (read-string value))
                           (mcmod.player/send-message player "Configuration updated."))))
             :usage "/ac config <block-id> <key> <value>"
             :description "Configure block settings"
             :aliases ["cfg" "set"]}
   
   "stats" {:permission :view
            :execute (fn [player [block-id period]]
                      (when-let [block (mcmod.block/get-block-by-id block-id)]
                        (let [period-ms (* (or (parse-long period) 3600) 1000)
                              stats (stats/get-block-stats block-id :energy-consumption
                                                         :window period-ms)]
                          (mcmod.player/send-message player
                            (str "Statistics for last " period " seconds:\n"
                                 "Total Energy: " (:total stats) "\n"
                                 "Average: " (:average stats) "\n"
                                 "Peak: " (:peak stats))))))
            :usage "/ac stats <block-id> [period-seconds]"
            :description "View block statistics"
            :aliases ["st"]}})

;; Command help
(defn get-command-help []
  (->> (:commands @command-registry)
       (map (fn [[name command]]
              (str name " - " (get-description command) "\n"
                   "Usage: " (get-usage command))))
       (clojure.string/join "\n\n")))

;; Initialize command system
(defn init-commands! []
  ;; Register default commands
  (doseq [[name cmd-def] default-commands]
    (register-command! name cmd-def))
  
  ;; Register command handler
  (mcmod.events/register-handler! :player-command
    (fn [player command]
      (when (clojure.string/starts-with? command "/ac ")
        (execute-command! player (subs command 4))))))