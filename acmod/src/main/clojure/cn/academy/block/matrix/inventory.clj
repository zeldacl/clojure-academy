(ns cn.academy.block.matrix.inventory
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixInventory
  (get-core-item [this])
  (set-core-item [this item])
  (get-plate-item [this index])
  (set-plate-item [this index item])
  (get-plate-count [this])
  (validate-core-item [this item])
  (validate-plate-item [this item]))

(defrecord MatrixInventory [matrix inventory-atom]
  IMatrixInventory
  (get-core-item [_]
    (:core @inventory-atom))
  
  (set-core-item [_ item]
    (swap! inventory-atom assoc :core item))
  
  (get-plate-item [_ index]
    (get-in @inventory-atom [:plates index]))
  
  (set-plate-item [_ index item]
    (swap! inventory-atom assoc-in [:plates index] item))
  
  (get-plate-count [_]
    (count (filter some? (:plates @inventory-atom))))
  
  (validate-core-item [_ item]
    (and item (matrix/is-valid-core? matrix item)))
  
  (validate-plate-item [_ item]
    (and item (matrix/is-valid-plate? matrix item))))

(defn create-inventory [matrix]
  (->MatrixInventory matrix 
                     (atom {:core nil
                           :plates [nil nil nil]})))

(defn get-inventory-data [inventory]
  {:core (get-core-item inventory)
   :plates (mapv #(get-plate-item inventory %) (range 3))})

(defn load-inventory-data! [inventory data]
  (set-core-item inventory (:core data))
  (doseq [[i plate] (map-indexed vector (:plates data))]
    (set-plate-item inventory i plate)))