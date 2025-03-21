(ns cn.academy.block.registry
  (:require [mcmod.protocols :refer [IBlockRegistry IItemRegistry IRegistryProvider]]
            [cn.academy.block.serialization :as serial]
            [clojure.tools.logging :as log]))

;; Registry state
(def registry-state
  (atom {:blocks {}
         :items {}
         :tile-entities {}}))

;; Protocol implementations
(defrecord BlockRegistry [state-atom]
  IBlockRegistry
  (register-block! [_ block-id block]
    (swap! state-atom assoc-in [:blocks block-id] block)
    (log/info "Registered block:" block-id)
    block)

  (register-block-entity! [_ block-id entity-supplier]
    (swap! state-atom assoc-in [:tile-entities block-id] entity-supplier)
    (log/info "Registered block entity for:" block-id)
    entity-supplier))

(defrecord ItemRegistry [state-atom]
  IItemRegistry
  (register-item! [_ item-id item]
    (swap! state-atom assoc-in [:items item-id] item)
    (log/info "Registered item:" item-id)
    item))

;; Block registration
(defn create-block-registry []
  (->BlockRegistry registry-state))

(defn register-block! [block-id block]
  (.register-block! (create-block-registry) block-id block))

(defn register-block-entity! [block-id entity-supplier]
  (.register-block-entity! (create-block-registry) block-id entity-supplier))

;; Item registration  
(defn create-item-registry []
  (->ItemRegistry registry-state))

(defn register-item! [item-id item]
  (.register-item! (create-item-registry) item-id item))

;; Registry lookup
(defn get-block [block-id]
  (get-in @registry-state [:blocks block-id]))

(defn get-item [item-id]
  (get-in @registry-state [:items item-id]))

(defn get-tile-entity-supplier [block-id]
  (get-in @registry-state [:tile-entities block-id]))

;; Registry state management
(defn clear-registries! []
  (reset! registry-state {:blocks {}
                         :items {}
                         :tile-entities {}}))