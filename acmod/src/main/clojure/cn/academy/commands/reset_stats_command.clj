(ns cn.academy.commands.reset-stats-command
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.stats :as stats]
            [mcmod.text :as text]
            [mcmod.world :as world]
            [mcmod.logging :as log]))

(defrecord ResetStatsCommand []
  ICommand
  (get-name [this] "reset-stats")
  
  (get-permission-level [this] 2)
  
  (execute [this context args]
    (let [source (:source context)]
      (try
        (doseq [[_ stat] stats/stats-registry]
          (stats/reset-value stat))
        (world/send-success source 
                     (text/create-text "All statistics have been reset")
                     true)
        (log/info "Statistics reset by %s" (world/get-display-name source))
        1
        (catch Exception e
          (log/error "Error resetting stats: %s" (.getMessage e))
          (world/send-failure source "Error resetting statistics")
          0)))))