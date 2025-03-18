(ns cn.academy.item.base-inventory)

(defprotocol IInventory
  "Base protocol for inventories"
  (get-size [this]
    "Get inventory size")
  (get-stack [this slot]
    "Get stack in slot")
  (set-stack [this slot stack]
    "Set stack in slot")
  (remove-stack [this slot amount]
    "Remove items from slot")
  (is-empty? [this]
    "Check if inventory is empty")
  (mark-dirty [this]
    "Mark inventory as changed"))

(defprotocol IItemStack
  "Base protocol for item stacks"
  (get-count [this]
    "Get stack size")
  (get-max-size [this]
    "Get max stack size")
  (split [this amount]
    "Split stack")
  (copy [this]
    "Copy stack")
  (is-empty? [this]
    "Check if stack is empty"))

(defprotocol IItemHandler
  "Base protocol for item handlers"
  (get-slots [this]
    "Get number of slots")
  (get-stack-in-slot [this slot]
    "Get stack in slot")
  (insert-item [this slot stack simulate]
    "Insert stack into slot")
  (extract-item [this slot amount simulate]
    "Extract items from slot"))

(defrecord BaseInventory [slots]
  IInventory
  (get-size [_]
    (count slots))
  
  (get-stack [_ slot]
    (get slots slot))
  
  (set-stack [this slot stack]
    (assoc slots slot stack)
    (mark-dirty this))
  
  (remove-stack [this slot amount]
    (when-let [stack (get-stack this slot)]
      (let [to-remove (min amount (get-count stack))
            remaining (- (get-count stack) to-remove)
            new-stack (if (pos? remaining)
                       (assoc stack :count remaining)
                       nil)]
        (set-stack this slot new-stack)
        (assoc stack :count to-remove))))
  
  (is-empty? [_]
    (every? nil? (vals slots)))
  
  (mark-dirty [_]
    nil)) ; Implementation provided by platform adapter

(defrecord ItemHandler [inventory]
  IItemHandler
  (get-slots [_]
    (get-size inventory))
  
  (get-stack-in-slot [_ slot]
    (get-stack inventory slot))
  
  (insert-item [_ slot stack simulate]
    (let [existing (get-stack inventory slot)]
      (if (or (nil? existing)
              (and existing (can-combine? existing stack)))
        (if simulate
          stack
          (do
            (set-stack inventory slot 
                      (combine-stacks existing stack))
            stack))
        stack)))
  
  (extract-item [_ slot amount simulate]
    (when-let [existing (get-stack inventory slot)]
      (if simulate
        (assoc existing :count (min amount (get-count existing)))
        (remove-stack inventory slot amount)))))

(defn create-inventory [size]
  (->BaseInventory (into {} (map #(vector % nil) (range size)))))

(defn create-item-handler [inventory]
  (->ItemHandler inventory))

(defn- can-combine? [stack1 stack2]
  (and (= (:item stack1) (:item stack2))
       (< (get-count stack1) (get-max-size stack1))))

(defn- combine-stacks [stack1 stack2]
  (if (nil? stack1)
    (copy stack2)
    (let [space (- (get-max-size stack1) (get-count stack1))
          amount (min space (get-count stack2))]
      (assoc stack1 :count (+ (get-count stack1) amount)))))