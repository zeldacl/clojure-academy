(ns cn.academy.block.component
  "Generic component system for block entities that can be used across different mod implementations")

;; Protocol definition for all block-related components
(defprotocol IBlockComponent
  (update! [this] 
    "Update the component state (called each tick)")
  
  (get-capability [this type side]
    "Get a capability of specified type from this component"))

;; Protocol for serialization of component data
(defprotocol IComponentSerializer
  (serialize [this component]
    "Convert component state to serializable data")
  
  (deserialize! [this component data]
    "Update component with deserialized data"))

;; Basic implementation of a component that tracks its position
(defrecord BaseComponent [state-atom]
  IBlockComponent
  (update! [_]
    nil) ;; Default no-op implementation
  
  (get-capability [_ _ _]
    nil)) ;; Default returns no capabilities

;; Create a new base component
(defn create-base-component []
  (->BaseComponent (atom {})))

;; Basic serializer that just passes through data
(defrecord BaseSerializer []
  IComponentSerializer
  (serialize [_ component]
    @(:state-atom component))
  
  (deserialize! [_ component data]
    (reset! (:state-atom component) data)))

;; Create a new base serializer
(defn create-base-serializer []
  (->BaseSerializer))