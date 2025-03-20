(ns cn.academy.block.api)

(defprotocol IBlockProperties
  "Protocol for block property management"
  (create-boolean-property [this name]
    "Create a boolean property with given name")
  (create-integer-property [this name min max]
    "Create an integer property with given name and range")
  (get-block-material [this name]
    "Get block material by name"))

(defprotocol IBlockState
  "Protocol for block state management"
  (with-property [this property value]
    "Add or update a property value")
  (get-property [this property]
    "Get value of a property")
  (get-default-state [this]
    "Get default state"))

(defprotocol IBlockContainer
  "Protocol for block container functionality"
  (set-hardness! [this hardness]
    "Set block hardness")
  (set-harvest-level! [this tool-class level]
    "Set required tool and level for harvesting")
  (create-tile-entity [this world meta]
    "Create a tile entity for this block")
  (on-block-placed [this world pos state player stack]
    "Handle block placement")
  (get-actual-state [this world pos]
    "Get actual block state at position"))

(defprotocol IForgeBlockFactory
  "Protocol for creating Forge block components"
  (create-block-properties [this]
    "Create block properties instance")
  (create-block-container [this material]
    "Create block container with given material") 
  (create-block-pos [this x y z]
    "Create block position")
  (create-item-stack [this block count meta]
    "Create item stack for block"))