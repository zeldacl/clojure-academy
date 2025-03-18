(ns cn.academy.block.matrix-adapter
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IBlockAdapter
  "Platform-independent block adapter"
  (create-block [this config]
    "Create a block with given config")
  (get-block-properties [this config]
    "Get platform-agnostic block properties")
  (handle-block-activated [this world pos state player]
    "Handle block activation")
  (handle-block-placed [this world pos state placer stack]
    "Handle block placement"))

(defprotocol ITileEntityAdapter
  "Platform-independent tile entity adapter"
  (create-tile-entity [this matrix world pos]
    "Create a tile entity for given matrix and position")
  (get-tile-position [this tile]
    "Get position of tile entity")
  (get-tile-world [this tile]
    "Get world of tile entity")
  (mark-tile-dirty! [this tile]
    "Mark tile entity as needing save")
  (invalidate-tile! [this tile]
    "Invalidate tile entity"))

(defprotocol IWorldAdapter
  "Platform-independent world interaction adapter"
  (get-tile-at [this world pos]
    "Get tile entity at position")
  (set-tile-at! [this world pos tile]
    "Set tile entity at position")
  (get-block-state [this world pos]
    "Get block state at position")
  (set-block-state! [this world pos state notify]
    "Set block state at position"))

(defprotocol IPositionAdapter
  "Platform-independent position adapter"
  (create-pos [this x y z]
    "Create a position")
  (get-x [this pos]
    "Get x coordinate")
  (get-y [this pos]
    "Get y coordinate")
  (get-z [this pos]
    "Get z coordinate"))

(defprotocol IMatrixStructureAdapter
  "Platform-independent matrix structure adapter"
  (create-structure [this]
    "Create matrix structure")
  (validate-structure [this world pos]
    "Validate matrix structure at position")
  (get-block-positions [this center]
    "Get block positions in structure"))

(defprotocol IPlayerAdapter
  "Platform-independent player adapter"
  (get-player-name [this player]
    "Get player name")
  (can-interact? [this player pos range]
    "Check if player can interact")
  (get-player-inventory [this player]
    "Get player inventory")
  (send-message [this player message]
    "Send message to player"))

(defprotocol IMatrixGuiAdapter
  "Platform-independent GUI adapter"
  (open-gui [this player world pos]
    "Open matrix GUI")
  (create-container [this player tile]
    "Create container for GUI")
  (get-slot-positions [this]
    "Get slot positions for GUI layout"))

(defprotocol IMatrixDataAdapter
  "Platform-independent data adapter" 
  (serialize-matrix [this matrix]
    "Serialize matrix data")
  (deserialize-matrix! [this matrix data]
    "Deserialize matrix data")
  (create-data-tag []
    "Create new data tag"))

(defrecord MatrixBlockConfig [material hardness resistance light-level])

(defn create-config
  "Create standard matrix block config"
  []
  (->MatrixBlockConfig :rock 3.0 3.0 1))

(defn create-matrix-structure
  "Create standard matrix structure points"
  []
  [[0 0 0] [1 0 0]
   [0 1 0] [1 1 0]
   [0 0 1] [1 0 1]
   [0 1 1] [1 1 1]])