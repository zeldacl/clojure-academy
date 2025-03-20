(ns forge-impl.container.node-container-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.inventory.container Container Slot]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]))

;; Custom slot implementation for node inventory
(defn- create-node-slot [inventory index x y]
  (proxy [Slot] [inventory index x y]
    (isItemValid [stack]
      ;; Add energy item validation logic here
      true)))

(defn create-forge-node-container [mcmod-container player inventory]
  (proxy [Container] [nil 0]
    ;; Add player inventory slots
    (addSlot [slot]
      (proxy-super addSlot slot))
    
    (canInteractWith [player]
      (can-interact-with mcmod-container player))
    
    (transferStackInSlot [player slot-id]
      (transfer-stack mcmod-container player slot-id)
      ItemStack/EMPTY)
    
    (onContainerClosed [player]
      (proxy-super onContainerClosed player))))

(defn create-container-slots [container inventory]
  ;; Add node inventory slots
  (let [slot-count (get-slots inventory)]
    (dotimes [i slot-count]
      (let [x (+ 8 (* (mod i 9) 18))
            y (+ 18 (* (quot i 9) 18))]
        (.addSlot container 
                  (create-node-slot inventory i x y))))
    
    ;; Add player inventory slots
    (dotimes [i 3]
      (dotimes [j 9]
        (let [x (+ 8 (* j 18))
              y (+ 84 (* i 18))]
          (.addSlot container
                    (Slot. (.inventory player)
                          (+ (* i 9) j 9)
                          x y)))))
    
    ;; Add player hotbar slots
    (dotimes [i 9]
      (.addSlot container
                (Slot. (.inventory player)
                      i
                      (+ 8 (* i 18))
                      142))))))