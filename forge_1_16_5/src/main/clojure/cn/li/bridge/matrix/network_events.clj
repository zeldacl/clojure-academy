(ns cn.li.bridge.matrix.network-events
  (:require [cn.li.bridge.matrix.api :as matrix]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.eventbus.api Event]))

(defrecord MatrixNetworkSyncEvent [matrix state]
  Event
  (isCancelable [_] false))

(defrecord MatrixStructureEvent [matrix pos]
  Event
  (isCancelable [_] true))

(defrecord MatrixFormationEvent [matrix]
  Event
  (isCancelable [_] true))

(defprotocol IMatrixNetworkHandler
  "Handler for matrix network events"
  (handle-sync [this event] "Handle sync events")
  (handle-structure-change [this event] "Handle structure changes")
  (handle-formation [this event] "Handle matrix formation"))

(defrecord MatrixNetworkEventHandler [matrix]
  IMatrixNetworkHandler
  (handle-sync [_ event]
    (let [{:keys [state]} event]
      (matrix/handle-sync matrix state)
      (log/debug "Matrix" matrix "synced state:" state)))
  
  (handle-structure-change [_ event]
    (let [{:keys [pos]} event]
      (when (matrix/validate-structure matrix pos)
        (matrix/update-structure matrix)
        (log/debug "Matrix" matrix "structure updated at" pos))))
  
  (handle-formation [_ event]
    (when (matrix/can-form? matrix)
      (matrix/form matrix)
      (log/debug "Matrix" matrix "formed successfully"))))

(defn create-network-handler [matrix]
  (->MatrixNetworkEventHandler matrix))

(defn register-network-events! [handler event-bus]
  (.register event-bus handler))