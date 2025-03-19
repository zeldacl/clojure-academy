(ns cn.academy.examples.energy-relay-te
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [cn.academy.api.public :as api]
            [mcmod.logging :as log])
  (:import [net.minecraft.nbt CompoundNBT]))

(defrecord EnergyRelayTE []
  ITileEntity
  (tick [this]
    (let [energy-storage (:energy-storage this)
          world (:world this)
          pos (:pos this)]
      (when (and (not (.isClientSide world))
                 (> (cap/get-energy-stored energy-storage) 0))
        ;; Use API to find and transfer energy to nearby blocks
        (doseq [target-te (api/find-energy-receivers world pos 3)]
          (when-let [transferred (api/transfer-energy this target-te 500)]
            (log/debug "Relay transferred %d energy to target at [%d,%d,%d]"
                      transferred
                      (-> target-te :pos :x)
                      (-> target-te :pos :y)
                      (-> target-te :pos :z)))))))
  
  (save [this]
    (let [tag (CompoundNBT.)]
      (when-let [energy-storage (:energy-storage this)]
        (.putInt tag "energy" (cap/get-energy-stored energy-storage)))
      tag))
  
  (load [this data]
    (let [{:keys [nbt]} data
          energy-storage (api/create-energy-storage
                          :capacity 50000
                          :max-receive 500
                          :max-extract 500)]
      (when (.contains nbt "energy")
        (cap/receive-energy energy-storage 
                           (.getInt nbt "energy")
                           false))
      (assoc this :energy-storage energy-storage)))
  
  cap/ICapabilityProvider
  (has-capability? [this capability-type side]
    (= (.getName capability-type) "forge:energy"))
  
  (get-capability [this capability-type side]
    (when (has-capability? this capability-type side)
      (:energy-storage this)))
  
  (invalidate-capabilities [this]
    nil))