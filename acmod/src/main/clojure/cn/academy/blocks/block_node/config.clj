(ns cn.academy.blocks.block-node.config
  "Configuration for wireless energy node blocks with different tiers.
   This module defines the node types and their properties, and provides
   helper functions to access them."
  (:require [clojure.tools.logging :as log]))

;; Node type definitions with their properties
(def node-types
  "Map of node types and their properties.
   Each node type has the following properties:
   - :max-energy - Maximum energy storage capacity (in FE)
   - :range - Connection range in blocks
   - :max-connections - Maximum number of simultaneous connections
   - :bandwidth - Energy transfer rate per tick (in FE/t)"
  {:basic {:max-energy 5000
           :range 8
           :max-connections 4
           :bandwidth 500}
   
   :standard {:max-energy 20000
              :range 16
              :max-connections 8
              :bandwidth 1000}
   
   :advanced {:max-energy 100000
              :range 32
              :max-connections 16
              :bandwidth 2000}})

(def ^:private valid-properties
  "Set of valid node properties"
  #{:max-energy :range :max-connections :bandwidth})

(defn get-node-config
  "Get the complete configuration for a node type.
   Returns nil if the node type doesn't exist."
  [node-type]
  (when-not (contains? node-types node-type)
    (log/warn "Requested unknown node type:" node-type))
  (get node-types node-type))

(defn get-node-property
  "Get a specific property for a node type.
   Returns nil if the node type or property doesn't exist."
  [node-type property]
  (when-not (contains? valid-properties property)
    (log/warn "Requested unknown node property:" property "for node type:" node-type))
  (get-in node-types [node-type property]))

(defn get-node-property-with-default
  "Get a specific property for a node type, with a default value if not found."
  [node-type property default-value]
  (or (get-node-property node-type property) default-value))

;; Define public constants for easy access to node types
(def BASIC :basic)
(def STANDARD :standard)
(def ADVANCED :advanced)

;; Define public constants for property names
(def PROP_MAX_ENERGY :max-energy)
(def PROP_RANGE :range)
(def PROP_MAX_CONNECTIONS :max-connections)
(def PROP_BANDWIDTH :bandwidth)