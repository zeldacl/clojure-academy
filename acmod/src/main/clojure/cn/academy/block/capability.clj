(ns cn.academy.block.capability
  (:require [cn.academy.block.energy :as energy]
            [clojure.tools.logging :as log]))

;; Capability providers
(defn create-energy-capability [block]
  (let [{:keys [state config]} block]
    {:energy-storage (energy/create-energy-storage state config)
     :energy-producer (when (:max-output config)
                       (energy/create-energy-producer config))
     :energy-consumer (when (:energy-per-tick config)
                       (energy/create-energy-consumer config))}))

(defn create-inventory-capability [block]
  (when-let [inv-size (get-in block [:config :inventory-size])]
    {:inventory (mcmod.inventory/create-inventory inv-size)}))

(defn create-multiblock-capability [block]
  (when-let [member (:member block)]
    {:multiblock member}))

;; Capability manager
(defn create-capability-provider [block]
  (let [capabilities (merge
                      (create-energy-capability block)
                      (create-inventory-capability block)
                      (create-multiblock-capability block))]
    (reify mcmod.capability/ICapabilityProvider
      (get-capability [_ cap-type side]
        (case cap-type
          :energy (or (:energy-storage capabilities)
                     (:energy-producer capabilities)
                     (:energy-consumer capabilities))
          :inventory (:inventory capabilities)
          :multiblock (:multiblock capabilities)
          nil))
      
      (has-capability? [_ cap-type side]
        (case cap-type
          :energy (boolean (or (:energy-storage capabilities)
                             (:energy-producer capabilities)
                             (:energy-consumer capabilities)))
          :inventory (boolean (:inventory capabilities))
          :multiblock (boolean (:multiblock capabilities))
          false)))))