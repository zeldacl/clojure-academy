(ns cn.li.bridge.matrix.events
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.event.api :as event]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.eventbus.api Event]))

(defrecord MatrixFormEvent [matrix]
  Event
  (isCancelable [_] true))

(defrecord MatrixDeformEvent [matrix]
  Event
  (isCancelable [_] true))

(defrecord MatrixEnergyTransferEvent [source target amount]
  Event
  (isCancelable [_] true))

(defprotocol IMatrixEventHandler
  "Handler for matrix-specific events"
  (handle-form [this event] "Handle matrix formation")
  (handle-deform [this event] "Handle matrix deformation")
  (handle-energy-transfer [this event] "Handle energy transfer between matrices"))

(defrecord MatrixEventDispatcher [matrix]
  IMatrixEventHandler
  (handle-form [_ event]
    (when (matrix/is-valid-structure? matrix)
      (log/debug "Matrix formed at" (matrix/get-position matrix))
      true))
  
  (handle-deform [_ event]
    (log/debug "Matrix deformed at" (matrix/get-position matrix))
    true)
  
  (handle-energy-transfer [_ event]
    (let [{:keys [source target amount]} event]
      (when (and (matrix/can-extract? source amount)
                 (matrix/can-receive? target amount))
        (let [extracted (matrix/extract-energy source amount false)
              received (matrix/receive-energy target extracted false)]
          (log/debug "Transferred" received "energy from" source "to" target)
          true)))))

(defn create-event-dispatcher [matrix]
  (->MatrixEventDispatcher matrix))

(defn register-events! [dispatcher event-bus]
  (.register event-bus dispatcher))