(ns cn.academy.protocols.block
  "Core protocols for block behavior in the mod system. 
   These protocols define the contract that all block implementations must follow,
   providing a clean separation between block behavior definition and implementation.")

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

;; Dynamic var to hold the current Forge factory implementation
(def ^:dynamic *forge-factory* nil)

;; Core API functions that will be implemented by each Forge version
(defn create-tile-entity [factory]
  ((:create-tile-entity factory)))

(defn add-capability-provider! [tile-entity provider]
  ((:add-capability! *forge-factory*) tile-entity provider))

(defn mark-dirty! [tile-entity]
  ((:mark-dirty! *forge-factory*) tile-entity))

(defn get-world-time []
  ((:get-world-time *forge-factory*)))

(defn put-double! [compound key value]
  ((:put-double! *forge-factory*) compound key value))

(defn put-boolean! [compound key value]
  ((:put-boolean! *forge-factory*) compound key value))

(defn put-int! [compound key value]
  ((:put-int! *forge-factory*) compound key value))

(defn get-double [compound key default]
  ((:get-double *forge-factory*) compound key default))

(defn get-boolean [compound key default]
  ((:get-boolean *forge-factory*) compound key default))

(defn get-int [compound key default]
  ((:get-int *forge-factory*) compound key default))

(defn get-tile-entity [world pos]
  ((:get-tile-entity *forge-factory*) world pos))

(defn is-cat-engine? [tile-entity]
  ((:is-cat-engine? *forge-factory*) tile-entity))

(defn is-remote? [world]
  ((:is-remote? *forge-factory*) world))

(defn send-message [player message & args]
  (apply (:send-message *forge-factory*) player message args))

(defn on-tile-entity-tick! [tile-entity callback]
  ((:on-tile-entity-tick! *forge-factory*) tile-entity callback))

(defn on-save-nbt! [tile-entity callback]
  ((:on-save-nbt! *forge-factory*) tile-entity callback))

(defn on-load-nbt! [tile-entity callback]
  ((:on-load-nbt! *forge-factory*) tile-entity callback))

(defn set-forge-factory!
  "Set the active Forge block factory implementation"
  [factory]
  (alter-var-root #'*forge-factory* (constantly factory)))