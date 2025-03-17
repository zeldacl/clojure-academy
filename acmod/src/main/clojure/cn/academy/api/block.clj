(ns cn.academy.api.block)

(defprotocol BlockProperties
  "Protocol defining block property operations"
  (create-boolean-property [this name]
    "Create a boolean property with given name")
  (create-integer-property [this name min max]
    "Create an integer property with given name and range")
  (get-block-material [this name]
    "Get block material by name"))

(defprotocol BlockState
  "Protocol defining block state operations"
  (with-property [this property value]
    "Update block state with given property value")
  (get-property [this property]
    "Get value of given property")
  (get-default-state [this]
    "Get default block state"))

(defprotocol BlockContainer
  "Protocol defining block container operations"
  (set-hardness! [this hardness]
    "Set block hardness")
  (set-harvest-level! [this tool-class level]
    "Set block harvest tool and level")
  (create-tile-entity [this world meta]
    "Create tile entity for this block")
  (on-block-placed [this world pos state player stack]
    "Handle block placement")
  (get-actual-state [this world pos]
    "Get actual block state"))

(defprotocol ForgeBlockFactory
  "Protocol for creating version-specific block implementations"
  (create-block-properties [this]
    "Create a BlockProperties implementation")
  (create-block-container [this material]
    "Create a BlockContainer with given material")
  (create-block-pos [this x y z]
    "Create a BlockPos")
  (create-item-stack [this block count meta]
    "Create an ItemStack"))

;; Dynamic var to hold the current factory instance
(def ^:dynamic *forge-factory* nil)

(defn set-forge-factory!
  "Set the active Forge block factory implementation"
  [factory]
  (alter-var-root #'*forge-factory* (constantly factory)))