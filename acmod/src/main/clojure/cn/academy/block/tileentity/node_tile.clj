(ns cn.academy.block.tileentity.node-tile
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.block-node :as block-node])
  (:import [net.minecraft.nbt NBTTagCompound]
           [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]))

(defprotocol IWirelessNode
  (get-node-type [this])
  (get-energy [this])
  (add-energy [this amount])
  (consume-energy [this amount])
  (get-max-energy [this])
  (get-bandwidth [this])
  (get-range [this])
  (get-capacity [this])
  (set-placer [this player])
  (get-placer [this])
  (is-enabled [this])
  (set-enabled [this enabled]))

(defrecord NodeTileEntity [state-atom inventory]
  ITileEntity
  (tick [this]
    ;; Process energy transfer from items in inventory
    (let [state @state-atom]
      (when (:enabled state)
        ;; Handle wireless energy transfer logic here
        (mark-dirty this))))
  
  (get-capabilities [this]
    ;; Return energy capability
    {:energy (get-energy this)})
  
  (read-from-nbt [this nbt]
    (reset! state-atom
            {:energy (.getDouble nbt "energy")
             :enabled (.getBoolean nbt "enabled")
             :placer-id (.getString nbt "placerId")
             :node-type (keyword (.getString nbt "nodeType"))
             :password (.getString nbt "password")
             :node-name (.getString nbt "nodeName")}))
  
  (write-to-nbt [this nbt]
    (let [state @state-atom]
      (doto nbt
        (.setDouble "energy" (:energy state))
        (.setBoolean "enabled" (:enabled state))
        (.setString "placerId" (or (:placer-id state) ""))
        (.setString "nodeType" (name (:node-type state)))
        (.setString "password" (or (:password state) ""))
        (.setString "nodeName" (or (:node-name state) "")))))
  
  (mark-dirty [_]
    ;; Mark tile for saving
    nil)

  IWirelessNode
  (get-node-type [_]
    (:node-type @state-atom))
  
  (get-energy [_]  
    (:energy @state-atom))
  
  (add-energy [this amount]
    (swap! state-atom update :energy + amount)
    (mark-dirty this))
  
  (consume-energy [this amount]
    (when (>= (:energy @state-atom) amount)
      (swap! state-atom update :energy - amount)
      (mark-dirty this)
      true))
  
  (get-max-energy [this]
    (get-in block-node/node-types [(get-node-type this) :max-energy]))
  
  (get-bandwidth [this]
    (get-in block-node/node-types [(get-node-type this) :bandwidth]))
  
  (get-range [this]
    (get-in block-node/node-types [(get-node-type this) :range]))
  
  (get-capacity [this]
    (get-in block-node/node-types [(get-node-type this) :capacity]))
  
  (set-placer [this player]
    (swap! state-atom assoc :placer-id (.getUniqueID player))
    (mark-dirty this))
  
  (get-placer [_]
    (:placer-id @state-atom))
  
  (is-enabled [_]
    (:enabled @state-atom))
  
  (set-enabled [this enabled]
    (swap! state-atom assoc :enabled enabled)
    (mark-dirty this)))

;; Factory function
(defn create-node-tile [node-type]
  (->NodeTileEntity 
    (atom {:energy 0.0
           :enabled false
           :placer-id nil
           :node-type node-type
           :password ""
           :node-name ""})
    ;; Create inventory based on capacity
    (let [capacity (get-in block-node/node-types [node-type :capacity])]
      (reify IInventory
        (getSizeInventory [_] capacity)
        (isEmpty [_] true)  ;; Implement actual inventory logic
        (getStackInSlot [_ slot] nil)
        (decrStackSize [_ slot amount] nil)
        (removeStackFromSlot [_ slot] nil)
        (setInventorySlotContents [_ slot stack] nil)
        (getInventoryStackLimit [_] 64)
        (markDirty [_] nil)
        (isUsableByPlayer [_ player] true)
        (openInventory [_ player] nil)
        (closeInventory [_ player] nil)
        (isItemValidForSlot [_ slot stack] true)
        (getField [_ id] 0)
        (setField [_ id value] nil)
        (getFieldCount [_] 0)
        (clear [_] nil)))))

;; Export for Java interop
(gen-class
  :name cn.academy.block.tileentity.TileNode
  :prefix "tile-"
  :init init
  :state state
  :constructors {[] []}
  :methods [[getEnergy [] double]
            [setEnergy [double] void]
            [getMaxEnergy [] double]
            [getBandwidth [] int]
            [getRange [] int]
            [getCapacity [] int]
            [setPlacer [Object] void]
            [isEnabled [] boolean]
            [setEnabled [boolean] void]])

(defn tile-init []
  [[] (create-node-tile :basic)])

(defn tile-getEnergy [this]
  (get-energy (.state this)))

(defn tile-setEnergy [this amount]
  (swap! (:state-atom (.state this)) assoc :energy amount))

(defn tile-getMaxEnergy [this]
  (get-max-energy (.state this)))

(defn tile-getBandwidth [this]
  (get-bandwidth (.state this)))

(defn tile-getRange [this]
  (get-range (.state this)))

(defn tile-getCapacity [this]
  (get-capacity (.state this)))

(defn tile-setPlacer [this player]
  (set-placer (.state this) player))

(defn tile-isEnabled [this]
  (is-enabled (.state this)))

(defn tile-setEnabled [this enabled]
  (set-enabled (.state this) enabled))