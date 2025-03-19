(ns cn.li.bridge.matrix.event-coordinator
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.energy-events :as energy]
            [cn.li.bridge.matrix.network-events :as network]
            [cn.li.bridge.matrix.state-manager :as state]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]))

(defprotocol IEventCoordinator
  "Coordinates events between Matrix components"
  (coordinate-energy-update [this matrix] "Handle energy updates")
  (coordinate-network-sync [this matrix] "Handle network sync")
  (coordinate-state-change [this matrix] "Handle state changes")
  (coordinate-structure-update [this matrix pos] "Handle structure updates"))

(defrecord MatrixEventCoordinator [event-bus]
  IEventCoordinator
  (coordinate-energy-update [_ matrix]
    (let [event (energy/->MatrixEnergyUpdateEvent matrix)]
      (.post event-bus event)
      (when (matrix/is-formed? matrix)
        (network/handle-sync matrix))))
  
  (coordinate-network-sync [_ matrix]
    (let [state (matrix/get-sync-data matrix)
          event (network/->MatrixNetworkSyncEvent matrix state)]
      (.post event-bus event)))
  
  (coordinate-state-change [_ matrix]
    (let [event (state/->MatrixStateChangeEvent matrix)]
      (.post event-bus event)
      (coordinate-network-sync matrix)))
  
  (coordinate-structure-update [_ matrix pos]
    (let [event (network/->MatrixStructureEvent matrix pos)]
      (when-not (.isCanceled (.post event-bus event))
        (state/update-state matrix)))))

(defn create-coordinator [event-bus]
  (->MatrixEventCoordinator event-bus))

(defn register-coordinator! [coordinator]
  (.register MinecraftForge/EVENT_BUS coordinator))