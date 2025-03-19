(ns cn.academy.block.matrix-inventory
  (:require [cn.academy.item.base-inventory :as inv]
            [cn.academy.block.matrix :as matrix])
  (:import [net.minecraft.item ItemStack]))

(def ^:private inventory-size 4) ; 3 plates + 1 core
(def ^:const CORE_SLOT 0)
(def ^:const FIRST_PLATE_SLOT 1)
(def ^:const PLATE_SLOTS 3)
(def ^:const TOTAL_SLOTS (inc PLATE_SLOTS))

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
    "Validate if item can be used as plate")
  (get-slots [this])
  (get-stack-in-slot [this slot])
  (insert-item [this slot stack simulate])
  (extract-item [this slot amount simulate])
  (get-slot-limit [this slot])
  (is-valid-item [this slot stack]))

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
      (plate-validator item)))
  
  (get-slots [_]
    TOTAL_SLOTS)

  (get-stack-in-slot [_ slot]
    (if (= slot CORE_SLOT)
      (get @inventory-atom :core ItemStack/EMPTY)
      (get-in @inventory-atom [:plates (dec slot)] ItemStack/EMPTY)))

  (insert-item [this slot stack simulate]
    (if (.isEmpty stack)
      stack
      (when (is-valid-item this slot stack)
        (let [current (get-stack-in-slot this slot)
              max-size (get-slot-limit this slot)
              insert-amount (min (.getCount stack)
                               (if (.isEmpty current)
                                 max-size
                                 (- max-size (.getCount current))))]
          (when (pos? insert-amount)
            (when-not simulate
              (if (= slot CORE_SLOT)
                (swap! inventory-atom assoc :core 
                       (doto (.copy stack)
                         (.setCount insert-amount)))
                (swap! inventory-atom assoc-in [:plates (dec slot)]
                       (doto (.copy stack)
                         (.setCount insert-amount)))))
            (doto (.copy stack)
              (.shrink insert-amount)))))))

  (extract-item [this slot amount simulate]
    (let [existing (get-stack-in-slot this slot)]
      (when-not (.isEmpty existing)
        (let [extract-amount (min amount (.getCount existing))]
          (when (pos? extract-amount)
            (if simulate
              (doto (.copy existing)
                (.setCount extract-amount))
              (do
                (if (= slot CORE_SLOT)
                  (swap! inventory-atom update :core 
                         #(doto % (.shrink extract-amount)))
                  (swap! inventory-atom update-in [:plates (dec slot)]
                         #(doto % (.shrink extract-amount))))
                (doto (.copy existing)
                  (.setCount extract-amount)))))))))

  (get-slot-limit [_ slot]
    64)

  (is-valid-item [_ slot stack]
    (if (= slot CORE_SLOT)
      (matrix/is-valid-core? matrix stack)
      (matrix/is-valid-plate? matrix stack))))

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

(defrecord MatrixInventoryHandler [matrix inventory-atom]
  IMatrixInventory
  (get-slots [_]
    TOTAL_SLOTS)

  (get-stack-in-slot [_ slot]
    (if (= slot CORE_SLOT)
      (get @inventory-atom :core ItemStack/EMPTY)
      (get-in @inventory-atom [:plates (dec slot)] ItemStack/EMPTY)))

  (insert-item [this slot stack simulate]
    (if (.isEmpty stack)
      stack
      (when (is-valid-item this slot stack)
        (let [current (get-stack-in-slot this slot)
              max-size (get-slot-limit this slot)
              insert-amount (min (.getCount stack)
                               (if (.isEmpty current)
                                 max-size
                                 (- max-size (.getCount current))))]
          (when (pos? insert-amount)
            (when-not simulate
              (if (= slot CORE_SLOT)
                (swap! inventory-atom assoc :core 
                       (doto (.copy stack)
                         (.setCount insert-amount)))
                (swap! inventory-atom assoc-in [:plates (dec slot)]
                       (doto (.copy stack)
                         (.setCount insert-amount)))))
            (doto (.copy stack)
              (.shrink insert-amount)))))))

  (extract-item [this slot amount simulate]
    (let [existing (get-stack-in-slot this slot)]
      (when-not (.isEmpty existing)
        (let [extract-amount (min amount (.getCount existing))]
          (when (pos? extract-amount)
            (if simulate
              (doto (.copy existing)
                (.setCount extract-amount))
              (do
                (if (= slot CORE_SLOT)
                  (swap! inventory-atom update :core 
                         #(doto % (.shrink extract-amount)))
                  (swap! inventory-atom update-in [:plates (dec slot)]
                         #(doto % (.shrink extract-amount))))
                (doto (.copy existing)
                  (.setCount extract-amount)))))))))

  (get-slot-limit [_ slot]
    64)

  (is-valid-item [_ slot stack]
    (if (= slot CORE_SLOT)
      (matrix/is-valid-core? matrix stack)
      (matrix/is-valid-plate? matrix stack))))

(defn create-inventory-handler [matrix]
  (->MatrixInventoryHandler matrix (atom {:core ItemStack/EMPTY
                                         :plates []})))