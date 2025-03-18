(ns cn.academy.block.matrix.tile-entity-adapter
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix.capability-adapter :as cap]
            [cn.academy.forge-1-16.nbt-bridge :as nbt]
            [cn.academy.block.matrix-serialization :as ser])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.network NetworkManager]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]
           [net.minecraft.util Direction]))

(defrecord MatrixTileEntity [matrix serializer nbt-bridge cap-adapter tile-type]
  net.minecraft.tileentity.ITickableTileEntity
  (^void tick [this]
    (matrix/update! matrix))
  
  Object
  (save [this tag]
    (let [data (ser/serialize-matrix serializer matrix)]
      (nbt/write-to-nbt nbt-bridge data tag))
    tag)
  
  (load [this state tag]
    (let [data (nbt/read-from-nbt nbt-bridge tag)]
      (ser/deserialize-matrix! serializer matrix data)))
  
  (getUpdatePacket [this]
    (let [tag (nbt/create-nbt nbt-bridge)
          data (ser/serialize-matrix serializer matrix)]
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

(defn register-type [registry block-type]
  (let [type (TileEntityType/Builder/of
               (reify TileEntityType$IFactory
                 (create [_ pos state]
                   (let [matrix (matrix/create-matrix)
                         serializer (ser/create-serializer)
                         nbt-bridge (nbt/create-nbt-bridge)
                         cap-adapter (cap/create-adapter matrix)]
                     (->MatrixTileEntity 
                       matrix
                       serializer
                       nbt-bridge
                       cap-adapter
                       nil))))
               (into-array net.minecraft.block.Block [block-type]))]
    (.register registry
               (net.minecraft.util.ResourceLocation. "academy" "matrix")
               type)))