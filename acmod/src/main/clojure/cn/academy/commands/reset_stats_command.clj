(ns cn.academy.commands.reset-stats-command
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.stats :as stats]
            [mcmod.logging :as log])
  (:import [net.minecraft.command CommandSource]
           [net.minecraft.util.text StringTextComponent]))

(defrecord ResetStatsCommand []
  ICommand
  (get-name [this] "reset-stats")
  
  (get-permission-level [this] 2)
  
  (execute [this context args]
    (let [source (.getSource context)]
      (try
        (doseq [[_ stat] stats/stats-registry]
          (stats/reset-value stat))
        (.sendSuccess source 
                     (StringTextComponent. "All statistics have been reset")
                     true)
        (log/info "Statistics reset by %s" (.getDisplayName source))
        1
        (catch Exception e
          (log/error "Error resetting stats: %s" (.getMessage e))
          (.sendFailure source "Error resetting statistics")
          0)))))