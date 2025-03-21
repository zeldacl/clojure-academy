(ns cn.academy.block.debug
  (:require [mcmod.protocols :refer [IDebugger IDebugTarget]]
            [cn.academy.block.error :as error]
            [cn.academy.block.monitor :as monitor]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.config :as config]
            [clojure.tools.logging :as log]))

;; Debug state tracking
(def debug-state
  (atom {:breakpoints {}  
         :watches {}
         :enabled-targets #{}
         :traces []}))

;; Debug target implementation
(defrecord DebugTarget [id category target-fn state-atom]
  IDebugTarget
  (enable! [_]
    (swap! state-atom update :enabled-targets conj id))
  
  (disable! [_]
    (swap! state-atom update :enabled-targets disj id))
  
  (is-enabled? [_]
    (contains? (:enabled-targets @state-atom) id))
  
  (collect-data [this]
    (when (.is-enabled? this)
      (error/with-error-handling :debug
        (target-fn)))))

;; Debugger implementation
(defrecord BlockDebugger [state-atom]
  IDebugger
  (add-breakpoint! [_ id pred]
    (swap! state-atom assoc-in [:breakpoints id] pred))
  
  (remove-breakpoint! [_ id]
    (swap! state-atom update :breakpoints dissoc id))
  
  (check-breakpoint [_ id state]
    (when-let [pred (get-in @state-atom [:breakpoints id])]
      (pred state)))
  
  (add-watch! [_ id path handler]
    (swap! state-atom assoc-in [:watches id] 
           {:path path :handler handler}))
  
  (remove-watch! [_ id]
    (swap! state-atom update :watches dissoc id))
  
  (notify-watch [this id old-state new-state]
    (when-let [{:keys [path handler]} (get-in @state-atom [:watches id])]
      (let [old-value (get-in old-state path)
            new-value (get-in new-state path)]
        (when (not= old-value new-value)
          (handler old-value new-value)))))
  
  (add-trace! [_ event]
    (swap! state-atom update :traces conj 
           (assoc event :timestamp (System/currentTimeMillis))))
  
  (get-traces [_]
    (:traces @state-atom))
  
  (clear-traces! [_]
    (swap! state-atom assoc :traces [])))

;; Factory functions
(defn create-debugger []
  (->BlockDebugger debug-state))

(defn create-target [id category target-fn]
  (->DebugTarget id category target-fn debug-state))

;; Debug data collectors
(def debug-targets
  [{:id :machine-states
    :category :machine
    :fn #(into {} (map (fn [m] 
                        [(:id m) (select-keys @(:state m) 
                                            [:active :progress :error])])
                      (mcmod.machine/get-active-machines)))}
   
   {:id :network-stats
    :category :network
    :fn #(let [stats-collector (stats/create-collector :network)]
           {:active-networks (count (mcmod.network/get-active-networks))
            :packet-stats (stats/generate-stats-report stats-collector)})}
   
   {:id :resource-usage
    :category :resource
    :fn #(let [stats-collector (stats/create-collector :resource)]
           (stats/generate-stats-report stats-collector))}
   
   {:id :error-state
    :category :error
    :fn #(into {} (map (fn [[id errors]]
                        [id (mapv :message errors)])
                      (error/get-active-errors)))}])

;; Debug reporting
(defn generate-debug-report []
  (let [debugger (create-debugger)]
    {:timestamp (System/currentTimeMillis)
     :version (mcmod.version/get-current-version)
     :config (config/get-active-config)
     :traces (.get-traces debugger)
     :metrics (monitor/get-all-metrics)
     :targets (->> debug-targets
                  (map (fn [{:keys [id category fn]}]
                        (let [target (create-target id category fn)]
                          [id (.collect-data target)])))
                  (into {}))}))

;; Debug command handlers
(defn handle-debug-command [command & args]
  (let [debugger (create-debugger)]
    (case command
      :breakpoint (let [[id pred] args]
                   (.add-breakpoint! debugger id pred))
      :watch (let [[id path handler] args]
              (.add-watch! debugger id path handler))
      :enable (let [[target-id] args
                   target (some #(when (= (:id %) target-id) %) 
                               debug-targets)]
               (when target
                 (.enable! (create-target (:id target) 
                                        (:category target)
                                        (:fn target)))))
      :disable (let [[target-id] args
                    target (some #(when (= (:id %) target-id) %) 
                                debug-targets)]
                (when target
                  (.disable! (create-target (:id target)
                                          (:category target) 
                                          (:fn target)))))
      :report (generate-debug-report)
      :clear (.clear-traces! debugger)
      {:error "Unknown debug command"})))

;; Initialize debug system
(defn init-debug! []
  (reset! debug-state {:breakpoints {}
                       :watches {}
                       :enabled-targets #{}
                       :traces []})
  
  ;; Enable default debug targets
  (doseq [{:keys [id category fn]} debug-targets]
    (.enable! (create-target id category fn))))