(ns cn.academy.commands.stats-command
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.stats :as stats]
            [mcmod.text :as text]
            [mcmod.world :as world]
            [mcmod.logging :as log]))

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
    (let [source (:source context)]
      (try
        (doseq [[name stat] stats/stats-registry]
          (world/send-success source 
                      (text/create-text 
                        (format-stat name (stats/get-value stat)))
                      false))
        1
        (catch Exception e
          (log/error "Error displaying stats: %s" (.getMessage e))
          (world/send-failure source "Error displaying statistics")
          0)))))