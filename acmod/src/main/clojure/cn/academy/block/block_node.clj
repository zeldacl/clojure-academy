(ns cn.academy.block.block-node
  (:require [mcmod.protocols :refer :all]
            [mcmod.block.block-state :as block-state]
            [cn.academy.block.tileentity.node-tile :as node-tile]))

;; Node type definitions with all properties
(def node-types
  {:basic {:name "basic"
           :max-energy 15000
           :bandwidth 150
           :range 9
           :capacity 5
           :render-type :model}
   :standard {:name "standard"
              :max-energy 50000
              :bandwidth 300
              :range 12
              :capacity 10
              :render-type :model}
   :advanced {:name "advanced"
              :max-energy 200000
              :bandwidth 900
              :range 19
              :capacity 20
              :render-type :model}})

;; Block state properties
(def state-properties
  {"connected" (block-state/create-bool-property "connected" false)
   "energy" (block-state/create-int-property "energy" 0 4 0)})

(defprotocol INodeBlock
  (get-node-type [this])
  (get-max-energy [this])
  (get-bandwidth [this])
  (get-range [this])
  (get-capacity [this])
  (get-actual-state [this world pos state]))

(defrecord BlockNode [node-type state]
  IBlock
  (get-properties [_]
    {:material :rock
     :hardness 2.5
     :resistance 3.0
     :light-level 0
     :harvest-level ["pickaxe" 1]
     :has-tile-entity true
     :creative-tab :academy
     :render-type (get-in node-types [node-type :render-type])})

  (get-material [_] :rock)
  (get-hardness [_] 2.5)
  (get-resistance [_] 3.0)
  (get-light-level [_] 0)
  (get-harvest-level [_] ["pickaxe" 1])
  
  (get-state-properties [_] state-properties)
  (get-default-state [_] state)

  (on-activated [_ pos data]
    (let [{:keys [world player]} data]
      (when-let [tile (.getTileEntity world pos)]
        (when (instance? cn.academy.block.tileentity.TileNode tile)
          true)))) ; Return true to open GUI

  (on-placed [_ pos data]
    (let [{:keys [world player]} data]
      (when-let [tile (.getTileEntity world pos)]
        (when (instance? cn.academy.block.tileentity.TileNode tile)
          (.setPlacer tile player)))))

  (on-removed [_ pos] nil)

  INodeBlock
  (get-node-type [_] 
    node-type)

  (get-max-energy [this]
    (get-in node-types [(get-node-type this) :max-energy]))

  (get-bandwidth [this]
    (get-in node-types [(get-node-type this) :bandwidth]))

  (get-range [this]
    (get-in node-types [(get-node-type this) :range]))

  (get-capacity [this]
    (get-in node-types [(get-node-type this) :capacity]))

  (get-actual-state [_ world pos state]
    (if-let [tile (.getTileEntity world pos)]
      (if (instance? cn.academy.block.tileentity.TileNode tile)
        (let [enabled? (.isEnabled tile)
              energy-pct (int (min 4 (Math/round (* 4 (/ (.getEnergy tile)
                                                        (.getMaxEnergy tile))))))]
          (-> state
              (block-state/with-property "connected" enabled?)
              (block-state/with-property "energy" energy-pct)))
        state)
      state)))

;; Factory functions
(defn create-basic-node []
  (->BlockNode :basic (block-state/create-block-state state-properties)))

(defn create-standard-node []
  (->BlockNode :standard (block-state/create-block-state state-properties)))

(defn create-advanced-node []
  (->BlockNode :advanced (block-state/create-block-state state-properties)))

;; Block registration helpers
(defn register-node! [registry block-id node]
  (register-block! registry block-id node)
  (register-tile-entity! registry block-id #(node-tile/create-node-tile (get-node-type node))))

;; Export constructors for Java interop
(gen-class
  :name cn.academy.block.BlockNode
  :methods [^:static [createBasic [] Object]
            ^:static [createStandard [] Object]
            ^:static [createAdvanced [] Object]]
  :prefix "block-")

(defn block-createBasic []
  (create-basic-node))

(defn block-createStandard []
  (create-standard-node))

(defn block-createAdvanced []
  (create-advanced-node))