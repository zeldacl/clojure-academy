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
  (on-removed [this pos] "Handle block removal")
  (get-render-type [this] "Get block render type")
  (is-opaque? [this] "Check if block is opaque"))

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
  (get-update-packet [this] "Get update packet for sync")
  (handle-update-packet [this packet] "Handle incoming update")
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
  (set-dirty [this] "Mark data as needing save"))

(defprotocol IWorldSavedData
  "World saved data functionality"
  (save-world-data [this] "Save data to NBT")
  (load-world-data [this nbt] "Load data from NBT")
  (mark-dirty [this] "Mark data as needing save"))

(defprotocol INetworkHandler
  "Network functionality"
  (send-to-server [this message] "Send message to server")
  (send-to-client [this message player] "Send message to client")
  (handle-message [this message context] "Handle received message"))

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