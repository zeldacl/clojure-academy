(ns cn.academy.block.matrix-inventory
  (:require [cn.academy.item.base-inventory :as inv]))

(def ^:private inventory-size 4) ; 3 plates + 1 core

(defprotocol IMatrixInventory
  "Matrix-specific inventory behavior"
  (get-core-item [this]
    "Get core item")
  (set-core-item [this item]
    "Set core item")
  (get-plate-item [this index]
    "Get plate item by index")
  (set-plate-item [this index item]
    "Set plate item by index")
  (get-plate-count [this]
    "Get number of installed plates")
  (validate-core-item [this item]
    "Validate if item can be used as core")
  (validate-plate-item [this item]
    "Validate if item can be used as plate"))

(defrecord MatrixInventory [inventory core-validator plate-validator]
  inv/IInventory
  (get-size [_]
    inventory-size)
  
  (get-stack [_]
    (inv/get-stack inventory))
  
  (set-stack [_ slot stack]
    (if (= slot 3)
      (when (validate-core-item stack)
        (inv/set-stack inventory slot stack))
      (when (validate-plate-item stack)
        (inv/set-stack inventory slot stack))))
  
  (remove-stack [_ slot amount]
    (inv/remove-stack inventory slot amount))
  
  (is-empty? [_]
    (inv/is-empty? inventory))
  
  (mark-dirty [_]
    (inv/mark-dirty inventory))

  IMatrixInventory
  (get-core-item [_]
    (inv/get-stack inventory 3))
  
  (set-core-item [this item]
    (when (validate-core-item this item)
      (inv/set-stack inventory 3 item)))
  
  (get-plate-item [_ index]
    (when (< index 3)
      (inv/get-stack inventory index)))
  
  (set-plate-item [this index item]
    (when (and (< index 3)
               (validate-plate-item this item))
      (inv/set-stack inventory index item)))
  
  (get-plate-count [_]
    (count (filter some? 
                   (map #(inv/get-stack inventory %) 
                       (range 3)))))
  
  (validate-core-item [_ item]
    (when item
      (core-validator item)))
  
  (validate-plate-item [_ item]
    (when item
      (plate-validator item))))

(defn create-matrix-inventory 
  "Create new matrix inventory with validators for core and plate items"
  [core-validator plate-validator]
  (->MatrixInventory 
    (inv/create-inventory inventory-size)
    core-validator
    plate-validator))

(defn get-inventory-data
  "Get serializable inventory data"
  [inventory]
  {:core (get-core-item inventory)
   :plates (vec (for [i (range 3)]
                  (get-plate-item inventory i)))})

(defn load-inventory-data!
  "Load inventory data"
  [inventory data]
  (set-core-item inventory (:core data))
  (doseq [[i plate] (map-indexed vector (:plates data))]
    (set-plate-item inventory i plate)))