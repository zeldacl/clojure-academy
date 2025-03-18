(ns cn.academy.block.tile-adapter
  (:require [cn.academy.block.matrix-tile :as tile]))

(defprotocol ITileEntityAdapter
  "Protocol for platform-specific tile entity implementation"
  (create-tile-type [this matrix-tile]
    "Create a tile entity type for the given matrix tile")
  (wrap-tile-entity [this tile-entity matrix-tile]
    "Wrap a platform tile entity with matrix tile functionality")
  (unwrap-tile-entity [this wrapped-tile]
    "Get the underlying platform tile entity")
  (mark-dirty! [this tile-entity]
    "Mark tile entity as needing to save"))

(defprotocol ITileCapabilityAdapter
  "Protocol for platform-specific capability handling"
  (has-capability? [this tile-entity capability side]
    "Check if tile entity has capability")
  (get-capability [this tile-entity capability side]
    "Get capability handler")
  (invalidate-capabilities [this tile-entity]
    "Invalidate tile entity capabilities"))

(defprotocol ITileNetworkAdapter
  "Protocol for platform-specific networking"
  (create-packet [this tile-entity data]
    "Create network packet from data")
  (send-to-client [this tile-entity packet]
    "Send packet to client")
  (handle-packet [this tile-entity packet]
    "Handle received packet"))

(defprotocol ITileSerializationAdapter
  "Protocol for platform-specific data serialization"
  (save-tile-data [this tile-entity]
    "Save tile entity data")
  (load-tile-data! [this tile-entity data]
    "Load tile entity data")
  (create-tag-compound []
    "Create a new compound tag"))