(ns cn.academy.block.inventory-adapter
  (:import [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]
           [net.minecraft.entity.player PlayerEntity]))

(defprotocol IForgeInventoryAdapter
  (create-inventory [this])
  (get-size [this])
  (get-item [this slot])
  (set-item [this slot item])
  (remove-item [this slot amount])
  (is-empty? [this])
  (can-player-use? [this player])
  (mark-dirty! [this]))

(defrecord ForgeInventory [handler]
  IForgeInventoryAdapter
  (create-inventory [_]
    (reify IInventory
      (getContainerSize [_] 
        (get-size handler))
      
      (getItem [_ slot] 
        (get-item handler slot))
      
      (setItem [_ slot item] 
        (set-item handler slot item))
      
      (removeItem [_ slot amount]
        (remove-item handler slot amount))
      
      (isEmpty [_]
        (is-empty? handler))
      
      (stillValid [_ player]
        (can-player-use? handler player))
      
      (setChanged [_]
        (mark-dirty! handler))))

  (get-size [_] 
    ((:get-size handler)))
  
  (get-item [_ slot]
    ((:get-item handler) slot))
  
  (set-item [_ slot item]
    ((:set-item handler) slot item))
  
  (remove-item [_ slot amount]
    ((:remove-item handler) slot amount))
  
  (is-empty? [_]
    ((:is-empty? handler)))
  
  (can-player-use? [_ player]
    ((:can-player-use? handler) player))
  
  (mark-dirty! [_]
    ((:mark-dirty! handler))))

(defn create-adapter [handler]
  (->ForgeInventory handler))