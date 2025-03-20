(ns cn.academy.block.container.node-container
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.tileentity.tile-node :as tile-node])
  (:import [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.inventory IInventory]))

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
        ;; Handle shift-clicking logic for energy items
        (let [remaining (.mergeItemStack inventory stack 0 9 false)]
          (when (pos? remaining)
            (.mergeItemStack inventory stack 9 36 false))))))
  
  (merge-stack [_ slot stack]
    (.mergeItemStack inventory stack 
                    (.slotNumber slot) 
                    (inc (.slotNumber slot)) 
                    false))
  
  (detect-sync-changes [_]
    (.detectAndSendChanges inventory)))

(defprotocol INodeContainer
  (get-node [this])
  (get-energy [this])
  (get-max-energy [this])
  (get-bandwidth [this])
  (get-range [this])
  (get-capacity [this]))

(defrecord NodeContainerImpl [container tile]
  INodeContainer
  (get-node [_] tile)
  
  (get-energy [_]
    (tile-node/get-energy tile))
  
  (get-max-energy [_]
    (tile-node/get-max-energy tile))
  
  (get-bandwidth [_]
    (tile-node/get-bandwidth tile))
  
  (get-range [_]
    (tile-node/get-range tile))
  
  (get-capacity [_]
    (tile-node/get-capacity tile)))

;; Factory function
(defn create-container [tile player]
  (let [inventory (tile-node/get-inventory tile)
        container (->NodeContainer tile player inventory)]
    (->NodeContainerImpl container tile)))

;; Export for Java interop
(gen-class
  :name cn.academy.block.container.ContainerNode
  :prefix "container-"
  :state state
  :init init
  :constructors {[Object Object] []}
  :methods [[getTileEntity [] Object]
            [getPlayer [] Object]])

(defn container-init [tile player]
  [[] (create-container tile player)])

(defn container-getTileEntity [this]
  (get-node (.state this)))

(defn container-getPlayer [this]
  (:player (.state this)))