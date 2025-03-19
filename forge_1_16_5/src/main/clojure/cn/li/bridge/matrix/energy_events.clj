(ns cn.li.bridge.matrix.energy-events
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.energy.api :as energy]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.eventbus.api Event]))

(defrecord MatrixEnergyReceiveEvent [matrix amount simulate]
  Event
  (isCancelable [_] true))

(defrecord MatrixEnergyExtractEvent [matrix amount simulate]
  Event
  (isCancelable [_] true))

(defrecord MatrixEnergyUpdateEvent [matrix]
  Event
  (isCancelable [_] false))

(defprotocol IMatrixEnergyHandler
  "Handler for matrix energy events"
  (handle-receive [this event] "Handle receiving energy")
  (handle-extract [this event] "Handle extracting energy")
  (handle-update [this event] "Handle energy updates"))

(defrecord MatrixEnergyEventHandler [matrix]
  IMatrixEnergyHandler
  (handle-receive [_ event]
    (let [{:keys [amount simulate]} event]
      (when (matrix/can-receive? matrix)
        (let [received (if simulate
                        (matrix/simulate-receive matrix amount)
                        (matrix/receive-energy matrix amount false))]
          (when (pos? received)
            (log/debug "Matrix" matrix "received" received "energy")
            received)))))
  
  (handle-extract [_ event]
    (let [{:keys [amount simulate]} event]
      (when (matrix/can-extract? matrix)
        (let [extracted (if simulate
                         (matrix/simulate-extract matrix amount)
                         (matrix/extract-energy matrix amount false))]
          (when (pos? extracted)
            (log/debug "Matrix" matrix "extracted" extracted "energy")
            extracted)))))
  
  (handle-update [_ event]
    (let [stored (matrix/get-energy-stored matrix)
          capacity (matrix/get-energy-capacity matrix)]
      (log/debug "Matrix" matrix "energy update:" stored "/" capacity)
      (matrix/update-energy matrix))))

(defn create-energy-handler [matrix]
  (->MatrixEnergyEventHandler matrix))

(defn register-energy-events! [handler event-bus]
  (.register event-bus handler))