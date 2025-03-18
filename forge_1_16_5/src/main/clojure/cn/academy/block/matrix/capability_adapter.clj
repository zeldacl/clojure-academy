(ns cn.academy.block.matrix.capability-adapter
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.forge-1-16.energy-bridge :as energy-bridge]
            [cn.academy.forge-1-16.inventory-bridge :as inv-bridge])
  (:import [net.minecraftforge.common.capabilities ICapabilityProvider Capability]
           [net.minecraftforge.items CapabilityItemHandler]
           [net.minecraftforge.energy CapabilityEnergy]
           [net.minecraft.util Direction LazyOptional]))

(defrecord MatrixCapabilityAdapter [matrix energy-bridge inv-bridge capabilities-atom]
  ICapabilityProvider
  (getCapability [_ cap side]
    (let [caps @capabilities-atom]
      (cond
        ; Energy capability
        (= cap (CapabilityEnergy/ENERGY))
        (LazyOptional/of (fn []
                          (energy-bridge/to-forge-energy 
                            energy-bridge 
                            (matrix/get-energy-storage matrix))))
        
        ; Item handler capability
        (= cap (CapabilityItemHandler/ITEM_HANDLER_CAPABILITY))
        (LazyOptional/of (fn []
                          (inv-bridge/to-forge-item-handler 
                            inv-bridge
                            (matrix/get-inventory matrix))))
        
        :else LazyOptional/EMPTY))))

(defn create-adapter [matrix]
  (->MatrixCapabilityAdapter
    matrix
    (energy-bridge/create-energy-bridge)
    (inv-bridge/create-inventory-bridge)
    (atom {})))

(defn invalidate-capabilities! [adapter]
  (let [caps @(:capabilities-atom adapter)]
    (doseq [cap (vals caps)]
      (.invalidate cap))
    (reset! (:capabilities-atom adapter) {})))