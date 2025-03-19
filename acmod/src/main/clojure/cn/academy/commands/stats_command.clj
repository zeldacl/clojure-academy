(ns cn.academy.commands.stats-command
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.stats :as stats]
            [mcmod.logging :as log])
  (:import [net.minecraft.command CommandSource]
           [net.minecraft.util.text StringTextComponent]))

(defn format-stat [name value]
  (str 
    (case name
      "wireless_matrix.energy_level" "Energy Level: "
      "wireless_matrix.energy_transferred" "Total Energy Transferred: "
      "wireless_matrix.transfer_count" "Total Transfers: "
      "wireless_matrix.transfer_targets" "Average Transfer Targets: "
      (str name ": "))
    value))

(defrecord ViewStatsCommand []
  ICommand
  (get-name [this] "view-stats")
  
  (get-permission-level [this] 0)
  
  (execute [this context args]
    (let [source (.getSource context)]
      (try
        (doseq [[name stat] stats/stats-registry]
          (.sendSuccess source 
                       (StringTextComponent. 
                         (format-stat name (stats/get-value stat)))
                       false))
        1
        (catch Exception e
          (log/error "Error displaying stats: %s" (.getMessage e))
          (.sendFailure source "Error displaying statistics")
          0)))))