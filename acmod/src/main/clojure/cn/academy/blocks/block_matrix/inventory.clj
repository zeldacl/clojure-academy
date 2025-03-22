(ns cn.academy.blocks.block-matrix.inventory
  "Matrix inventory management for handling cores and plates."
  (:require [cn.academy.blocks.block-matrix.protocols :refer [IMatrixInventory]]
            [cn.academy.blocks.block-matrix.utils :as utils]
            [cn.academy.blocks.block-matrix.config :as config]
            [clojure.tools.logging :as log]))

;; Common item validation predicates
(defn is-valid-core? 
  "Check if an item is a valid matrix core"
  [item]
  (when item
    (let [item-id (.getRegistryName item)]
      (some #(.equals item-id %) 
            ["academy:matrix_core_basic" 
             "academy:matrix_core_standard" 
             "academy:matrix_core_advanced"]))))

(defn is-valid-plate?
  "Check if an item is a valid matrix plate"
  [item]
  (when item
    (let [item-id (.getRegistryName item)]
      (some #(.equals item-id %) 
            ["academy:matrix_plate_iron" 
             "academy:matrix_plate_gold" 
             "academy:matrix_plate_crystal"]))))

(defn get-core-level
  "Get the level of a core item (0-based)"
  [item]
  (when (is-valid-core? item)
    (let [item-id (.getRegistryName item)]
      (case (str item-id)
        "academy:matrix_core_basic" 0
        "academy:matrix_core_standard" 1
        "academy:matrix_core_advanced" 2
        0))))

(defn get-plate-level
  "Get the level of a plate item (0-based)"
  [item]
  (when (is-valid-plate? item)
    (let [item-id (.getRegistryName item)]
      (case (str item-id)
        "academy:matrix_plate_iron" 0
        "academy:matrix_plate_gold" 1
        "academy:matrix_plate_crystal" 2
        0))))

;; Matrix inventory implementation
(defrecord MatrixInventory [config inventory-atom]
  IMatrixInventory
  (get-core-item [_]
    (:core @inventory-atom))
  
  (set-core-item [_ item]
    (swap! inventory-atom assoc :core 
           (utils/validate-item item is-valid-core?)))
  
  (get-plate-item [_ index]
    (when-let [validated-index (config/validate-plate-slot config index)]
      (get-in @inventory-atom [:plates validated-index])))
  
  (set-plate-item [_ index item]
    (when-let [validated-index (config/validate-plate-slot config index)]
      (swap! inventory-atom assoc-in [:plates validated-index] 
             (utils/validate-item item is-valid-plate?))))
  
  (is-valid-core? [_ item]
    (boolean (utils/validate-item item is-valid-core?)))
  
  (is-valid-plate? [_ item]
    (boolean (utils/validate-item item is-valid-plate?))))

;; Additional inventory utility functions
(defn get-plate-count
  "Get the count of non-empty plate slots"
  [inventory]
  (count (filter some? 
                (map #(get-plate-item inventory %) 
                     (range utils/MAX_PLATES)))))

(defn get-inventory-data
  "Serialize inventory to a data map"
  [inventory]
  {:core (get-core-item inventory)
   :plates (mapv #(get-plate-item inventory %) 
                 (range utils/MAX_PLATES))})

(defn load-inventory-data!
  "Load inventory data from a data map"
  [inventory data]
  (set-core-item inventory (:core data))
  (doseq [[i plate] (map-indexed vector (:plates data))]
    (set-plate-item inventory i plate)))

(defn create-inventory
  "Create a new matrix inventory"
  [config]
  (let [max-plates (config/get-config-value config :inventory :max-plates)
        empty-plates (vec (repeat max-plates nil))]
    (->MatrixInventory config (atom {:core nil
                                     :plates empty-plates}))))