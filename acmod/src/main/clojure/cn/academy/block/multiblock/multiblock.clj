(ns cn.academy.block.multiblock.multiblock
  (:require [mcmod.protocols :refer [IMultiblock IBlockEntity IWorld]]
            [cn.academy.block.multiblock.pattern :as pattern]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [clojure.tools.logging :as log]))

;; Multiblock controller implementation
(defrecord MultiblockController [blocks master-pos pattern world]
  IMultiblock
  (is-complete? [this]
    (and (every? #(get-tile-entity world %) blocks)
         (pattern/matches-pattern? pattern blocks)))
    
  (get-blocks [_]
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

  IBlockEntity
  (tick [this]
    (when (not (is-remote? world))
      (when (is-complete? this)  
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
        required-positions (set (pattern/get-required-positions (get-controller this)))]
    (= positions required-positions)))

(defn- process-multiblock [this]
  (when-let [master (get-controller this)]
    ;; Process energy distribution
    (when (satisfies? IEnergyStorage master)
      (let [storage-blocks (->> (get-blocks this)
                               (map #(get-tile-entity world %))
                               (filter #(satisfies? IEnergyStorage %)))
            total-energy (reduce + (map get-energy-stored storage-blocks))
            energy-per-block (quot total-energy (count storage-blocks))]
        (doseq [block storage-blocks]
          (distribute-energy! block energy-per-block))))))

;; Public interface
(defn create-multiblock [world pattern]
  (->MultiblockController #{} nil pattern world))

(defn add-block [controller pos]
  (update controller :blocks conj pos))

(defn remove-block [controller pos]
  (update controller :blocks disj pos))

(defn set-master [controller pos]
  (assoc controller :master-pos pos))

(defn distribute-energy! [block target-amount]
  (let [current (get-energy-stored block)
        diff (- target-amount current)]
    (if (pos? diff)
      (receive-energy block diff false)
      (extract-energy block (- diff) false))))