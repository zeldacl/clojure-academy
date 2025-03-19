(ns cn.li.bridge.block.api)

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

(defprotocol IBlockEntity
  "Block entity (tile entity) functionality"
  (get-capabilities [this] "Get block capabilities")
  (load-data [this nbt] "Load block entity data")
  (save-data [this nbt] "Save block entity data")
  (mark-dirty [this] "Mark block as needing save")
  (on-load [this] "Called when block entity loads")
  (on-unload [this] "Called when block entity unloads"))

(defn create-forge-factory []
  nil) ;; Implement in bridge