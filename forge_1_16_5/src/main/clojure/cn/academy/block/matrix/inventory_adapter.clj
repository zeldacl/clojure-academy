(ns cn.academy.block.matrix.inventory-adapter
  (:require [cn.academy.block.matrix-inventory :as matrix-inv]
            [cn.academy.forge-1-16.inventory-bridge :as inv-bridge])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.items CapabilityItemHandler]
           [net.minecraft.util Direction]))

(defrecord MatrixInventoryAdapter [matrix-inventory bridge]
  ICapabilityProvider
  (getCapability [_ cap side]
    (when (= cap (CapabilityItemHandler/ITEM_HANDLER_CAPABILITY))
      (inv-bridge/to-forge-item-handler bridge
        (reify IItemHandler
          (getSlots [_]
            (matrix-inv/get-size matrix-inventory))
          
          (getStackInSlot [_ slot]
            (if (= slot 3)
              (matrix-inv/get-core-item matrix-inventory)
              (matrix-inv/get-plate-item matrix-inventory slot)))
          
          (insertItem [_ slot stack simulate]
            (if (= slot 3)
              (when (matrix-inv/validate-core-item matrix-inventory stack)
                (if simulate
                  stack
                  (do
                    (matrix-inv/set-core-item matrix-inventory stack)
                    stack)))
              (when (matrix-inv/validate-plate-item matrix-inventory stack)
                (if simulate
                  stack
                  (do
                    (matrix-inv/set-plate-item matrix-inventory slot stack)
                    stack)))))
          
          (extractItem [_ slot amount simulate]
            (if (= slot 3)
              (when-let [core (matrix-inv/get-core-item matrix-inventory)]
                (if simulate
                  core
                  (do
                    (matrix-inv/set-core-item matrix-inventory nil)
                    core)))
              (when-let [plate (matrix-inv/get-plate-item matrix-inventory slot)]
                (if simulate
                  plate
                  (do
                    (matrix-inv/set-plate-item matrix-inventory slot nil)
                    plate)))))))))

(defn create-adapter [matrix-inventory]
  (->MatrixInventoryAdapter 
    matrix-inventory
    (inv-bridge/create-inventory-bridge)))