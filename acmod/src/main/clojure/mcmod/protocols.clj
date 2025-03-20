(ns mcmod.protocols)

;; Buffer protocol for network message serialization
(defprotocol IBuffer
  (write-long [this value])
  (read-long [this])
  (write-double [this value])
  (read-double [this])
  (write-boolean [this value])
  (read-boolean [this])
  (write-string [this value])
  (read-string [this]))

;; Position protocol for block positions
(defprotocol IBlockPos
  (pos->long [this])
  (long->pos [value]))

;; World protocol for tile entity access
(defprotocol IWorld
  (get-tile-entity [this pos]))

;; Node protocol for tile entity validation
(defprotocol INode
  (node? [this])
  (set-node-energy! [this energy])
  (set-node-enabled! [this enabled])
  (set-node-config! [this name password]))

;; Network message registration
(defprotocol INetworkRegistry
  (register-message! [this id message-type])))