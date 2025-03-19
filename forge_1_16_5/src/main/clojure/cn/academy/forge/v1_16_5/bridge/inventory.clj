(ns cn.academy.forge.v1_16_5.bridge.inventory
  (:require [cn.academy.inventory.core :as inv]
            [cn.academy.item.base-inventory :as base-inv])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraftforge.items IItemHandler IItemHandlerModifiable]
           [net.minecraft.item ItemStack]
           [net.minecraft.nbt CompoundNBT ListNBT]))

;; Combining inventory_bridge.clj and inventory_adapter.clj into one file

;; --- Inventory Bridge ---

(defprotocol IForgeInventoryBridge
  "Bridge between platform-independent inventory and Forge inventory"
  (wrap-forge-inventory [this forge-inv]
    "Wrap Forge inventory with platform-independent interface")
  (wrap-forge-item-handler [this forge-handler]
    "Wrap Forge item handler with platform-independent interface")
  (to-forge-inventory [this inventory]
    "Convert platform-independent inventory to Forge inventory")
  (to-forge-item-handler [this handler]
    "Convert platform-independent handler to Forge item handler"))

(defrecord ForgeInventoryWrapper [forge-inv]
  base-inv/IInventory
  (get-size [_]
    (.getContainerSize forge-inv))
  
  (get-stack [_ slot]
    (let [stack (.getItem forge-inv slot)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (set-stack [_ slot stack]
    (.setItem forge-inv slot
              (if stack
                (ItemStack. (:item stack) (:count stack))
                ItemStack/EMPTY)))
  
  (remove-stack [_ slot amount]
    (let [stack (.removeItem forge-inv slot amount)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (is-empty? [_]
    (.isEmpty forge-inv))
  
  (mark-dirty [_]
    (.setChanged forge-inv)))

(defrecord ForgeItemHandlerWrapper [forge-handler]
  base-inv/IItemHandler
  (get-slots [_]
    (.getSlots forge-handler))
  
  (get-stack-in-slot [_ slot]
    (let [stack (.getStackInSlot forge-handler slot)]
      {:item (.getItem stack)
       :count (.getCount stack)}))
  
  (insert-item [_ slot stack simulate]
    (let [result (.insertItem forge-handler slot
                             (ItemStack. (:item stack) (:count stack))
                             simulate)]
      {:item (.getItem result)
       :count (.getCount result)}))
  
  (extract-item [_ slot amount simulate]
    (let [result (.extractItem forge-handler slot amount simulate)]
      {:item (.getItem result)
       :count (.getCount result)})))

(defrecord ForgeInventoryBridge []
  IForgeInventoryBridge
  (wrap-forge-inventory [_ forge-inv]
    (->ForgeInventoryWrapper forge-inv))
  
  (wrap-forge-item-handler [_ forge-handler]
    (->ForgeItemHandlerWrapper forge-handler))
  
  (to-forge-inventory [_ inventory]
    (reify IInventory
      (getContainerSize [_]
        (base-inv/get-size inventory))
      
      (getItem [_ slot]
        (if-let [stack (base-inv/get-stack inventory slot)]
          (ItemStack. (:item stack) (:count stack))
          ItemStack/EMPTY))
      
      (setItem [_ slot stack]
        (base-inv/set-stack inventory slot
                      (when-not (.isEmpty stack)
                        {:item (.getItem stack)
                         :count (.getCount stack)})))
      
      (removeItem [_ slot amount]
        (let [result (base-inv/remove-stack inventory slot amount)]
          (if result
            (ItemStack. (:item result) (:count result))
            ItemStack/EMPTY)))
      
      (isEmpty [_]
        (base-inv/is-empty? inventory))
      
      (setChanged [_]
        (base-inv/mark-dirty inventory))))
  
  (to-forge-item-handler [_ handler]
    (reify IItemHandler
      (getSlots [_]
        (base-inv/get-slots handler))
      
      (getStackInSlot [_ slot]
        (if-let [stack (base-inv/get-stack-in-slot handler slot)]
          (ItemStack. (:item stack) (:count stack))
          ItemStack/EMPTY))
      
      (insertItem [_ slot stack simulate]
        (let [result (base-inv/insert-item handler slot
                                     {:item (.getItem stack)
                                      :count (.getCount stack)}
                                     simulate)]
          (if result
            (ItemStack. (:item result) (:count result))
            ItemStack/EMPTY)))
      
      (extractItem [_ slot amount simulate]
        (let [result (base-inv/extract-item handler slot amount simulate)]
          (if result
            (ItemStack. (:item result) (:count result))
            ItemStack/EMPTY))))))

;; --- Inventory Adapter ---

(defn- to-forge-stack [stack]
  (if (nil? stack)
    ItemStack/EMPTY
    stack))

(deftype ForgeItemHandler [inventory]
  IItemHandler
  (getSlots [_]
    (inv/get-slots inventory))
  
  (getStackInSlot [_ slot]
    (to-forge-stack (inv/get-stack-in-slot inventory slot)))
  
  (insertItem [_ slot stack simulate]
    (to-forge-stack (inv/insert-item inventory slot stack simulate)))
  
  (extractItem [_ slot amount simulate]
    (to-forge-stack (inv/extract-item inventory slot amount simulate)))
  
  (getSlotLimit [_ slot]
    (inv/get-slot-limit inventory slot))
  
  (isItemValid [_ slot stack]
    (inv/is-item-valid? inventory slot stack))
  
  IItemHandlerModifiable
  (setStackInSlot [_ slot stack]
    (inv/set-stack inventory slot (to-forge-stack stack))))

(defprotocol IInventoryFactory
  (create-inventory [this size] "Create inventory")
  (create-handler [this inventory] "Create item handler adapter"))

(defrecord ForgeInventoryFactory []
  IInventoryFactory
  (create-inventory [_ size]
    (inv/create-inventory size))
  
  (create-handler [_ inventory]
    (->ForgeItemHandler inventory)))

;; --- Simplified API ---

(defn create-inventory-bridge []
  (->ForgeInventoryBridge))

(defn create-factory []
  (->ForgeInventoryFactory))

(defn create-forge-adapter 
  "Creates a Forge-compatible inventory adapter for the given inventory instance"
  [inventory]
  {:inventory (to-forge-inventory (->ForgeInventoryBridge) inventory)
   :item-handler (to-forge-item-handler (->ForgeInventoryBridge) inventory)})

(defn save-to-nbt [inventory]
  (let [nbt (CompoundNBT.)]
    (doseq [[k v] (inv/save-to-nbt inventory)]
      (.putString nbt k v))
    nbt))

(defn load-from-nbt [factory nbt]
  (let [data (reduce (fn [m k]
                      (assoc m k (.getString nbt k)))
                    {}
                    (iterator-seq (.keySet nbt)))]
    (inv/load-from-nbt factory data)))