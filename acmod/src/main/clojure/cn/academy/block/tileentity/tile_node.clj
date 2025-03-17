(ns cn.academy.block.tileentity.tile-node
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]))

(defprotocol IWirelessNode
  (get-max-energy [this])
  (get-bandwidth [this])
  (get-range [this])
  (get-capacity [this])
  (set-placer [this player]))

;; Node types with their attributes
(def node-types
  {:basic {:max-energy 15000
           :bandwidth 150
           :range 9
           :capacity 5}
   :standard {:max-energy 50000
              :bandwidth 300
              :range 12
              :capacity 10}
   :advanced {:max-energy 200000
              :bandwidth 900
              :range 19
              :capacity 20}})

;; Node state management
(defrecord NodeState [energy active placer-id password-hash properties]
  IWirelessNode
  (get-max-energy [_] (:max-energy properties))
  (get-bandwidth [_] (:bandwidth properties))
  (get-range [_] (:range properties))
  (get-capacity [_] (:capacity properties))
  (set-placer [this player]
    (assoc this :placer-id (.getUniqueID player))))

(defn create-node-state [properties]
  (map->NodeState
    {:energy 0.0
     :active false
     :placer-id nil
     :password-hash ""
     :properties properties}))

(defn- get-node-attr [state attr]
  (get-in node-types [(:node-type state) attr]))

(defn tick-node [state]
  state) ; No automatic energy generation for nodes

(defprotocol INodeTile
  (get-energy [this])
  (set-energy [this amount])
  (charge [this amount ignore-bandwidth?])
  (discharge [this amount ignore-bandwidth?])
  (can-charge? [this])
  (can-discharge? [this]))

(defrecord TileNode [state-atom inventory]
  INodeTile
  (get-energy [_] (:energy @state-atom))
  
  (set-energy [_ amount]
    (swap! state-atom assoc :energy (max 0 (min amount (get-max-energy @state-atom)))))
  
  (charge [this amount ignore-bandwidth?]
    (let [state @state-atom
          bandwidth (if ignore-bandwidth? amount (get-bandwidth state))
          max-charge (min amount bandwidth)
          current (get-energy this)
          max-energy (get-max-energy state)
          actual-charge (min max-charge (- max-energy current))]
      (set-energy this (+ current actual-charge))
      actual-charge))
  
  (discharge [this amount ignore-bandwidth?]
    (let [state @state-atom
          bandwidth (if ignore-bandwidth? amount (get-bandwidth state))
          max-discharge (min amount bandwidth)
          current (get-energy this)
          actual-discharge (min max-discharge current)]
      (set-energy this (- current actual-discharge))
      actual-discharge))
  
  (can-charge? [this]
    (< (get-energy this) (get-max-energy @state-atom)))
  
  (can-discharge? [this]
    (> (get-energy this) 0)))

(defn create-tile-node [tile-entity properties]
  (let [state-atom (atom (create-node-state properties))
        inventory (block-api/create-inventory 2)] ; 2 slots for energy item input/output
    
    ;; Set up energy capability
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        (get-max-energy @state-atom)
        #(get-energy tile-entity)
        #(set-energy tile-entity %)
        #(get-bandwidth @state-atom)))
    
    ;; Set up inventory
    (block-api/add-inventory! tile-entity inventory)
    
    ;; Create tile instance
    (->TileNode state-atom inventory)))

;; TileEntity implementation for Forge
(defn create-node-tile [node-type]
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-node-state (get node-types node-type)))]
    
    ;; Register capability provider for energy
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        (get-node-attr @state-atom :max-energy)
        (fn [] (:energy @state-atom))
        (fn [amount] (swap! state-atom assoc :energy amount))
        (fn [] (get-node-attr @state-atom :bandwidth))))
    
    ;; Register tick method
    (block-api/on-tile-entity-tick! 
      tile-entity 
      (fn []
        (swap! state-atom tick-node)
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
            (block-api/put-int! "facing" (:facing state))
            (block-api/put-string! "placerName" (:placer-name state))
            (block-api/put-string! "password" (:password state))
            (block-api/put-string! "nodeName" (:node-name state))
            (block-api/put-string! "nodeType" (name (:node-type state)))))))
    
    ;; Implement NBT deserialization
    (block-api/on-load-nbt! 
      tile-entity 
      (fn [compound]
        (reset! state-atom
                (->NodeState
                  (block-api/get-double compound "energy" 0.0)
                  (block-api/get-boolean compound "active" false)
                  (block-api/get-int compound "facing" 0)
                  (block-api/get-string compound "placerName" "")
                  (block-api/get-string compound "password" "")
                  (block-api/get-string compound "nodeName" "")
                  (keyword (block-api/get-string compound "nodeType" "basic"))))))
    
    ;; Add getter/setter methods
    (block-api/add-method! tile-entity "getMaxEnergy" (fn [] (get-node-attr @state-atom :max-energy)))
    (block-api/add-method! tile-entity "getBandwidth" (fn [] (get-node-attr @state-atom :bandwidth)))
    (block-api/add-method! tile-entity "getRange" (fn [] (get-node-attr @state-atom :range)))
    (block-api/add-method! tile-entity "getCapacity" (fn [] (get-node-attr @state-atom :capacity)))
    (block-api/add-method! tile-entity "getPlacerName" (fn [] (:placer-name @state-atom)))
    (block-api/add-method! tile-entity "setPlacerName" (fn [name] (swap! state-atom assoc :placer-name name)))
    (block-api/add-method! tile-entity "getNodeName" (fn [] (:node-name @state-atom)))
    (block-api/add-method! tile-entity "setNodeName" (fn [name] (swap! state-atom assoc :node-name name)))
    (block-api/add-method! tile-entity "getPassword" (fn [] (:password @state-atom)))
    (block-api/add-method! tile-entity "setPassword" (fn [pwd] (swap! state-atom assoc :password pwd)))
    
    tile-entity))

;; Export the constructor functions for Java interop
(gen-class
  :name cn.academy.block.tileentity.TileNode$Factory
  :methods [^:static [createBasic [] Object]
            ^:static [createStandard [] Object]
            ^:static [createAdvanced [] Object]]
  :prefix "tile-factory-")

(defn tile-factory-createBasic []
  (create-node-tile :basic))

(defn tile-factory-createStandard []
  (create-node-tile :standard))

(defn tile-factory-createAdvanced []
  (create-node-tile :advanced))