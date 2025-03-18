(ns cn.academy.forge-1-16.capability-adapter
  (:require [cn.academy.capability.core :as cap]
            [cn.academy.forge-1-16.energy-bridge :as energy-bridge]
            [cn.academy.forge-1-16.inventory-bridge :as inv-bridge])
  (:import [net.minecraftforge.common.capabilities ICapabilityProvider Capability]
           [net.minecraftforge.items CapabilityItemHandler]
           [net.minecraftforge.energy CapabilityEnergy]
           [net.minecraft.util Direction LazyOptional]))

(defn- create-handler [capability provider bridge]
  (LazyOptional/of 
    (fn []
      (case (cap/get-type-id capability)
        :energy (energy-bridge/to-forge-energy 
                  bridge 
                  (cap/get-capability provider :energy nil))
        :inventory (inv-bridge/to-forge-item-handler 
                    bridge
                    (cap/get-capability provider :inventory nil))
        nil))))

(defrecord ForgeCapabilityAdapter [provider energy-bridge inv-bridge capabilities-atom]
  ICapabilityProvider
  (getCapability [_ cap side]
    (let [caps @capabilities-atom]
      (cond
        ; Energy capability
        (= cap (CapabilityEnergy/ENERGY))
        (create-handler cap provider energy-bridge)
        
        ; Item handler capability  
        (= cap (CapabilityItemHandler/ITEM_HANDLER_CAPABILITY))
        (create-handler cap provider inv-bridge)
        
        :else LazyOptional/EMPTY)))

  cap/ICapabilityProvider
  (invalidate-capabilities! [_]
    (let [caps @capabilities-atom]
      (doseq [cap (vals caps)]
        (.invalidate cap))
      (reset! capabilities-atom {}))))

(defn create-adapter [provider]
  (->ForgeCapabilityAdapter
    provider
    (energy-bridge/create-energy-bridge)
    (inv-bridge/create-inventory-bridge)
    (atom {})))