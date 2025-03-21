(ns cn.academy.block.tile-entity
  (:require [mcmod.protocols :refer [IBlockEntity ITickable INBTSerializable ICapabilityProvider]]
            [cn.academy.block.error :as error]
            [cn.academy.block.capability :as cap]
            [clojure.tools.logging :as log]))

;; Tile entity state tracking
(def te-state
  (atom {:active-entities {}}))

;; Base tile entity implementation
(defrecord BlockEntity [block capabilities state-atom]
  IBlockEntity
  (get-block-pos [_]
    (get-in block [:state :pos]))
  
  (get-block-type [_]
    (:type block))
  
  (remove [this]
    (when-let [pos (.get-block-pos this)]
      (swap! te-state update :active-entities dissoc pos)))
  
  ITickable
  (tick [_]
    (when-let [machine (get-in block [:state :machine])]
      (.update machine)))
  
  INBTSerializable
  (write-nbt [_]
    (when-let [state @(:state block)]
      (mcmod.nbt/write-map state)))
  
  (read-nbt [_ nbt]
    (when-let [data (mcmod.nbt/read-map nbt)]
      (reset! (:state block) data)))
  
  ICapabilityProvider
  (get-capability [_ cap side]
    (when-let [provider (get capabilities cap)]
      (.get-capability provider side)))
  
  (has-capability [_ cap side]
    (when-let [provider (get capabilities cap)]
      (.has-capability? provider side)))
  
  (invalidate-capabilities [_]
    (doseq [provider (vals capabilities)]
      (.invalidate-capabilities provider))))

;; Machine tile entity implementation
(defrecord MachineEntity [block capabilities state-atom]
  IBlockEntity
  (get-block-pos [_]
    (get-in block [:state :pos]))
  
  (get-block-type [_]
    (:type block))
  
  (remove [this]
    (when-let [pos (.get-block-pos this)]
      (swap! te-state update :active-entities dissoc pos)))
  
  ITickable
  (tick [_]
    (when-let [machine (get-in block [:state :machine])]
      (.update machine))
    (when-let [processor (get-in block [:state :processor])]
      (.update processor)))
  
  INBTSerializable
  (write-nbt [_]
    (when-let [state @(:state block)]
      (mcmod.nbt/write-map state)))
  
  (read-nbt [_ nbt]
    (when-let [data (mcmod.nbt/read-map nbt)]
      (reset! (:state block) data)))
  
  ICapabilityProvider
  (get-capability [_ cap side]
    (when-let [provider (get capabilities cap)]
      (.get-capability provider side)))
  
  (has-capability [_ cap side]
    (when-let [provider (get capabilities cap)]
      (.has-capability? provider side)))
  
  (invalidate-capabilities [_]
    (doseq [provider (vals capabilities)]
      (.invalidate-capabilities provider))))

;; Factory functions
(defn create-block-entity [block]
  (let [entity (->BlockEntity block 
                             {cap/ENERGY (cap/create-capability-provider block)
                              cap/FLUID (cap/create-capability-provider block)}
                             (atom {}))]
    (swap! te-state assoc-in 
           [:active-entities (.get-block-pos entity)]
           entity)
    entity))

(defn create-machine-entity [block]
  (let [entity (->MachineEntity block
                               {cap/ENERGY (cap/create-capability-provider block)
                                cap/FLUID (cap/create-capability-provider block)
                                cap/INVENTORY (cap/create-capability-provider block)}
                               (atom {}))]
    (swap! te-state assoc-in
           [:active-entities (.get-block-pos entity)]
           entity)
    entity))

;; Entity management
(defn get-entity [pos]
  (get-in @te-state [:active-entities pos]))

(defn remove-entity! [pos]
  (when-let [entity (get-entity pos)]
    (.remove entity)))

;; Entity updating
(defn tick-entities! []
  (doseq [entity (vals (:active-entities @te-state))]
    (error/with-safe-execution 
      (str "tick_" (.get-block-type entity)) :tile-entity
      (.tick entity))))

;; Initialize tile entity system
(defn init-tile-entities! []
  (reset! te-state {:active-entities {}}))