(ns mcmod.protocols)

(defprotocol IBlock
  "Core block functionality"
  (get-properties [this] "Get block properties")
  (get-material [this] "Get block material type")
  (get-hardness [this] "Get block hardness")
  (get-resistance [this] "Get block resistance")
  (get-light-level [this] "Get block light level")
  (on-activated [this pos data] "Handle block activation")
  (on-placed [this pos data] "Handle block placement")
  (on-removed [this pos] "Handle block removal"))

(defprotocol IItem
  "Core item functionality"
  (get-item-properties [this] "Get item properties")
  (get-max-stack-size [this] "Get maximum stack size")
  (on-item-use [this context] "Handle item use")
  (on-item-right-click [this context] "Handle right click with item")
  (get-use-duration [this] "Get item use duration"))

(defprotocol ITileEntity
  "Core tile entity functionality"
  (tick [this] "Update tile entity state")
  (save [this] "Save tile entity data")
  (load [this data] "Load tile entity data")
  (get-update-packet [this] "Get network update packet")
  (handle-update-packet [this packet] "Handle network update packet"))

(defprotocol IContainer
  "Container functionality"
  (get-slots [this] "Get container slots")
  (transfer-stack-in-slot [this player slot-id] "Handle slot clicking")
  (can-interact-with [this player] "Check if player can use container")
  (mark-dirty [this] "Mark container as needing sync")
  (on-closed [this player] "Handle container closing"))

(defprotocol IGui
  "GUI functionality"
  (init [this] "Initialize GUI")
  (render [this mouse-data render-data] "Render GUI")
  (on-button-click [this button-id data] "Handle button clicks")
  (on-close [this] "Handle GUI closing")
  (get-title [this] "Get GUI title"))

(defprotocol IBlockContainer
  "Block container functionality"
  (set-hardness! [this hardness] "Set block hardness")
  (set-harvest-level! [this tool level] "Set harvest tool and level")
  (create-tile-entity [this world data] "Create block's tile entity")
  (get-gui-container [this player world pos] "Get block's GUI container"))

(defprotocol IModRegistry
  "Registry functionality for mod components"
  (register-block! [this id block] "Register a block")
  (register-item! [this id item] "Register an item")
  (register-tile-entity! [this id block te] "Register a tile entity")
  (register-gui! [this id container screen] "Register a GUI"))