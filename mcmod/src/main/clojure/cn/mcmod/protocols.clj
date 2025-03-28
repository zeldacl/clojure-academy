(ns cn.mcmod.protocols)

;; Block Protocols
(defprotocol IBlock
  "Core block functionality"
  (get-position [this] "Get block position")
  (get-properties [this] "Get block properties")
  (get-material [this] "Get block material type")
  (get-hardness [this] "Get block hardness")
  (get-resistance [this] "Get block resistance")
  (get-light-level [this] "Get block light level")
  (on-activated [this world pos player hand] "Handle block activation")
  (on-placed [this world pos placer] "Handle block placement")
  (on-broken [this world pos] "Handle block breaking")
  (on-removed [this pos] "Handle block removal")
  (get-render-type [this] "Get block render type")
  (is-opaque? [this] "Check if block is opaque"))

(defprotocol IBlockEntity
  "Block entity (TileEntity) functionality"
  (load-data [this tag] "Load data from NBT")
  (save-data [this] "Save data to NBT")
  (get-update-packet [this] "Get network update packet")
  (handle-update-packet [this packet] "Handle network update packet")
  (on-load [this] "Called when tile entity loads")
  (on-unload [this] "Called when tile entity unloads")
  (get-capability [this cap side] "Get capability for side")
  (read-from-nbt [this nbt] "Read state from NBT")
  (write-to-nbt [this nbt] "Write state to NBT")
  (mark-dirty [this] "Mark tile entity for saving")
  (get-capabilities [this side] "Get capabilities for side")
  (get-position [this] "Get tile entity position")
  (get-block-type [this] "Get associated block type")
  (get-block-state [this] "Get current block state"))

(defprotocol IBlockRegistry
  "Block registration functionality"
  (register-block! [this block-id block] "Register a block")
  (register-block-entity! [this block-id entity-supplier] "Register a block entity"))

(defprotocol IItemRegistry
  "Item registration functionality"
  (register-item! [this item-id item] "Register an item"))

(defprotocol IMultiblock
  "Multiblock structure functionality"
  (is-complete? [this] "Check if structure is complete")
  (get-blocks [this] "Get all blocks in structure")
  (get-controller [this] "Get controller block")
  (validate-structure [this] "Validate multiblock structure")
  (on-structure-formed [this] "Called when structure forms")
  (on-structure-broken [this] "Called when structure breaks"))

(defprotocol IRegistryProvider
  "Version-specific registry provider"
  (create-registry [this mod-id] "Create new registry for mod")
  (get-registry [this] "Get existing registry")
  (register! [this registry-type id value] "Register value with given ID"))

;; Network related protocols
(defprotocol IBuffer
  "Network buffer serialization functionality"
  (write-long [this value] "Write long value to buffer")
  (read-long [this] "Read long value from buffer")
  (write-double [this value] "Write double value to buffer")
  (read-double [this] "Read double value from buffer")
  (write-boolean [this value] "Write boolean value to buffer")
  (read-boolean [this] "Read boolean value from buffer")
  (write-string [this value] "Write string value to buffer")
  (read-string [this] "Read string value from buffer"))

(defprotocol IBlockPos
  "Block position functionality"
  (pos->long [this] "Convert position to long")
  (long->pos [value] "Convert long to position"))

(defprotocol INetworkHandler
  "Network functionality"
  (send-to-server [this message] "Send message to server")
  (send-to-client [this message player] "Send message to client")
  (handle-message [this message context] "Handle received message"))

(defprotocol INetworkRegistry
  "Network registry functionality"
  (register-message! [this id message-type] "Register a network message type"))

;; Network and packet related protocols
(defprotocol IPacket
  "Protocol for network packets"
  (encode [this buf] "Encode packet data to buffer")
  (decode [this buf] "Decode packet data from buffer")
  (handle [this ctx] "Handle packet on receiving side"))

(defprotocol IPacketEncoder
  "Packet encoding functionality"
  (encode-message [this message buf] "Encode message to buffer"))

(defprotocol IPacketDecoder
  "Packet decoding functionality"
  (decode-message [this buf] "Decode message from buffer"))

(defprotocol IPacketHandler
  "Packet handling functionality"
  (handle-message [this message ctx] "Handle received message"))

(defprotocol INetworkManager
  "Network management functionality"
  (send-to-server [this message] "Send message to server")
  (send-to-client [this message player] "Send message to client")
  (send-to-all [this message] "Send message to all clients"))

(defprotocol INetworkBridge
  "Bridge between platform-independent networking and Forge"
  (create-channel [this channel-name] "Create a network channel")
  (register-message [this channel message-type encoder decoder handler direction] "Register message type with channel")
  (send-to-server [this channel message] "Send message to server")
  (send-to-client [this channel message player] "Send message to client")
  (send-to-all [this channel message] "Send message to all clients"))

;; Command protocols
(defprotocol ICommand
  "Command functionality"
  (get-name [this] "Get the command name")
  (get-permission-level [this] "Get required permission level")
  (execute [this context args] "Execute the command"))

(defprotocol ICommandBuilder
  "Command builder functionality"
  (literal [this name] "Create a literal command argument")
  (requires [this predicate] "Add permission requirement")
  (executes [this handler] "Set command execution handler")
  (then [this child] "Add child command"))

(defprotocol ICommandContext
  "Command context functionality"
  (get-source [this] "Get command source")
  (get-input [this] "Get raw command input"))

;; Core gameplay protocols
(defprotocol IItem
  "Core item functionality"
  (get-item-properties [this] "Get item properties")
  (get-max-stack-size [this] "Get maximum stack size")
  (get-max-damage [this] "Get maximum item damage")
  (on-item-use [this context] "Handle item use")
  (on-item-right-click [this context] "Handle right click with item")
  (get-use-duration [this] "Get item use duration")
  (get-creative-tab [this] "Get item creative tab")
  (has-effect? [this stack] "Check if item has enchantment glint")
  (is-repairable [this] "Check if item can be repaired")
  (on-hit-entity [this target attacker] "Handle hitting entity with item"))

(defprotocol IItemStack
  "Item stack functionality"
  (get-count [this] "Get item count")
  (set-count [this amount] "Set item count")
  (get-item [this] "Get item")
  (get-damage [this] "Get item damage")
  (set-damage [this damage] "Set item damage")
  (get-tag [this] "Get NBT tag")
  (set-tag [this tag] "Set NBT tag"))

(defprotocol IBlockProperty
  "Block property functionality"
  (get-name [this] "Get property name")
  (get-values [this] "Get possible values")
  (get-default [this] "Get default value")
  (validate [this value] "Validate if value is allowed"))

(defprotocol IBlockState
  "Block state functionality"
  (with-property [this property value] "Add or update property value")
  (get-property [this property] "Get value of property")
  (get-default-state [this] "Get default state"))

(defprotocol IContainer
  "Container functionality"
  (get-slots [this] "Get container slots")
  (get-slot [this idx] "Get slot at index")
  (set-slot [this idx stack] "Set slot contents")
  (can-interact-with [this player] "Check if player can use container")
  (transfer-stack-in-slot [this player slot-id] "Handle slot clicking")
  (detect-changes [this] "Detect and sync changes")
  (on-closed [this player] "Handle container closing")
  (get-inventory [this] "Get container inventory")
  (transfer-stack [this player index] "Transfer stack between inventories")
  (merge-stack [this slot stack] "Merge itemstack into slot")
  (detect-sync-changes [this] "Detect and sync container changes"))

(defprotocol IInventory
  "Inventory functionality"
  (get-size [this] "Get inventory size")
  (get-stack-in-slot [this slot] "Get item in slot")
  (remove-stack-in-slot [this slot amount] "Remove items from slot")
  (set-inventory-slot [this slot stack] "Set slot contents")
  (is-empty? [this] "Check if inventory is empty")
  (mark-dirty [this] "Mark inventory as changed"))

(defprotocol IInventorySlot
  "Inventory slot functionality"
  (get-stack [this] "Get slot's ItemStack")
  (set-stack [this stack] "Set slot's ItemStack")
  (get-max-stack-size [this] "Get max stack size")
  (is-item-valid? [this stack] "Check if item can go in slot"))

(defprotocol IEntity
  "Core entity functionality"
  (get-position [this] "Get entity position")
  (set-position [this x y z] "Set entity position")
  (get-motion [this] "Get entity motion vector")
  (set-motion [this x y z] "Set entity motion")
  (is-alive [this] "Check if entity is alive")
  (get-world [this] "Get entity's world")
  (get-bounding-box [this] "Get entity hitbox")
  (damage [this source amount] "Apply damage to entity"))

(defprotocol IWorld
  "Core world functionality"
  (get-block [this pos] "Get block at position")
  (set-block [this pos block] "Set block at position")
  (get-tile-entity [this pos] "Get tile entity at position")
  (spawn-entity [this entity] "Spawn entity in world")
  (is-remote [this] "Check if world is client side")
  (get-redstone-power [this pos] "Get redstone power level"))

(defprotocol IGuiHandler
  "GUI handler functionality"
  (get-client-gui [this player world pos] "Get client-side GUI")
  (get-server-container [this player world pos] "Get server-side container"))

;; Resource and sound protocols
(defprotocol IResourceLocation
  "Protocol for resource locations"
  (get-namespace [this] "Get resource namespace")
  (get-path [this] "Get resource path"))

(defprotocol ISoundCategory
  "Protocol for sound categories"
  (get-category-name [this] "Get the sound category name")
  (get-volume [this] "Get category volume"))

(defprotocol ISoundEvent
  "Protocol for sound events"
  (get-location [this] "Get resource location for the sound event")
  (get-category [this] "Get category for the sound event"))

(defprotocol ISoundEmitter
  "Protocol for objects that can emit sounds"
  (play-sound [this sound-id pos data] "Play a sound at the given position"))

;; Event and registry protocols
(defprotocol IEventBus
  "Protocol for platform-independent event bus"
  (register-handler [this handler] "Register event handler")
  (post-event [this event] "Post event to bus"))

(defprotocol IServerEvents
  "Protocol for server lifecycle events"
  (on-stopping [this] "Called when server is stopping")
  (on-started [this] "Called when server has started"))

(defprotocol IRegistry
  "Registry functionality"
  (register-block! [this id block] "Register a block")
  (register-item! [this id item] "Register an item")
  (register-tile-entity! [this id te-supplier block] "Register a tile entity")
  (register-container! [this id container] "Register a container")
  (register-gui! [this id gui-factory] "Register a GUI"))

;; GUI related protocols
(defprotocol IGui
  "GUI functionality"
  (init [this] "Initialize GUI")
  (render [this mouse-x mouse-y partial-ticks] "Render GUI")
  (on-mouse-clicked [this mouse-x mouse-y button] "Handle mouse click")
  (on-key-pressed [this key-code scan-code modifiers] "Handle key press")
  (on-close [this] "Handle GUI closing")
  (draw-background [this renderer mouse-x mouse-y] "Draw GUI background")
  (draw-foreground [this renderer mouse-x mouse-y] "Draw GUI foreground")
  (handle-mouse-click [this mouse-x mouse-y button] "Handle mouse click")
  (handle-key-press [this key scancode modifiers] "Handle key press"))

(defprotocol IGuiComponent
  "GUI component functionality"
  (init-component [this] "Initialize component")
  (render [this x y partial-ticks] "Render component")
  (is-mouse-over? [this mouse-x mouse-y] "Check if mouse is over")
  (on-mouse-clicked [this mouse-x mouse-y button] "Handle mouse click"))

(defprotocol IScreen
  "Core screen functionality"
  (init [this] "Initialize the screen")
  (render [this state data] "Render the screen")
  (tick [this] "Update screen state")
  (on-close [this] "Handle screen closing"))

(defprotocol IScreenFactory
  "Screen factory functionality"
  (create-screen [this container player] "Create screen instance")
  (get-screen-id [this] "Get unique screen identifier"))

(defprotocol IContainerFactory
  "Container factory functionality"
  (create-container [this id player pos] "Create container instance")
  (get-container-id [this] "Get unique container identifier"))

;; Capability protocols
(defprotocol ICapabilityProvider
  "Capability provider functionality"
  (has-capability? [this cap side] "Check for capability")
  (get-capability [this cap side] "Get capability implementation")
  (invalidate-capabilities [this] "Invalidate cached capabilities"))

(defprotocol INBTSerializable
  "NBT serialization functionality"
  (write-nbt [this] "Write data to NBT")
  (read-nbt [this nbt] "Read data from NBT"))

;; Energy capabilities
(defprotocol IEnergyStorage
  "Energy storage capability"
  (receive-energy [this max-receive simulate] "Receive energy")
  (extract-energy [this max-extract simulate] "Extract energy")
  (get-energy-stored [this] "Get stored energy")
  (get-max-energy-stored [this] "Get max energy capacity")
  (can-extract? [this] "Whether energy can be extracted")
  (can-receive? [this] "Whether energy can be received"))

;; Item handling capabilities
(defprotocol IItemHandler
  "Item handler capability"
  (get-slots [this] "Get number of slots")
  (get-stack-in-slot [this slot] "Get stack in slot")
  (insert-item [this slot stack simulate] "Insert stack into slot")
  (extract-item [this slot amount simulate] "Extract items from slot"))

;; Fluid handling capabilities
(defprotocol IFluidStorage
  "Fluid storage capability"
  (get-fluid [this] "Get stored fluid")
  (get-fluid-amount [this] "Get amount of stored fluid")
  (get-capacity [this] "Get storage capacity")
  (fill [this resource simulate] "Fill storage with fluid")
  (drain [this amount simulate] "Drain fluid from storage"))

(defprotocol IFluidHandler
  "Fluid handler capability"
  (get-tanks [this] "Get number of tanks")
  (get-fluid-in-tank [this tank] "Get fluid in tank")
  (get-tank-capacity [this tank] "Get tank capacity")
  (is-fluid-valid [this tank fluid] "Check if fluid is valid for tank"))
