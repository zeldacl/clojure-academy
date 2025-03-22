(ns cn.academy.blocks.block-node.registry
  (:require [cn.academy.blocks.block-node.core :as core]
            [cn.academy.blocks.block-node.tile :as tile]
            [cn.academy.blocks.block-node.container :as container]
            [cn.academy.blocks.block-node.gui :as gui]
            [cn.academy.blocks.block-node.config :as config]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [mcmod.registry :as registry]
            [clojure.tools.logging :as log]))

;; Node type mapping constants
(def ^:private NODE_TYPES
  {:basic   {:block-id "node_basic"
             :tile-factory tile/tile-factory-createBasic}
   :standard {:block-id "node_standard"
              :tile-factory tile/tile-factory-createStandard}
   :advanced {:block-id "node_advanced"
              :tile-factory tile/tile-factory-createAdvanced}})

(defn register-node-types! 
  "Register node type configurations with the energy system"
  []
  (doseq [[node-type node-config] config/node-types]
    (energy-api/register-energy-node-type! node-type node-config))
  (log/info "Registered" (count config/node-types) "node types with energy system"))

(defn register! 
  "Register all node blocks, tile entities, containers and GUIs with the mod registry"
  [mod-id]
  ;; Register node types with energy system first
  (register-node-types!)
  
  ;; Register blocks and their tile entities
  (doseq [[node-type {:keys [block-id tile-factory]}] NODE_TYPES]
    (log/debug "Registering node type:" node-type "with ID:" block-id)
    (registry/register-block! mod-id block-id (core/create-node node-type))
    (registry/register-tile-entity! mod-id block-id tile-factory))
  
  ;; Register container and GUI (shared across all node types)
  (registry/register-container! mod-id "node" container/create-container)
  (registry/register-gui! mod-id "node" gui/create-gui)
  
  (log/info "Registered" (count NODE_TYPES) "wireless node blocks"))