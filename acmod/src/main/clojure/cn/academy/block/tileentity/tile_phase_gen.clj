(ns cn.academy.block.tileentity.tile-phase-gen
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [clojure.tools.logging :as log]))

;; Phase Generator TileEntity constants
(def ^:const ENERGY_MAX 100000.0)
(def ^:const ENERGY_GEN_PER_TICK 5.0)

;; Phase Generator TileEntity state
(defrecord PhaseGenState [energy active facing])

(defn create-phase-gen-state []
  (->PhaseGenState 0.0 false 0))

(defn tick-phase-gen [state]
  (if (:active state)
    (update state :energy #(min (+ % ENERGY_GEN_PER_TICK) ENERGY_MAX))
    state))

;; TileEntity implementation for Forge
(defn create-phase-gen-tile []
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-phase-gen-state))]
    
    ;; Register capability provider for energy
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        ENERGY_MAX 
        (fn [] (:energy @state-atom))
        (fn [amount] (swap! state-atom assoc :energy amount))))
    
    ;; Register tick method
    (block-api/on-tile-entity-tick! 
      tile-entity 
      (fn []
        (swap! state-atom tick-phase-gen)
        (when (zero? (mod (block-api/get-world-time) 20))
          (block-api/mark-dirty! tile-entity))))
    
    ;; Implement NBT serialization
    (block-api/on-save-nbt! 
      tile-entity 
      (fn [compound]
        (let [state @state-atom]
          (doto compound
            (block-api/put-double! "energy" (:energy state))
            (block-api/put-boolean! "active" (:active state))
            (block-api/put-int! "facing" (:facing state))))))
    
    ;; Implement NBT deserialization
    (block-api/on-load-nbt! 
      tile-entity 
      (fn [compound]
        (reset! state-atom
                (->PhaseGenState
                  (block-api/get-double compound "energy" 0.0)
                  (block-api/get-boolean compound "active" false)
                  (block-api/get-int compound "facing" 0)))))
    
    tile-entity))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.tileentity.TilePhaseGen$Factory
  :methods [^:static [create [] Object]]
  :prefix "tile-factory-")

(defn tile-factory-create []
  (create-phase-gen-tile))