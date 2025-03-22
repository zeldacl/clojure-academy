(ns cn.academy.blocks.block-node.network
  "Network message handling for wireless energy nodes.
   Defines message types for synchronizing node state between server and client."
  (:require [mcmod.protocols :refer :all]
            [cn.academy.blocks.block-node.tile :as tile-node]
            [clojure.tools.logging :as log]))

;; Common message handling utilities
(defn- handle-node-message
  "Generic handler for node messages that handles common validation"
  [pos world action-fn]
  (if-let [tile (get-tile-entity world pos)]
    (if (tile-node/node? tile)
      (try
        (action-fn tile)
        true
        (catch Exception e
          (log/error "Error handling node message:" (.getMessage e))
          false))
      (log/warn "Received message for non-node tile entity at" pos))
    (log/warn "Received message for non-existent tile entity at" pos)))

;; Message protocol
(defprotocol INodeMessage
  "Protocol for node network messages"
  (write-to-buffer [this buffer] "Serialize message to network buffer")
  (read-from-buffer [this buffer] "Deserialize message from network buffer")
  (handle [this world player] "Handle received message"))

;; Energy update message
(defrecord NodeEnergyMessage [pos energy]
  INodeMessage
  (write-to-buffer [_ buffer]
    (doto buffer
      (write-long (pos->long pos))
      (write-double energy)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (long->pos (read-long buffer))
           :energy (read-double buffer)))
  
  (handle [this world player]
    (handle-node-message pos world #(tile-node/set-node-energy! % energy))))

;; Node state message (enabled/disabled)
(defrecord NodeStateMessage [pos enabled]
  INodeMessage
  (write-to-buffer [_ buffer]
    (doto buffer
      (write-long (pos->long pos))
      (write-boolean enabled)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (long->pos (read-long buffer))
           :enabled (read-boolean buffer)))
  
  (handle [this world player]
    (handle-node-message pos world #(tile-node/set-node-enabled! % enabled))))

;; Node config message (name/password)
(defrecord NodeConfigMessage [pos name password]
  INodeMessage
  (write-to-buffer [_ buffer]
    (doto buffer
      (write-long (pos->long pos))
      (write-string name)
      (write-string password)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (long->pos (read-long buffer))
           :name (read-string buffer)
           :password (read-string buffer)))
  
  (handle [this world player]
    (handle-node-message pos world #(tile-node/set-node-config! % name password))))

;; Message type map for registration
(def ^:private message-types
  {"node_energy" NodeEnergyMessage
   "node_state" NodeStateMessage
   "node_config" NodeConfigMessage})

;; Network handler factory functions
(defn create-energy-message 
  "Create a new energy update message"
  [pos energy]
  (->NodeEnergyMessage pos energy))

(defn create-state-message 
  "Create a new node state message"
  [pos enabled]
  (->NodeStateMessage pos enabled))

(defn create-config-message 
  "Create a new node configuration message"
  [pos name password]
  (->NodeConfigMessage pos name password))

;; Message registration
(defn register-messages! 
  "Register all node message types with the network handler"
  [network]
  (doseq [[msg-id msg-type] message-types]
    (register-message! network msg-id msg-type))
  (log/debug "Registered" (count message-types) "node message types"))

;; Send message helpers
(defn send-energy-update! 
  "Send an energy update message to clients"
  [network pos energy]
  (send-message! network (create-energy-message pos energy)))

(defn send-state-update! 
  "Send a node state update message to clients"
  [network pos enabled]
  (send-message! network (create-state-message pos enabled)))

(defn send-config-update! 
  "Send a node configuration update message to clients"
  [network pos name password]
  (send-message! network (create-config-message pos name password)))