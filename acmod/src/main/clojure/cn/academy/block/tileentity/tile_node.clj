(ns cn.academy.block.tileentity.tile-node
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [cn.academy.core.node-types :as node-types]
            [mcmod.protocols :refer [ITileEntity]]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.inventory IInventory]
           [net.minecraft.item ItemStack]))

;; Now implementing shared IWirelessNode protocol from node-types
;; Node state management
(defrecord NodeState [energy active placer-id password-hash node-type node-name]
  node-types/IWirelessNode
  (get-node-type [_] node-type)
  (get-energy [_] energy)
  (get-max-energy [this]
    (node-types/get-node-max-energy (get-node-type this)))
  (get-bandwidth [this]
    (node-types/get-node-bandwidth (get-node-type this)))
  (get-range [this]
    (node-types/get-node-range (get-node-type this)))
  (get-capacity [this]
    (node-types/get-node-capacity (get-node-type this)))
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
  ; Enhanced tick logic to handle enabled state and energy transfer
  (if (:active state)
    (do
      ; Process any energy transfer logic here
      state)
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
           (node-types/clamp-energy amount 0 (node-types/get-node-max-energy (:node-type @state-atom)))))
  
  (charge [this amount ignore-bandwidth?]
    (node-types/charge-node state-atom
                         #(node-types/get-node-max-energy (:node-type %)) 
                         #(node-types/get-node-bandwidth (:node-type %))
                         amount 
                         ignore-bandwidth?))
  
  (discharge [this amount ignore-bandwidth?]
    (node-types/discharge-node state-atom
                           #(node-types/get-node-bandwidth (:node-type %))
                           amount
                           ignore-bandwidth?))
  
  (can-charge? [this]
    (< (get-energy this) (node-types/get-node-max-energy (:node-type @state-atom))))
  
  (can-discharge? [this]
    (> (get-energy this) 0)))

;; TileEntity implementation for Forge
(defn create-node-tile [node-type]
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-node-state node-type))]
    
    ;; Register capability provider for energy
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        (node-types/get-node-max-energy node-type)
        (fn [] (:energy @state-atom))
        (fn [amount] (swap! state-atom assoc :energy amount))
        (fn [] (node-types/get-node-bandwidth node-type))))
    
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
            (block-api/put-string! "placerId" (or (:placer-id state) ""))
            (block-api/put-string! "password" (or (:password-hash state) ""))
            (block-api/put-string! "nodeName" (or (:node-name state) ""))
            (block-api/put-string! "nodeType" (name (:node-type state)))))))
    
    ;; Implement NBT deserialization
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
    
    ;; Add getter/setter methods for Java interop
    (block-api/add-method! tile-entity "getMaxEnergy" 
                          (fn [] (node-types/get-node-max-energy (:node-type @state-atom))))
    (block-api/add-method! tile-entity "getBandwidth" 
                          (fn [] (node-types/get-node-bandwidth (:node-type @state-atom))))
    (block-api/add-method! tile-entity "getRange" 
                          (fn [] (node-types/get-node-range (:node-type @state-atom))))
    (block-api/add-method! tile-entity "getCapacity" 
                          (fn [] (node-types/get-node-capacity (:node-type @state-atom))))
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