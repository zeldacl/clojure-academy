(ns cn.academy.block.multiblock.multiblock
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [clojure.tools.logging :as log]))

(defrecord MultiblockController [blocks master-pos pattern world]
  IMultiblock
  (is-complete? [this]
    (every? #(get-tile-entity world %) blocks))
    
  (get-blocks [this]
    blocks)
    
  (get-controller [this]
    (when-let [master (get-tile-entity world master-pos)]
      master))
      
  (validate-structure [this]
    (and (is-complete? this)
         (validate-pattern this pattern)))
         
  (on-structure-formed [this]
    (doseq [pos blocks]
      (when-let [te (get-tile-entity world pos)]
        (when (satisfies? base/IMultiblockMember te)
          (base/on-connection te this)))))
          
  (on-structure-broken [this]
    (doseq [pos blocks]
      (when-let [te (get-tile-entity world pos)]
        (when (satisfies? base/IMultiblockMember te)
          (base/set-controller te nil)))))

  ITileEntity
  (tick [this]
    (when (not (is-remote? world))
      (when (validate-structure this)  
        (process-multiblock this))))
        
  (save [this]
    {:blocks blocks
     :master master-pos
     :pattern pattern})
     
  (load [this data]
    (->MultiblockController (:blocks data)
                           (:master data)
                           (:pattern data)
                           world)))
                           
;; Private helper functions
(defn- validate-pattern [this pattern]
  (let [positions (set (get-blocks this))
        required-positions (set (pattern (get-controller this)))]
    (= positions required-positions)))

(defn- process-multiblock [this]
  (when-let [master (get-controller this)]
    ;; Process energy transfer between blocks
    (let [storage-blocks (->> (get-blocks this)
                             (map #(get-tile-entity world %))
                             (filter #(satisfies? IEnergyStorage %)))]
      (when (seq storage-blocks)
        (let [total-energy (reduce + (map get-energy-stored storage-blocks))
              energy-per-block (quot total-energy (count storage-blocks))]
          (doseq [block storage-blocks]
            (let [current (get-energy-stored block)
                  diff (- energy-per-block current)]
              (if (pos? diff)
                (receive-energy block diff false)
                (extract-energy block (- diff) false)))))))))

(defn create-multiblock [world pattern]
  (->MultiblockController #{} nil pattern world))

(defn add-block [controller pos]
  (update controller :blocks conj pos))

(defn remove-block [controller pos]
  (update controller :blocks disj pos))

(defn set-master [controller pos]
  (assoc controller :master-pos pos))