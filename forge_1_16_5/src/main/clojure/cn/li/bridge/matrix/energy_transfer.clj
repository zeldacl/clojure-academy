(ns cn.li.bridge.matrix.energy-transfer
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.events :refer [->MatrixEnergyTransferEvent]]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]))

(def ^:private TRANSFER_EFFICIENCY 0.95) ; 5% energy loss in transfer

(defn can-transfer? [source target amount]
  (and (matrix/can-extract? source amount)
       (matrix/can-receive? target (* amount TRANSFER_EFFICIENCY))))

(defn calculate-transfer-amount [source target requested]
  (let [available (matrix/get-energy-stored source)
        capacity (matrix/get-energy-capacity target)
        stored (matrix/get-energy-stored target)
        space (- capacity stored)
        max-receive (/ space TRANSFER_EFFICIENCY)]
    (min requested available max-receive)))

(defn transfer-energy! [source target amount]
  (let [transfer-amount (calculate-transfer-amount source target amount)
        event (->MatrixEnergyTransferEvent source target transfer-amount)]
    (when (not (.isCanceled (.post MinecraftForge/EVENT_BUS event)))
      (let [extracted (matrix/extract-energy source transfer-amount false)
            received (matrix/receive-energy target 
                                          (* extracted TRANSFER_EFFICIENCY) 
                                          false)]
        (when (pos? received)
          (log/debug "Transferred" received "energy from" source "to" target)
          received)))))

(defn distribute-energy! [source targets amount]
  (let [per-target (/ amount (count targets))]
    (doseq [target targets]
      (when (can-transfer? source target per-target)
        (transfer-energy! source target per-target)))))