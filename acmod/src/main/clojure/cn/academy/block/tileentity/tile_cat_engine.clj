(ns cn.academy.block.tileentity.tile-cat-engine
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [clojure.tools.logging :as log]
            [cn.academy.energy.api.wireless-helper :as wireless]
            [cn.academy.energy.api.tile-entity :as energy-tile]))

;; Cat Engine TileEntity constants
(def ^:const ENERGY_MAX 100000.0)
(def ^:const ENERGY_GEN_PER_TICK 5.0)

;; Cat Engine TileEntity state
(defrecord CatEngineState [energy active facing])

(defn create-cat-engine-state []
  (->CatEngineState 0.0 false 0))

(defn tick-engine [state]
  (if (:active state)
    (update state :energy #(min (+ % ENERGY_GEN_PER_TICK) ENERGY_MAX))
    state))

;; TileEntity implementation for Forge
(defn create-cat-engine-tile []
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-cat-engine-state))]
    
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
        (swap! state-atom tick-engine)
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
                (->CatEngineState
                  (block-api/get-double compound "energy" 0.0)
                  (block-api/get-boolean compound "active" false)
                  (block-api/get-int compound "facing" 0)))))
    
    tile-entity))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.tileentity.TileCatEngine$Factory
  :methods [^:static [create [] Object]]
  :prefix "tile-factory-")

(defn tile-factory-create []
  (create-cat-engine-tile))

(defprotocol ICatEngine
  (tick [this])
  (get-energy-stored [this])
  (add-energy [this amount])
  (get-max-energy [this])
  (can-receive-energy? [this])
  (get-node [this]))

(defrecord TileCatEngine [properties]
  ICatEngine
  (tick [_]
    (when (wireless/has-node? properties)
      (let [gen-amount (+ 3 (rand-int 2))]
        (wireless/receive-energy (:node properties) gen-amount))))
  
  (get-energy-stored [_]
    0)  ; Cat engine doesn't store energy, transfers directly
  
  (add-energy [_ _]
    0)  ; Cannot receive energy
  
  (get-max-energy [_]
    0)  ; No energy storage
  
  (can-receive-energy? [_]
    false)  ; Only generates, doesn't receive
  
  (get-node [_]
    (:node properties))

  energy-tile/IEnergyTile
  (energy-stored [this]
    (get-energy-stored this))
  
  (max-energy [this]
    (get-max-energy this))
  
  (receive-energy [this amount]
    (add-energy this amount)))

(defn create []
  (->TileCatEngine {:node nil}))