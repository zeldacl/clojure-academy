(ns cn.academy.tech-system.energy-system.network.messaging
  (:require [mcmod.protocols :refer :all]
            [cn.academy.tech-system.energy-system.nodes.tile :as node-tile]))

;; Message types for node network communication
(defprotocol INodeMessage
  (write-to-buffer [this buffer])
  (read-from-buffer [this buffer])
  (handle [this world player]))

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
    (when-let [tile (get-tile-entity world pos)]
      (when (node? tile)
        (set-node-energy! tile energy)))))

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
    (when-let [tile (get-tile-entity world pos)]
      (when (node? tile)
        (set-node-enabled! tile enabled)))))

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
    (when-let [tile (get-tile-entity world pos)]
      (when (node? tile)
        (set-node-config! tile name password)))))

;; Network handler factory functions
(defn create-energy-message [pos energy]
  (->NodeEnergyMessage pos energy))

(defn create-state-message [pos enabled]
  (->NodeStateMessage pos enabled))

(defn create-config-message [pos name password]
  (->NodeConfigMessage pos name password))

;; Message registration
(defn register-messages! [network]
  ;; Register message handlers
  (register-message! network "node_energy" #'NodeEnergyMessage)
  (register-message! network "node_state" #'NodeStateMessage)
  (register-message! network "node_config" #'NodeConfigMessage))