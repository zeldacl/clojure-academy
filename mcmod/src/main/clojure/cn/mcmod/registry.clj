(ns cn.mcmod.registry
  (:require [cn.mcmod.protocols :refer :all]
            [cn.mcmod.logging :as log]))

;; Registry state
(def ^:private registries (atom {}))

;; Global registry instance
(def ^:private global-registry-instance (atom nil))

;; Core registry protocol
(defprotocol IRegistry
  (register-block! [this block-id block] "Register a block")
  (register-item! [this item-id item] "Register an item")
  (register-tile-entity! [this te-id te] "Register a tile entity")
  (register-container! [this container-id container] "Register a container")
  (get-blocks [this] "Get all registered blocks")
  (get-items [this] "Get all registered items") 
  (get-tile-entities [this] "Get all registered tile entities")
  (get-containers [this] "Get all registered containers"))

;; Mod-specific registry functionality
(defprotocol IModRegistry
  (register-mod [this mod-id] "Register a new mod")
  (get-mod-blocks [this mod-id] "Get blocks registered for a specific mod")
  (get-mod-items [this mod-id] "Get items registered for a specific mod")
  (get-mod-tile-entities [this mod-id] "Get tile entities registered for a specific mod"))

;; Base registry record implementing IRegistry
(defrecord Registry [mod-id registry-data]
  IRegistry
  (register-block! [this block-id block]
    (swap! (:blocks registry-data) assoc block-id block)
    block)
  
  (register-item! [this item-id item]
    (swap! (:items registry-data) assoc item-id item)
    item)
  
  (register-tile-entity! [this te-id te]
    (swap! (:tile-entities registry-data) assoc te-id te)
    te)
  
  (register-container! [this container-id container]
    (swap! (:containers registry-data) assoc container-id container)
    container)
  
  (get-blocks [this]
    @(:blocks registry-data))
  
  (get-items [this]
    @(:items registry-data))
  
  (get-tile-entities [this]
    @(:tile-entities registry-data))
  
  (get-containers [this]
    @(:containers registry-data))
    
  IModRegistry
  (register-mod [this mod-id]
    (swap! registry-data update :mods conj mod-id))
  
  (get-mod-blocks [this mod-id]
    (get-in @registry-data [:mod-blocks mod-id]))
  
  (get-mod-items [this mod-id]
    (get-in @registry-data [:mod-items mod-id]))
  
  (get-mod-tile-entities [this mod-id]
    (get-in @registry-data [:mod-tile-entities mod-id])))

;; Create a new registry instance for a mod
(defn create-registry [mod-id]
  (let [registry-data {:blocks (atom {})
                      :items (atom {})
                      :tile-entities (atom {})
                      :containers (atom {})
                      :mods #{}
                      :mod-blocks {}
                      :mod-items {}
                      :mod-tile-entities {}}
        registry (->Registry mod-id registry-data)]
    (swap! registries assoc mod-id registry)
    ;; If this is the first registry, also set it as the global one
    (when (and (= (count @registries) 1) (nil? @global-registry-instance))
      (reset! global-registry-instance registry))
    registry))

;; Get an existing registry
(defn get-registry [mod-id]
  (get @registries mod-id))

;; Get or create the global registry
(defn get-global-registry []
  (if-let [registry @global-registry-instance]
    registry
    (do
      (log/info "Creating global registry")
      (let [registry (create-registry "global")]
        (reset! global-registry-instance registry)
        registry))))

;; Helper for creating common block types
(defn create-basic-block [& {:keys [material hardness resistance light-level]
                            :or {material :stone
                                 hardness 3.0
                                 resistance 5.0
                                 light-level 0}}]
  (reify IBlock
    (get-properties [_]
      {:material material
       :hardness hardness
       :resistance resistance
       :light-level light-level})
    
    (get-material [_] material)
    (get-hardness [_] hardness)
    (get-resistance [_] resistance)
    (get-light-level [_] light-level)
    
    (on-activated [_ pos data] false)
    (on-placed [_ pos data] nil)
    (on-removed [_ pos] nil)))

;; Helper for creating common item types  
(defn create-basic-item [& {:keys [max-stack damage-value creative-tab]
                           :or {max-stack 64
                                damage-value 0
                                creative-tab :misc}}]
  (reify IItem
    (get-properties [_]
      {:max-stack max-stack
       :damage-value damage-value
       :creative-tab creative-tab})
    
    (get-max-stack-size [_] max-stack)
    (get-damage-value [_] damage-value)
    (get-creative-tab [_] creative-tab)
    
    (on-right-click [_ world player] false)
    (on-hit-entity [_ target attacker] false)))