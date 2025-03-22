(ns cn.academy.blocks.block-node.container
  (:require [mcmod.protocols :refer :all]
            [cn.academy.blocks.block-node.tile :as tile]
            [cn.academy.blocks.block-node.ui-components :as ui]))

;; Simplified container implementation
(defrecord NodeContainer [tile-entity player inventory]
  IContainer
  (get-slots [_]
    (range (.getSizeInventory inventory)))
  
  (get-slot [_ idx]
    (.getStackInSlot inventory idx))
  
  (set-slot [_ idx stack]
    (.setInventorySlotContents inventory idx stack))
  
  (can-interact-with [_ player]
    true)
  
  (transfer-stack [_ player idx]
    (let [slot (.getSlot inventory idx)
          stack (.getStack slot)]
      (when stack
        ;; Simplified merging logic
        (or (.mergeItemStack inventory stack 0 9 false)
            (.mergeItemStack inventory stack 9 36 false)))))

  (merge-stack [_ slot stack]
    (.mergeItemStack inventory stack 
                    (.slotNumber slot) 
                    (inc (.slotNumber slot)) 
                    false))
  
  (detect-sync-changes [_]
    (.detectAndSendChanges inventory)))

;; Single container implementation with both IContainer and node-specific functionality
(defrecord NodeContainerImpl [inventory player tile-entity]
  IContainer
  (get-slots [_] (range (.getSizeInventory inventory)))
  (get-slot [_ idx] (.getStackInSlot inventory idx))
  (set-slot [_ idx stack] (.setInventorySlotContents inventory idx stack))
  (can-interact-with [_ _] true)
  (transfer-stack [this player idx] 
    (when-let [stack (.getStack (.getSlot inventory idx))]
      (or (.mergeItemStack inventory stack 0 9 false)
          (.mergeItemStack inventory stack 9 36 false))))
  (merge-stack [_ slot stack]
    (.mergeItemStack inventory stack (.slotNumber slot) (inc (.slotNumber slot)) false))
  (detect-sync-changes [_] (.detectAndSendChanges inventory))
  
  ;; Additional node-specific functionality
  Object
  (getTileEntity [_] tile-entity)
  (getPlayer [_] player)
  (getEnergy [_] (ui/get-tile-property tile-entity :energy))
  (getMaxEnergy [_] (ui/get-tile-property tile-entity :max-energy))
  (getBandwidth [_] (ui/get-tile-property tile-entity :bandwidth))
  (getRange [_] (ui/get-tile-property tile-entity :range)))

;; Factory function
(defn create-container [tile player]
  (let [inventory (tile/get-inventory tile)]
    (->NodeContainerImpl inventory player tile)))

;; Export for Java interop
(gen-class
  :name cn.academy.blocks.block-node.ContainerNode
  :prefix "container-"
  :state state
  :init init
  :constructors {[Object Object] []}
  :methods [[getTileEntity [] Object]
            [getPlayer [] Object]])

(defn container-init [tile player]
  [[] (create-container tile player)])

;; Use the common Java interop accessors
(ui/define-java-accessor "container" "getTileEntity" :tile-entity)
(ui/define-java-accessor "container" "getPlayer" :player)