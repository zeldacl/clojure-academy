(ns cn.mcmod.protocols)

;; Block Protocols
(defprotocol IBlock
  "Core block functionality"
  (get-position [this] "Get block position")
  (get-properties [this] "Get block properties")
  (on-placed [this world pos placer] "Handle block placement")
  (on-broken [this world pos] "Handle block breaking")
  (on-activated [this world pos player hand] "Handle block activation"))

(defprotocol IBlockEntity
  "Block entity (TileEntity) functionality"
  (load-data [this tag] "Load data from NBT")
  (save-data [this] "Save data to NBT")
  (get-update-packet [this] "Get network update packet")
  (handle-update-packet [this packet] "Handle network update packet"))

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
(defprotocol IBlock
  "Core block functionality"
  (get-properties [this] "Get block properties")
  (get-material [this] "Get block material type")
  (get-hardness [this] "Get block hardness")
  (get-resistance [this] "Get block resistance")
  (get-light-level [this] "Get block light level")
  (on-activated [this pos data] "Handle block activation")
  (on-placed [this pos data] "Handle block placement")
  (on-removed [this pos] "Handle block removal")
  (get-render-type [this] "Get block render type")
  (is-opaque? [this] "Check if block is opaque"))

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

(defprotocol ITileEntity
  "Core tile entity functionality"
  (tick [this] "Update tile entity state")
  (save [this] "Save tile entity data")
  (load [this data] "Load tile entity data")
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

(defprotocol IGuiHandler
  "GUI handler functionality"
  (get-client-gui [this player world pos] "Get client-side GUI")
  (get-server-container [this player world pos] "Get server-side container"))

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

(defprotocol ICapabilityProvider
  "Capability provider functionality"
  (has-capability? [this cap side] "Check for capability")
  (get-capability [this cap side] "Get capability implementation")
  (invalidate-capabilities [this] "Invalidate cached capabilities")
  (get-capability-impl [this capability side] "Get capability implementation")
  (has-capability [this capability side] "Check if has capability"))

(defprotocol INBTSerializable
  "NBT serialization functionality"
  (write-nbt [this] "Write data to NBT")
  (read-nbt [this nbt] "Read data from NBT"))

(defprotocol INameable
  "Display name functionality"
  (get-display-name [this] "Get display name")
  (set-display-name [this name] "Set display name")
  (has-custom-name? [this] "Check if has custom name"))

(defprotocol IWorldSavedData
  "World saved data functionality"
  (load-data [this nbt] "Load data from NBT")
  (save-data [this] "Save data to NBT")
  (load-world-data [this nbt] "Load world data from NBT")
  (save-world-data [this] "Save world data to NBT")
  (set-dirty [this] "Mark data as needing save")
  (mark-dirty [this] "Mark data as needing save"))

(defprotocol IModelBakery
  "Model baking functionality"
  (bake-model [this state format spriteGetter] "Bake model")
  (get-textures [this] "Get required textures")
  (get-overrides [this] "Get model overrides"))

(defprotocol IRegistry
  "Registry functionality"
  (register-block! [this id block] "Register a block")
  (register-item! [this id item] "Register an item")
  (register-tile-entity! [this id te-supplier block] "Register a tile entity")
  (register-container! [this id container] "Register a container")
  (register-gui! [this id gui-factory] "Register a GUI"))

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

(defprotocol IEventBus
  "Protocol for platform-independent event bus"
  (register-handler [this handler] "Register event handler")
  (post-event [this event] "Post event to bus"))

(defprotocol IServerEvents
  "Protocol for server lifecycle events"
  (on-stopping [this] "Called when server is stopping")
  (on-started [this] "Called when server has started"))

(defprotocol IResourceLocation
  "Protocol for resource locations"
  (get-namespace [this] "Get resource namespace")
  (get-path [this] "Get resource path"))

(defprotocol ISoundCategory
  "Protocol for sound categories"
  (get-name [this] "Get category name")
  (get-volume [this] "Get category volume"))

(defprotocol ISoundEvent
  "Protocol for sound events"
  (get-location [this] "Get sound location")
  (get-category [this] "Get sound category"))

(defprotocol ISoundEmitter
  "Protocol for objects that can emit sounds"
  (play-sound [this sound-id pos data] "Play a sound at the given position"))

;; GUI related protocols
(defprotocol IGuiRegistry
  "GUI registry functionality"
  (register-screen [this factory] "Register screen factory")
  (register-container [this factory] "Register container factory")
  (create-menu [this id player pos] "Create menu for given ID")
  (open-screen [this screen player] "Open screen for player"))

(defprotocol IGuiRenderer
  "GUI rendering functionality"
  (render-background [this width height] "Render background")
  (render-foreground [this] "Render foreground")
  (render-tooltips [this x y] "Render tooltips"))

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

(defprotocol IGuiConfig
  "GUI configuration"
  (get-texture [this] "Get GUI texture")
  (get-title [this] "Get GUI title")
  (get-width [this] "Get GUI width")
  (get-height [this] "Get GUI height"))

(defprotocol ISlot
  "GUI slot functionality"
  (get-index [this] "Get slot index")
  (get-x [this] "Get slot x position")
  (get-y [this] "Get slot y position")
  (is-valid? [this item] "Check if item is valid for slot")
  (on-slot-changed [this] "Handle slot content change"))

;; Energy capabilities
(defprotocol IEnergyStorage
  "Energy storage capability"
  (receive-energy [this max-receive simulate] "Receive energy")
  (extract-energy [this max-extract simulate] "Extract energy")
  (get-energy-stored [this] "Get stored energy")
  (get-max-energy-stored [this] "Get max energy capacity")
  (can-extract? [this] "Whether energy can be extracted")
  (can-receive? [this] "Whether energy can be received"))

(defprotocol IWirelessNode
  "Wireless energy node capability"
  (get-range [this] "Get node range")
  (get-max-connections [this] "Get maximum connections")
  (get-max-energy [this] "Get maximum energy")
  (get-energy [this] "Get current energy")
  (connect [this other] "Connect to another node")
  (disconnect [this other] "Disconnect from another node")
  (can-connect? [this other] "Check if can connect to other node"))

;; Capability management
(defprotocol ICapabilityManager
  "Capability management functionality"
  (register-capability [this cap-type] "Register new capability type")
  (get-capability-instance [this cap-type] "Get capability instance"))

(defprotocol ICapabilityBridge
  "Bridge between platform-independent capabilities and Forge"
  (register-capability [this capability-class] "Register a new capability type")
  (create-provider [this capabilities] "Create a capability provider")
  (get-capability [this provider cap side] "Get capability from provider"))

(defprotocol IWirelessCapability
  "Wireless capability functionality"
  (get-capability [this cap dir] "Get capability for direction")
  (invalidate-caps [this] "Invalidate cached capabilities"))

;; Item handling capabilities
(defprotocol IItemHandler
  "Item handler capability"
  (get-slots [this] "Get number of slots")
  (get-stack-in-slot [this slot] "Get stack in slot")
  (insert-item [this slot stack simulate] "Insert stack into slot")
  (extract-item [this slot amount simulate] "Extract items from slot"))

(defprotocol IInventoryCapability
  "Inventory capability functionality"
  (get-inventory [this] "Get inventory interface")
  (get-item-handler [this side] "Get item handler for side"))

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

(defprotocol IFluidHandlerItem
  "Fluid handler capability for items"
  (get-container [this] "Get container item")
  (get-fluid [this] "Get contained fluid")
  (get-capacity [this] "Get container capacity")
  (fill [this resource simulate] "Fill container")
  (drain [this amount simulate] "Drain container")
  (drain-fluid [this fluid simulate] "Drain specific fluid"))

;; NBT and data storage protocols
(defprotocol INBTConverter
  "NBT data conversion functionality"
  (to-nbt [this value] "Convert value to NBT format")
  (from-nbt [this nbt] "Convert NBT back to value type"))

(defprotocol INBTStorage
  "NBT data storage functionality"
  (put-value [this key value] "Store value with key")
  (get-value [this key] "Get value by key")
  (remove-value [this key] "Remove value by key")
  (get-all-keys [this] "Get all stored keys"))

(defprotocol IDataStorage
  "General data storage functionality"
  (store-data [this key data] "Store data with key")
  (load-data [this key] "Load data by key")
  (delete-data [this key] "Delete data by key")
  (list-data [this] "List all stored data"))

(defprotocol IWorldData
  "World saved data functionality"
  (load-world-data [this] "Load data from world storage")
  (save-world-data [this] "Save data to world storage")
  (mark-world-dirty [this] "Mark world data as needing save"))

;; Registry and capability protocols
(defprotocol IRegistryManager
  "Registry management functionality"
  (get-blocks-registry [this] "Get blocks registry")
  (get-items-registry [this] "Get items registry")
  (get-tile-entities-registry [this] "Get tile entities registry")
  (get-containers-registry [this] "Get containers registry"))

(defprotocol IRegistryHandler
  "Registry event handling"
  (on-block-registry [this event] "Handle block registry event")
  (on-item-registry [this event] "Handle item registry event")
  (on-tile-entity-registry [this event] "Handle tile entity registry event"))

(defprotocol IRegistryBridge
  "Bridge between platform-independent registry and Forge"
  (register-block [this id block] "Register a block")
  (register-item [this id item] "Register an item")
  (register-tile-entity [this id type] "Register a tile entity type")
  (register-container [this id type] "Register a container type"))

;; Model and rendering protocols
(defprotocol IModelLoader
  "Model loading functionality"
  (load-model [this path] "Load a model from path")
  (load-texture [this path] "Load a texture from path")
  (register-model [this model-id model] "Register a model")
  (register-texture [this texture-id texture] "Register a texture"))

(defprotocol IModel
  "Model functionality"
  (render-part [this part-name] "Render specific model part")
  (get-texture [this] "Get model texture")
  (set-texture [this texture] "Set model texture"))

(defprotocol IModelGeometry
  "Model geometry functionality"
  (get-vertices [this] "Get model vertices")
  (get-faces [this] "Get model faces")
  (get-texture-coords [this] "Get texture coordinates")
  (apply-transform [this transform] "Apply transformation"))

(defprotocol IRenderState
  "Render state management"
  (push-matrix [this] "Push matrix stack")
  (pop-matrix [this] "Pop matrix stack")
  (translate [this x y z] "Translate")
  (rotate [this angle x y z] "Rotate")
  (scale [this x y z] "Scale")
  (bind-texture [this texture] "Bind texture"))

(defprotocol IRenderer
  "Custom renderer functionality"
  (render [this state data] "Render with state and data")
  (should-render-offscreen? [this] "Check if should render when offscreen"))

(defprotocol ITextureAtlas
  "Texture atlas functionality"
  (register-sprite [this location] "Register sprite location")
  (get-sprite [this location] "Get sprite by location")
  (stitch [this] "Stitch atlas"))

(defprotocol IAnimatedSprite
  "Animated sprite functionality"
  (get-frame [this age] "Get sprite frame for age")
  (get-random-frame [this] "Get random sprite frame")
  (update-animation [this] "Update sprite animation"))

(defprotocol IParticleRenderType
  "Particle render type functionality"
  (begin-render [this buffer] "Begin particle rendering")
  (end-render [this] "End particle rendering")
  (get-vertex-format [this] "Get vertex format"))

;; Particle and effect protocols
(defprotocol IParticleFactory
  "Particle factory functionality"
  (create-particle [this world x y z vx vy vz data] "Create a particle instance"))

(defprotocol IParticle
  "Core particle functionality"
  (tick [this] "Update particle state")
  (render [this buffer partial-ticks] "Render particle")
  (set-color [this r g b] "Set particle color")
  (set-alpha [this alpha] "Set particle alpha")
  (set-lifetime [this ticks] "Set particle lifetime")
  (is-alive? [this] "Check if particle is still alive"))

(defprotocol IParticleSystem
  "Particle system functionality"
  (spawn-particle [this world type pos velocity color lifetime] "Spawn a particle")
  (register-particle-type [this id factory] "Register particle type")
  (update-particles [this] "Update all particles")
  (render-particles [this state] "Render all particles"))

(defprotocol IParticleData
  "Particle data functionality"
  (write-to-network [this buffer] "Write particle data to network")
  (write-to-command [this builder] "Write particle data to command")
  (get-particle-type [this] "Get particle type"))

(defprotocol IParticleManager
  "Particle management functionality"
  (register-factory [this type factory] "Register particle factory")
  (spawn-particle [this data x y z vx vy vz] "Spawn particle")
  (add-effect [this effect] "Add particle effect")
  (clear-effects [this] "Clear all effects"))

(defprotocol IParticleEffect
  "Particle effect functionality"
  (spawn-particles [this world pos] "Spawn effect particles")
  (get-particles [this] "Get effect particles")
  (update-effect [this] "Update effect state")
  (is-finished? [this] "Check if effect is finished"))

;; Inventory protocols
(defprotocol IInventory
  "Core inventory functionality"
  (get-size [this] "Get inventory size")
  (get-stack [this slot] "Get item stack in slot")
  (set-stack [this slot stack] "Set item stack in slot")
  (remove-stack [this slot amount] "Remove items from slot")
  (is-empty? [this] "Check if inventory is empty")
  (mark-dirty [this] "Mark inventory as changed"))

(defprotocol IItemHandler
  "Item handler functionality"
  (get-slots [this] "Get number of slots")
  (get-stack-in-slot [this slot] "Get stack in slot")
  (insert-item [this slot stack simulate] "Insert stack into slot")
  (extract-item [this slot amount simulate] "Extract items from slot"))

(defprotocol IInventoryBridge
  "Bridge between platform-independent inventory and platform-specific implementation"
  (wrap-platform-inventory [this platform-inv]
    "Wrap platform inventory with platform-independent interface")
  (wrap-platform-item-handler [this platform-handler]
    "Wrap platform item handler with platform-independent interface")
  (to-platform-inventory [this inventory]
    "Convert platform-independent inventory to platform inventory")
  (to-platform-item-handler [this handler]
    "Convert platform-independent handler to platform item handler"))

;; Bridge protocols
(defprotocol IBridge
  "Base protocol for all bridge implementations"
  (init [this] "Initialize the bridge")
  (teardown [this] "Clean up bridge resources"))

(defprotocol IInventoryBridge
  "Bridge for inventory operations"
  (get-inventory [this holder] "Get inventory from holder")
  (get-slot-count [this inventory] "Get number of slots")
  (get-stack [this inventory slot] "Get item stack in slot")
  (set-stack [this inventory slot stack] "Set item stack in slot")
  (is-empty? [this inventory] "Check if inventory is empty"))

(defprotocol ICapabilityBridge
  "Bridge for capability system"
  (register-capability [this cap] "Register a new capability type")
  (get-capability [this object cap] "Get capability from object")
  (has-capability? [this object cap] "Check if object has capability"))
