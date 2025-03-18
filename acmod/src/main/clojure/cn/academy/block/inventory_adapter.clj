(ns cn.academy.block.inventory-adapter)

(defprotocol IInventoryAdapter
  "Generic inventory adapter protocol that any inventory system can implement"
  (get-size [this]
    "Returns the number of slots in this inventory")
  
  (get-item [this slot]
    "Gets the item in the given slot")
  
  (set-item! [this slot item]
    "Sets an item in the given slot")
  
  (get-max-stack-size [this]
    "Gets the maximum stack size for this inventory")
  
  (validate-item [this slot item]
    "Validates if an item can be placed in the given slot"))

(defn create-default-adapter
  "Creates a basic inventory adapter implementation with the given properties"
  [{:keys [size max-stack-size validator items]
    :or {size 1
         max-stack-size 64
         validator (constantly true)
         items (atom {})}}]
  (reify IInventoryAdapter
    (get-size [_] size)
    
    (get-item [_ slot]
      (get @items slot))
    
    (set-item! [_ slot item]
      (if item
        (swap! items assoc slot item)
        (swap! items dissoc slot))
      item)
    
    (get-max-stack-size [_]
      max-stack-size)
    
    (validate-item [_ slot item]
      (validator slot item))))