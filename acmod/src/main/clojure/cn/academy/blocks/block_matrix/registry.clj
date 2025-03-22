(ns cn.academy.blocks.block-matrix.registry
  "Registry system for the wireless matrix block and components.
   Provides a unified registration interface for all block components."
  (:require [cn.academy.blocks.block-matrix.config :as config]
            [cn.academy.blocks.block-matrix.state :as state]
            [cn.academy.blocks.block-matrix.network :as network]
            [cn.academy.blocks.block-matrix.energy :as energy]
            [cn.academy.blocks.block-matrix.gui :as gui]
            [cn.academy.blocks.block-matrix.render :as render]
            [cn.academy.blocks.block-matrix.events :as events]
            [clojure.tools.logging :as log]))

;; Block definition with properties
(def matrix-block-def
  {:id "wireless_matrix"
   :properties {:material :iron
               :hardness 3.0
               :resistance 15.0
               :light-level 0
               :has-tile-entity true}})

;; Matrix component creation
(defn create-matrix-components
  "Create all matrix components"
  [config position]
  (let [state (state/create-matrix-state config)
        energy-manager (energy/create-energy config state)
        network-manager (network/create-network config state energy-manager position)
        event-dispatcher (events/create-event-dispatcher state network-manager energy-manager)]
    {:config config
     :state state
     :energy energy-manager
     :network network-manager
     :events event-dispatcher
     :position position}))

;; Matrix registry protocol and implementation
(defprotocol IMatrixRegistry
  "Protocol for managing matrix registration"
  (register-matrix-block [this])
  (register-tile-entity [this])
  (register-renderer [this])
  (register-container [this])
  (register-all [this]))

(defrecord MatrixRegistry [mod-id config]
  IMatrixRegistry
  (register-matrix-block [_]
    (merge matrix-block-def
           {:mod-id mod-id
            :config config}))
  
  (register-tile-entity [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [world pos]
                (create-matrix-components config pos))})
  
  (register-renderer [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [matrix-components]
                (render/create-renderer matrix-components))})
  
  (register-container [_]
    {:block-id (:id matrix-block-def)
     :factory (fn [matrix-components player]
                (gui/create-gui matrix-components player))})
  
  (register-all [this]
    (log/info "Registering wireless matrix block")
    {:block (register-matrix-block this)
     :tile-entity (register-tile-entity this)
     :renderer (register-renderer this)
     :container (register-container this)}))

;; Factory and registration functions
(defn create-registry
  "Create a matrix registry with the specified mod ID and config"
  ([mod-id] (create-registry mod-id (config/create-config)))
  ([mod-id config]
   (->MatrixRegistry mod-id config)))

(defn register!
  "Register all matrix components with the game"
  [mod-id]
  (let [registry (create-registry mod-id)]
    (register-all registry)))

;; Utility functions to access registry components
(def registry-cache (atom {}))

(defn get-block-def []
  (get @registry-cache :block matrix-block-def))

(defn get-tile-entity-factory []
  (get-in @registry-cache [:tile-entity :factory]))

(defn get-renderer-factory []
  (get-in @registry-cache [:renderer :factory]))

(defn get-container-factory []
  (get-in @registry-cache [:container :factory]))