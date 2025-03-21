(ns cn.academy.tech-system.energy-system.admin
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.config :as config]
            [cn.academy.tech-system.energy-system.debug :as debug]
            [cn.academy.tech-system.energy-system.error-handling :as error]
            [cn.academy.tech-system.energy-system.security :as security]
            [cn.academy.tech-system.energy-system.analytics :as analytics]
            [cn.academy.tech-system.energy-system.monitoring :as monitoring]
            [cn.academy.tech-system.energy-system.visualization :as viz]
            [mcmod.commands :as cmd]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;; Admin command handlers
(defn- handle-debug [player args]
  (case (first args)
    "enable" (do (debug/enable-debug!)
                 (cmd/feedback player "Debug logging enabled"))
    "disable" (do (debug/disable-debug!)
                  (cmd/feedback player "Debug logging disabled"))
    "dump" (if-let [network-id (second args)]
             (if-let [network (network/get-network network-id)]
               (do (debug/dump-network-state! network)
                   (cmd/feedback player "Network state dumped to log"))
               (cmd/error player "Network not found"))
             (cmd/error player "Network ID required"))
    "history" (let [entries (debug/get-debug-history)]
                (cmd/feedback player 
                            (str/join "\n" 
                                     (map #(str (:type %) ": " (pr-str (dissoc % :type :timestamp)))
                                          (take-last 5 entries)))))
    (cmd/error player "Unknown debug command")))

(defn- handle-network [player args]
  (case (first args)
    "list" (let [networks (network/get-all-networks)]
             (cmd/feedback player 
                          (str/join "\n"
                                   (for [[id net] networks]
                                     (str id ": " (count (network/get-nodes net)) " nodes")))))
    "info" (if-let [network-id (second args)]
             (if-let [network (network/get-network network-id)]
               (let [nodes (network/get-nodes network)
                     total-energy (reduce + (map network/get-energy nodes))]
                 (cmd/feedback player
                              (str/join "\n"
                                       [(str "Network: " network-id)
                                        (str "Nodes: " (count nodes))
                                        (str "Total Energy: " total-energy)])))
               (cmd/error player "Network not found"))
             (cmd/error player "Network ID required"))
    "validate" (do (doseq [[id net] (network/get-all-networks)]
                    (network/validate-network! net))
                  (cmd/feedback player "All networks validated"))
    (cmd/error player "Unknown network command")))

(defn- handle-config [player args]
  (case (first args)
    "reload" (do (config/load-config!)
                 (cmd/feedback player "Configuration reloaded"))
    "save" (do (config/save-config!)
               (cmd/feedback player "Configuration saved"))
    "get" (if-let [path (rest args)]
            (if-let [value (apply config/get-config (map keyword path))]
              (cmd/feedback player (str (str/join "." path) " = " (pr-str value)))
              (cmd/error player "Configuration path not found"))
            (cmd/error player "Configuration path required"))
    (cmd/error player "Unknown config command")))

(defn- handle-security [player args]
  (case (first args)
    "blacklist" (case (second args)
                 "add" (if-let [target (cmd/get-player (nth args 2))]
                        (do (security/add-to-blacklist security/manager target)
                            (cmd/feedback player "Player added to blacklist"))
                        (cmd/error player "Player not found"))
                 "remove" (if-let [target (cmd/get-player (nth args 2))]
                          (do (security/remove-from-blacklist security/manager target)
                              (cmd/feedback player "Player removed from blacklist"))
                          (cmd/error player "Player not found"))
                 (cmd/error player "Unknown blacklist command"))
    "logs" (let [logs (take-last 5 (get-in @(:state-atom security/manager) [:access-logs]))]
             (cmd/feedback player
                          (str/join "\n"
                                   (map #(str (:timestamp %) ": " (:player %) 
                                            (if (:success %) " succeeded" " failed"))
                                        logs))))
    (cmd/error player "Unknown security command")))

(defn- handle-analytics [player args]
  (case (first args)
    "report" (if-let [network-id (second args)]
               (if-let [report (analytics/generate-report analytics/analytics network-id)]
                 (cmd/feedback player 
                             (str/join "\n"
                                      [(str "Network Report for " network-id)
                                       (str "Nodes: " (get-in report [:current-state :node-count]))
                                       (str "Total Energy: " (get-in report [:current-state :total-energy]))
                                       (str "Connections: " (get-in report [:current-state :connections]))
                                       ""
                                       (viz/create-bar-chart "Daily Energy Statistics"
                                                           ["Average" "Peak" "Min"]
                                                           [(get-in report [:daily-statistics :average-energy])
                                                            (get-in report [:daily-statistics :peak-energy])
                                                            (get-in report [:daily-statistics :min-energy])]
                                                           50 8)
                                       ""
                                       "Recommendations:"
                                       (str/join "\n" (map #(str "- " (:reason %))
                                                         (:recommendations report)))]))
                 (cmd/error player "Failed to generate report"))
               (cmd/error player "Network ID required"))
    
    "trends" (if-let [network-id (second args)]
               (if-let [trends (analytics/analyze-trends analytics/analytics network-id 24)]
                 (let [efficiency (:efficiency trends)
                       transfer-rates (take-last 24 (get-in @(:state-atom analytics/analytics) 
                                                          [:stats network-id]))
                       rate-values (map :transfer-rate transfer-rates)]
                   (cmd/feedback player
                                (str/join "\n"
                                         [(str "Network Trends for " network-id)
                                          ""
                                          (viz/create-line-chart "Energy Transfer Rate (24h)"
                                                               rate-values
                                                               50 6
                                                               :labels ["0h" "6h" "12h" "18h" "24h"])
                                          ""
                                          (str "Energy Trend: " (name (get-in trends [:energy-trend :direction]))
                                               " (Rate: " (get-in trends [:energy-trend :rate]) ")")
                                          (str "Stability Score: " (get-in trends [:stability :score]))
                                          (str "Efficiency: Average " (:average efficiency)
                                               ", Peak " (:peak efficiency))])))
                 (cmd/error player "Failed to analyze trends"))
               (cmd/error player "Network ID required"))
    
    "predict" (if-let [network-id (second args)]
               (let [hours (or (some-> (nth args 2) Integer/parseInt) 24)
                     prediction (analytics/predict-usage analytics/analytics network-id hours)]
                 (if prediction
                   (let [pred-values (map :predicted-energy (:predictions prediction))
                         conf-values (map :confidence (:predictions prediction))]
                     (cmd/feedback player
                                 (str/join "\n"
                                          [(str "Energy Predictions for " network-id)
                                           (str "Reliability: " (* 100 (:reliability prediction)) "%")
                                           ""
                                           (viz/create-line-chart "Predicted Energy Usage"
                                                                pred-values
                                                                50 6
                                                                :labels ["0h" (str (quot hours 2) "h") (str hours "h")])
                                           ""
                                           "Confidence Levels:"
                                           (viz/create-bar-chart "Prediction Confidence"
                                                               (map #(str "H" %) (range 0 (min 6 hours)))
                                                               (take 6 conf-values)
                                                               40 4)])))
                   (cmd/error player "Failed to generate predictions")))
               (cmd/error player "Network ID required"))
    
    "chart" (if-let [network-id (second args)]
             (let [metric (or (nth args 2) "energy")
                   timespan (or (some-> (nth args 3) Integer/parseInt) 24)
                   stats (take-last timespan 
                                  (get-in @(:state-atom analytics/analytics)
                                         [:stats network-id]))]
               (if (seq stats)
                 (let [values (case metric
                              "energy" (map :total-energy stats)
                              "nodes" (map :node-count stats)
                              "connections" (map :connections stats)
                              "transfer" (map :transfer-rate stats)
                              (map :total-energy stats))
                       chart (viz/create-line-chart (str (str/capitalize metric) " over time")
                                                  values
                                                  50 8
                                                  :labels [(str "-" timespan "h") 
                                                         (str "-" (quot timespan 2) "h")
                                                         "now"])]
                   (cmd/feedback player chart))
                 (cmd/error player "No data available for chart")))
             (cmd/error player "Network ID required"))
    
    (cmd/error player "Unknown analytics command")))

;; Command registration
(defn register-commands! []
  (cmd/register-command! "energyadmin"
    (fn [player args]
      (case (first args)
        "debug" (handle-debug player (rest args))
        "network" (handle-network player (rest args))
        "config" (handle-config player (rest args))
        "security" (handle-security player (rest args))
        "analytics" (handle-analytics player (rest args))
        (cmd/error player "Unknown admin command")))
    {:permission-level 4 ; Op level required
     :description "Energy system administration commands"}))