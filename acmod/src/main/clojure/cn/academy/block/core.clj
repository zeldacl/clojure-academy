(ns cn.academy.block.core)

(defprotocol IBlock
  "Core block functionality"
  (get-position [this] "Get block position")
  (get-properties [this] "Get block properties")
  (on-placed [this pos placer data] "Handle block placement")
  (on-removed [this pos] "Handle block removal")
  (on-activated [this pos activator data] "Handle block activation")
  (can-place? [this pos] "Check if block can be placed")
  (can-remove? [this pos] "Check if block can be removed"))

(defprotocol IBlockEntity
  "Block entity (tile entity) functionality"
  (load-data [this data] "Load block entity data")
  (save-data [this] "Save block entity data")
  (get-capabilities [this] "Get block capabilities")
  (mark-dirty [this] "Mark block as needing save")
  (on-load [this] "Called when block entity loads")
  (on-unload [this] "Called when block entity unloads"))

(defprotocol IBlockProperties
  "Block properties"
  (get-hardness [this] "Get block hardness")
  (get-resistance [this] "Get block blast resistance")
  (get-light-level [this] "Get block light emission level")
  (is-opaque? [this] "Check if block is opaque")
  (get-material [this] "Get block material type"))

(defrecord BlockProperties [hardness resistance light-level opaque? material]
  IBlockProperties
  (get-hardness [_] hardness)
  (get-resistance [_] resistance) 
  (get-light-level [_] light-level)
  (is-opaque? [_] opaque?)
  (get-material [_] material))

(defn create-properties
  "Create block properties"
  [& {:keys [hardness resistance light-level opaque? material]
      :or {hardness 3.0
           resistance 3.0
           light-level 0
           opaque? true
           material :stone}}]
  (->BlockProperties hardness resistance light-level opaque? material))