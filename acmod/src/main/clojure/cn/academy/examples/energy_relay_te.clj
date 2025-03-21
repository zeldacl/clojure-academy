(ns cn.academy.examples.energy-relay-te
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [mcmod.nbt :as nbt]
            [mcmod.world :as world]
            [cn.academy.api.public :as api]
            [mcmod.logging :as log]))

(defrecord EnergyRelayTE []
  ITileEntity
  (tick [this]
    (let [energy-storage (:energy-storage this)
          world-obj (:world this)
          pos (:pos this)]
      (when (and (not (world/is-client-side? world-obj))
                 (> (cap/get-energy-stored energy-storage) 0))
        ;; Use API to find and transfer energy to nearby blocks
        (doseq [target-te (api/find-energy-receivers world-obj pos 3)]
          (when-let [transferred (api/transfer-energy this target-te 500)]
            (log/debug "Relay transferred %d energy to target at [%d,%d,%d]"
                      transferred
                      (-> target-te :pos :x)
                      (-> target-te :pos :y)
                      (-> target-te :pos :z)))))))
  
  (save [this]
    (let [tag (nbt/create-compound)]
      (when-let [energy-storage (:energy-storage this)]
        (nbt/put-int tag "energy" (cap/get-energy-stored energy-storage)))
      tag))
  
  (load [this data]
    (let [{:keys [nbt]} data
          energy-storage (api/create-energy-storage
                          :capacity 50000
                          :max-receive 500
                          :max-extract 500)]
      (when (nbt/contains? nbt "energy")
        (cap/receive-energy energy-storage 
                           (nbt/get-int nbt "energy")
                           false))
      (assoc this :energy-storage energy-storage)))
  
  cap/ICapabilityProvider
  (has-capability? [this capability-type side]
    (= (cap/get-name capability-type) "forge:energy"))
  
  (get-capability [this capability-type side]
    (when (has-capability? this capability-type side)
      (:energy-storage this)))
  
  (invalidate-capabilities [this]
    nil))