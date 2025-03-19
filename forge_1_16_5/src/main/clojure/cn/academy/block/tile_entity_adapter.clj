(ns cn.academy.block.tile-entity-adapter
  (:require [cn.academy.forge-1-16.nbt-bridge :as nbt]
            [cn.academy.forge-1-16.capability-adapter :as cap]
            [cn.academy.block.component :as component])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.network NetworkManager]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]
           [net.minecraft.util Direction]))

;; Generic tile entity record that can work with any component
(defrecord GenericTileEntity [component serializer nbt-bridge cap-adapter tile-type]
  net.minecraft.tileentity.ITickableTileEntity
  (^void tick [this]
    (component/update! component))
  
  Object
  (save [this tag]
    (let [data (component/serialize serializer component)]
      (nbt/write-to-nbt nbt-bridge data tag))
    tag)
  
  (load [this state tag]
    (let [data (nbt/read-from-nbt nbt-bridge tag)]
      (component/deserialize! serializer component data)))
  
  (getUpdatePacket [this]
    (let [tag (nbt/create-nbt nbt-bridge)
          data (component/serialize serializer component)]
      (nbt/write-to-nbt nbt-bridge data tag)
      (SUpdateTileEntityPacket. (.getBlockPos this) -1 tag)))
  
  (getUpdateTag [this]
    (let [tag (.serializeNBT this)]
      (.save this tag)
      tag))
  
  (onDataPacket [this net packet]
    (.load this (.getBlockState this) (.getTag packet)))
  
  (handleUpdateTag [this state tag]
    (.load this state tag))
  
  (getCapability [this cap side]
    (.getCapability cap-adapter cap side))
  
  (invalidateCaps [this]
    (cap/invalidate-capabilities! cap-adapter)))

;; Factory function to register a tile entity type with a component factory function
(defn register-type [registry block-type component-factory serializer-factory id]
  (let [type (TileEntityType$Builder/of
               (reify TileEntityType$IFactory
                 (create [_ pos state]
                   (let [component (component-factory)
                         serializer (serializer-factory)
                         nbt-bridge (nbt/->ForgeNbtBridge)
                         cap-adapter (cap/create-adapter component)]
                     (->GenericTileEntity 
                       component
                       serializer
                       nbt-bridge
                       cap-adapter
                       nil))))
               (into-array net.minecraft.block.Block [block-type]))]
    (.register registry
               (net.minecraft.util.ResourceLocation. "academy" id)
               type)))

;; Create a tile entity for a specific block type
(defn create-tile-entity [block-type component-factory serializer-factory registry id]
  (register-type registry block-type component-factory serializer-factory id))