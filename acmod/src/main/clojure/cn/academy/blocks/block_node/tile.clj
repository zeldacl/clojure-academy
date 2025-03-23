(ns cn.academy.blocks.block-node.tile
  (:require [cn.academy.protocols.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [cn.academy.blocks.block-node.config :as config]
            [mcmod.protocols :refer [ITileEntity IInventory IEnergyStorage]]
            [clojure.tools.logging :as log]))

;; Function migrated from cn.academy.block.tileentity.node-tile
(defn- calculate-connection-points 
  "Calculate all possible connection points within the specified range from a position"
  [pos range]
  (let [[x y z] pos
        radius range]
    (for [dx (range (- radius) (inc radius))
          dy (range (- radius) (inc radius))
          dz (range (- radius) (inc radius))
          :let [dist (Math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))]
          :when (<= dist radius)]
      [(+ x dx) (+ y dy) (+ z dz)])))

;; Node utility functions
(defn clamp-energy
  "Ensure energy amount is within valid range"
  [amount min-value max-value]
  (max min-value (min amount max-value)))

(defn charge-node
  "Add energy to a node with bandwidth limit check"
  [state-atom max-energy-fn bandwidth-fn amount ignore-bandwidth?]
  (let [state @state-atom
        energy (:energy state)
        max-energy (max-energy-fn state)
        space (- max-energy energy)
        bandwidth (if ignore-bandwidth? Double/MAX_VALUE (bandwidth-fn state))
        charge-amount (min amount space bandwidth)]
    (when (pos? charge-amount)
      (swap! state-atom update :energy + charge-amount))
    charge-amount))

(defn discharge-node
  "Remove energy from a node with bandwidth limit check"
  [state-atom bandwidth-fn amount ignore-bandwidth?]
  (let [state @state-atom
        energy (:energy state)
        bandwidth (if ignore-bandwidth? Double/MAX_VALUE (bandwidth-fn state))
        discharge-amount (min amount energy bandwidth)]
    (when (pos? discharge-amount)
      (swap! state-atom update :energy - discharge-amount))
    discharge-amount))

;; Node state record
(defrecord NodeState [energy active placer-id password-hash node-type node-name connections]
  Object
  (get-node-type [_] node-type)
  (get-energy [_] energy)
  (get-max-energy [this]
    (config/get-node-property node-type :max-energy))
  (get-bandwidth [this]
    (config/get-node-property node-type :bandwidth))
  (get-range [this]
    (config/get-node-property node-type :range))
  (get-capacity [this]
    (config/get-node-property node-type :max-connections)))

(defn create-node-state 
  "Create a new node state with default values"
  [node-type]
  (map->NodeState
    {:energy 0.0
     :active false
     :placer-id nil
     :password-hash ""
     :node-type node-type
     :node-name (str "Node " (name node-type))
     :connections #{}}))

(defn tick-node 
  "Handle node state updates during tick"
  [state]
  (if (:active state)
    ;; When node is active, distribute energy on some ticks
    (if (and (> (:energy state) 0)
             (pos? (count (:connections state)))
             (zero? (mod (System/currentTimeMillis) 40)))
      (assoc state :pending-distribute true)
      state)
    state))

;; Node tile entity protocol
(defprotocol INodeTile
  (get-energy [this])
  (set-energy [this amount])
  (charge [this amount ignore-bandwidth?])
  (discharge [this amount ignore-bandwidth?])
  (can-charge? [this])
  (can-discharge? [this])
  (node? [this])
  (get-connections [this])
  (get-position [this])
  (can-connect? [this other])
  (connect [this other])
  (disconnect [this other])
  (distribute-energy [this]))

;; Default INodeTile implementation
(defn node?
  "Check if a tile entity is a node"
  [tile]
  (instance? INodeTile tile))

;; Tile entity implementation
(defrecord TileNode [state-atom inventory]
  ITileEntity
  (tick [this]
    (let [prev-state @state-atom
          updated-state (swap! state-atom tick-node)]
      
      ;; If pending distribution, handle energy distribution
      (when (:pending-distribute updated-state)
        (distribute-energy this)
        (swap! state-atom dissoc :pending-distribute))
        
      ;; Mark dirty to save state changes when needed
      (when (zero? (mod (block-api/get-world-time) 20))
        (block-api/mark-dirty! this))))
  
  INodeTile
  (get-energy [_] (:energy @state-atom))
  
  (set-energy [_ amount]
    (swap! state-atom assoc :energy 
           (clamp-energy amount 0 (config/get-node-property (:node-type @state-atom) :max-energy))))
  
  (charge [this amount ignore-bandwidth?]
    (charge-node state-atom
                #(config/get-node-property (:node-type %) :max-energy) 
                #(config/get-node-property (:node-type %) :bandwidth)
                amount 
                ignore-bandwidth?))
  
  (discharge [this amount ignore-bandwidth?]
    (discharge-node state-atom
                   #(config/get-node-property (:node-type @state-atom) :bandwidth)
                   amount
                   ignore-bandwidth?))
  
  (can-charge? [this]
    (< (get-energy this) (config/get-node-property (:node-type @state-atom) :max-energy)))
  
  (can-discharge? [this]
    (> (get-energy this) 0))
  
  (node? [_] true)
  
  (get-connections [_]
    (:connections @state-atom))
    
  (get-position [this]
    (block-api/get-pos this))
    
  (can-connect? [this other]
    (and (instance? TileNode other)
         (< (count (:connections @state-atom)) 
            (config/get-node-property (:node-type @state-atom) :max-connections))))
    
  (connect [this other]
    (when (can-connect? this other)
      (swap! state-atom update :connections conj (get-position other))
      true))
    
  (disconnect [this other]
    (swap! state-atom update :connections disj (get-position other))
    true)
    
  (distribute-energy [this]
    (let [connected-nodes (->> (get-connections this)
                             (map #(get-tile-entity (block-api/get-world this) %))
                             (filter identity))
          total-nodes (inc (count connected-nodes))
          energy-per-node (int (/ (get-energy this) total-nodes))]
      (doseq [node connected-nodes]
        (let [transferred (extract-energy this energy-per-node true)]
          (when (> transferred 0)
            (let [accepted (receive-energy node transferred true)]
              (when (> accepted 0)
                (extract-energy this accepted false)
                (receive-energy node accepted false))))))))
  
  ;; Standard IEnergyStorage protocol
  IEnergyStorage
  (get-energy-stored [this]
    (get-energy this))
  
  (get-max-energy-stored [this]
    (config/get-node-property (:node-type @state-atom) :max-energy))
  
  (receive-energy [this amount simulate]
    (if simulate
      (let [current (get-energy this)
            max-capacity (get-max-energy-stored this)
            bandwidth (config/get-node-property (:node-type @state-atom) :bandwidth)
            space (- max-capacity current)
            accept-amount (min amount space bandwidth)]
        (max 0 accept-amount))
      (charge this amount false)))
  
  (extract-energy [this amount simulate]
    (if simulate
      (let [current (get-energy this)
            bandwidth (config/get-node-property (:node-type @state-atom) :bandwidth)
            available (min current bandwidth)]
        (min amount available))
      (discharge this amount false)))
  
  (can-receive? [this]
    (can-charge? this))
  
  (can-extract? [this]
    (can-discharge? this)))

;; Helper functions for property methods
(defn- add-property-methods! [tile-entity state-atom]
  (doseq [[method-name getter]
          [["MaxEnergy" #(config/get-node-property (:node-type @state-atom) :max-energy)]
           ["Bandwidth" #(config/get-node-property (:node-type @state-atom) :bandwidth)]
           ["Range" #(config/get-node-property (:node-type @state-atom) :range)]
           ["Capacity" #(config/get-node-property (:node-type @state-atom) :max-connections)]]]
    (block-api/add-method! tile-entity (str "get" method-name) getter)))

(defn- add-state-methods! [tile-entity state-atom]
  (doseq [[property key-name]
          [["PlacerId" :placer-id]
           ["NodeName" :node-name]
           ["PasswordHash" :password-hash]
           ["Energy" :energy]
           ["Active" :active]]]
    (block-api/add-method! tile-entity (str "get" property) 
                         #(get @state-atom key-name))
    (block-api/add-method! tile-entity (str "set" property) 
                         #(swap! state-atom assoc key-name %))))

(defn- add-nbt-handlers! [tile-entity state-atom]
  ;; Save NBT data
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
          (block-api/put-string! "nodeType" (name (:node-type state)))
          (block-api/put-list! "connections" (:connections state) block-api/put-pos!)))))
  
  ;; Load NBT data
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
                 :node-type (keyword (block-api/get-string compound "nodeType" "basic"))
                 :connections (set (block-api/get-pos-list compound "connections" #{}))})))))

;; Main tile entity creation function
(defn create-node-tile [node-type]
  (let [factory @block-api/*forge-factory*
        tile-entity (block-api/create-tile-entity factory)
        state-atom (atom (create-node-state node-type))]
    
    ;; Add capability provider for energy
    (block-api/add-capability-provider! 
      tile-entity 
      (energy-api/create-energy-storage 
        (config/get-node-property node-type :max-energy)
        #(:energy @state-atom)
        #(swap! state-atom assoc :energy %)
        #(config/get-node-property node-type :bandwidth)))
    
    ;; Add tick handler
    (block-api/on-tile-entity-tick! 
      tile-entity 
      (fn []
        (swap! state-atom tick-node)
        (when (zero? (mod (block-api/get-world-time) 20))
          (block-api/mark-dirty! tile-entity))))
    
    ;; Add NBT handlers
    (add-nbt-handlers! tile-entity state-atom)
    
    ;; Add property getters/setters
    (add-property-methods! tile-entity state-atom)
    (add-state-methods! tile-entity state-atom)
    
    ;; Return the configured tile entity
    tile-entity))

;; Factory methods for Java interop
(defn tile-factory-createBasic [] (create-node-tile :basic))
(defn tile-factory-createStandard [] (create-node-tile :standard))
(defn tile-factory-createAdvanced [] (create-node-tile :advanced))

;; Generate Java classes
(gen-class
  :name cn.academy.blocks.block-node.TileNode$Factory
  :methods [^:static [createBasic [] Object]
            ^:static [createStandard [] Object]
            ^:static [createAdvanced [] Object]]
  :prefix "tile-factory-")

;; Utility methods for external use
(defn get-inventory [tile]
  (.getInventory tile))

(defn set-node-energy! [tile energy]
  (.setEnergy tile energy))

(defn set-node-enabled! [tile enabled]
  (.setActive tile enabled))

(defn set-node-config! [tile name password]
  (.setNodeName tile name)
  (.setPasswordHash tile password))