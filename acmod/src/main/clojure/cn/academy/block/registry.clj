(ns cn.academy.block.registry
  (:require [cn.academy.block.component :as component])
  (:import [net.minecraft.block.material Material]))

;; Registry state
(def ^:private registry-state 
  (atom {:blocks {}
         :tile-entities {}
         :containers {}}))

;; Implementation tracking 
(def ^:private registry-impl (atom nil))

(defprotocol IBlockRegistryImpl
  "Interface for Minecraft version-specific registry implementations"
  (create-block [this id properties])
  (create-item-block [this block])
  (create-tile-entity [this id properties])
  (register-block! [this id block])
  (register-tile-entity! [this id te-type factory])
  (register-container! [this id factory])
  (open-gui [this player world pos]))

;; Registry functions that use current implementation
(defn set-registry-impl! [impl]
  (reset! registry-impl impl))

(defn create-block [id properties]
  (when-let [impl @registry-impl]
    (create-block impl id properties)))

(defn create-item-block [block]
  (when-let [impl @registry-impl]
    (create-item-block impl block)))

(defn create-tile-entity [id properties]
  (when-let [impl @registry-impl]
    (create-tile-entity impl id properties)))

(defn register-block! [id block]
  (when-let [impl @registry-impl]
    (swap! registry-state assoc-in [:blocks id] block)
    (register-block! impl id block)))

(defn register-tile-entity! [id te-type factory]
  (when-let [impl @registry-impl]
    (swap! registry-state assoc-in [:tile-entities id] factory)
    (register-tile-entity! impl id te-type factory)))

(defn register-container! [id factory]
  (when-let [impl @registry-impl]
    (swap! registry-state assoc-in [:containers id] factory)
    (register-container! impl id factory)))

(defn open-gui [player world pos]
  (when-let [impl @registry-impl]
    (open-gui impl player world pos)))

;; Helper functions
(defn get-block [id]
  (get-in @registry-state [:blocks id]))

(defn get-tile-entity-factory [id]
  (get-in @registry-state [:tile-entities id]))

(defn get-container-factory [id]
  (get-in @registry-state [:containers id]))

;; Event registration
(def event-handlers (atom {}))

(defn register-event-handler! [event-type handler]
  (swap! event-handlers assoc event-type handler))

(defn handle-event! [event-type & args]
  (when-let [handler (get @event-handlers event-type)]
    (apply handler args)))