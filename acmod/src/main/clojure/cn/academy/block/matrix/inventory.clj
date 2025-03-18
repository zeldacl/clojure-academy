(ns cn.academy.block.matrix.inventory
  (:require [cn.academy.inventory.core :as inv]))

(defrecord MatrixInventory [slots dirty-atom]
  inv/IInventory
  (get-size [_] (count slots))
  
  (get-stack [_ slot]
    (get slots slot))
  
  (set-stack [this slot stack]
    (reset! dirty-atom true)
    (assoc slots slot stack))
  
  (get-max-stack-size [_] 64)
  
  (is-valid-slot [this slot]
    (and (>= slot 0) 
         (< slot (inv/get-size this))))
  
  (is-empty? [_]
    (every? nil? (vals slots)))
  
  (is-valid-stack? [_ slot stack]
    true) ; Matrix accepts any item
  
  (mark-dirty [_]
    (reset! dirty-atom true))
  
  inv/IItemHandler
  (get-slots [this]
    (inv/get-size this))
  
  (get-stack-in-slot [this slot]
    (inv/get-stack this slot))
  
  (insert-item [this slot stack simulate?]
    (if simulate?
      stack
      (do
        (inv/set-stack this slot stack)
        stack)))
  
  (extract-item [this slot amount simulate?]
    (let [existing (inv/get-stack this slot)]
      (if simulate?
        existing
        (do 
          (inv/set-stack this slot nil)
          existing))))
  
  (get-slot-limit [_ _]
    64)
  
  (is-item-valid? [this slot stack]
    (inv/is-valid-stack? this slot stack)))

(defrecord MatrixInventoryFactory []
  inv/IInventoryFactory
  (create-inventory [_ size]
    (->MatrixInventory (vec (repeat size nil)) (atom false)))
  
  (load-from-nbt [this nbt]
    (let [size (get nbt "Size")
          inv (inv/create-inventory this size)]
      (doseq [i (range size)]
        (when-let [stack (get nbt (str "Slot" i))]
          (inv/set-stack inv i stack)))
      inv))
  
  (save-to-nbt [this]
    (let [size (inv/get-size this)
          nbt {"Size" size}]
      (reduce (fn [acc i]
                (if-let [stack (inv/get-stack this i)]
                  (assoc acc (str "Slot" i) stack)
                  acc))
              nbt
              (range size)))))

(defn create-factory []
  (->MatrixInventoryFactory))

(defn is-dirty? [inventory]
  @(:dirty-atom inventory))

(defn clear-dirty! [inventory]
  (reset! (:dirty-atom inventory) false))