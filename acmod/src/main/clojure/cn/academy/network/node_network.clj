(ns cn.academy.network.node-network
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.tileentity.node-tile :as node-tile])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraft.util.math BlockPos]))

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
      (.writeLong (.asLong pos))
      (.writeDouble energy)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (BlockPos/fromLong (.readLong buffer))
           :energy (.readDouble buffer)))
  
  (handle [this world player]
    (when-let [tile (.getTileEntity world pos)]
      (when (instance? cn.academy.block.tileentity.TileNode tile)
        (.setEnergy tile energy)))))

;; Node state message (enabled/disabled)
(defrecord NodeStateMessage [pos enabled]
  INodeMessage
  (write-to-buffer [_ buffer]
    (doto buffer
      (.writeLong (.asLong pos))
      (.writeBoolean enabled)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (BlockPos/fromLong (.readLong buffer))
           :enabled (.readBoolean buffer)))
  
  (handle [this world player]
    (when-let [tile (.getTileEntity world pos)]
      (when (instance? cn.academy.block.tileentity.TileNode tile)
        (.setEnabled tile enabled)))))

;; Node config message (name/password)
(defrecord NodeConfigMessage [pos name password]
  INodeMessage
  (write-to-buffer [_ buffer]
    (doto buffer
      (.writeLong (.asLong pos))
      (.writeString name)
      (.writeString password)))
  
  (read-from-buffer [this buffer]
    (assoc this
           :pos (BlockPos/fromLong (.readLong buffer))
           :name (.readString buffer)
           :password (.readString buffer)))
  
  (handle [this world player]
    (when-let [tile (.getTileEntity world pos)]
      (when (instance? cn.academy.block.tileentity.TileNode tile)
        (.setNodeName tile name)
        (.setPassword tile password)))))

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