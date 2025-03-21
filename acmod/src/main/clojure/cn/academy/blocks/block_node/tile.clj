(ns cn.academy.blocks.block-node.tile
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [cn.academy.blocks.block-node.config :as config]
            [mcmod.protocols :refer [ITileEntity IInventory]]
            [clojure.tools.logging :as log]))

(defrecord NodeState [energy active placer-id password-hash node-type node-name]
  node-types/IWirelessNode
  (get-node-type [_] node-type)
  (get-energy [_] energy)
  (get-max-energy [this]
    (config/get-node-property node-type :max-energy))
  (get-bandwidth [this]
    (config/get-node-property node-type :bandwidth))
  (get-range [this]
    (config/get-node-property node-type :range))
  (get-capacity [this]
    (config/get-node-property node-type :max-connections))
  (set-placer [this player]
    (assoc this :placer-id (.getUniqueID player))))

(defn create-node-state [node-type]
  (map->NodeState
    {:energy 0.0
     :active false
     :placer-id nil
     :password-hash ""
     :node-type node-type
     :node-name ""}))

(defn tick-node [state]
  (if (:active state)
    state
    state))

(defprotocol INodeTile
  (get-energy [this])
  (set-energy [this amount])
  (charge [this amount ignore-bandwidth?])
  (discharge [this amount ignore-bandwidth?])
  (can-charge? [this])
  (can-discharge? [this]))

(defrecord TileNode [state-atom inventory]
  ITileEntity
  (tick [this]
    (swap! state-atom tick-node)
    (when (zero? (mod (block-api/get-world-time) 20))
      (block-api/mark-dirty! this)))
  
  INodeTile
  (get-energy [_] (:energy @state-atom))
  
  (set-energy [_ amount]
    (swap! state-atom assoc :energy 
           (node-types/clamp-energy amount 0 (config/get-node-property (:node-type @state-atom) :max-energy))))
  
  (charge [this amount ignore-bandwidth?]
    (node-types/charge-node state-atom
                         #(config/get-node-property (:node-type %) :max-energy) 
                         #(config/get-node-property (:node-type %) :bandwidth)
                         amount 
                         ignore-bandwidth?))
  
  (discharge [this amount ignore-bandwidth?]
    (node-types/discharge-node state-atom
                           #(config/get-node-property (:node-type %) :bandwidth)
                           amount
                           ignore-bandwidth?))
  
  (can-charge? [this]
    (< (get-energy this) (config/get-node-property (:node-type @state-atom) :max-energy)))
  
  (can-discharge? [this]
    (> (get-energy this) 0)))

(defn create-node-tile [node-type]
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-node-state node-type))]
    
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        (config/get-node-property node-type :max-energy)
        (fn [] (:energy @state-atom))
        (fn [amount] (swap! state-atom assoc :energy amount))
        (fn [] (config/get-node-property node-type :bandwidth))))
    
    (block-api/on-tile-entity-tick! 
      tile-entity 
      (fn []
        (swap! state-atom tick-node)
        (when (zero? (mod (block-api/get-world-time) 20))
          (block-api/mark-dirty! tile-entity))))
    
    (block-api/on-save-nbt! 
      tile-entity 
      (fn [compound]
        (let [state @state-atom]
          (doto compound
            (block-api/put-double! "energy" (:energy state))
            (block-api/put-boolean! "active" (:active state))
            (block-api/put-string! "placerId" (or (:placer-id state) ""))
            (block-api/put-string! "password" (or (:password-hash state) ""))
            (block-api/put-string! "nodeName" (or (:node-name state) ""))
            (block-api/put-string! "nodeType" (name (:node-type state)))))))
    
    (block-api/on-load-nbt! 
      tile-entity 
      (fn [compound]
        (reset! state-atom
                (map->NodeState
                  {:energy (block-api/get-double compound "energy" 0.0)
                   :active (block-api/get-boolean compound "active" false)
                   :placer-id (block-api/get-string compound "placerId" "")
                   :password-hash (block-api/get-string compound "password" "")
                   :node-name (block-api/get-string compound "nodeName" "")
                   :node-type (keyword (block-api/get-string compound "nodeType" "basic"))}))))
    
    (block-api/add-method! tile-entity "getMaxEnergy" 
                          (fn [] (config/get-node-property (:node-type @state-atom) :max-energy)))
    (block-api/add-method! tile-entity "getBandwidth" 
                          (fn [] (config/get-node-property (:node-type @state-atom) :bandwidth)))
    (block-api/add-method! tile-entity "getRange" 
                          (fn [] (config/get-node-property (:node-type @state-atom) :range)))
    (block-api/add-method! tile-entity "getCapacity" 
                          (fn [] (config/get-node-property (:node-type @state-atom) :max-connections)))
    (block-api/add-method! tile-entity "getPlacerId" 
                          (fn [] (:placer-id @state-atom)))
    (block-api/add-method! tile-entity "setPlacerId" 
                          (fn [id] (swap! state-atom assoc :placer-id id)))
    (block-api/add-method! tile-entity "getNodeName" 
                          (fn [] (:node-name @state-atom)))
    (block-api/add-method! tile-entity "setNodeName" 
                          (fn [name] (swap! state-atom assoc :node-name name)))
    (block-api/add-method! tile-entity "getPasswordHash" 
                          (fn [] (:password-hash @state-atom)))
    (block-api/add-method! tile-entity "setPasswordHash" 
                          (fn [pwd] (swap! state-atom assoc :password-hash pwd)))
    (block-api/add-method! tile-entity "getEnergy" 
                          (fn [] (:energy @state-atom)))
    (block-api/add-method! tile-entity "setEnergy" 
                          (fn [amount] (swap! state-atom assoc :energy amount)))
    (block-api/add-method! tile-entity "isActive" 
                          (fn [] (:active @state-atom)))
    (block-api/add-method! tile-entity "setActive" 
                          (fn [active] (swap! state-atom assoc :active active)))
    
    tile-entity))

(gen-class
  :name cn.academy.blocks.block-node.TileNode$Factory
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