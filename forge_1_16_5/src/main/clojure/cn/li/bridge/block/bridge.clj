(ns cn.li.bridge.block.bridge
  (:require [cn.li.bridge.block.api :as block]
            [cn.li.bridge.nbt.api :as nbt]
            [cn.li.bridge.capability.api :as cap])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraft.util Direction ResourceLocation]))

(defn- to-minecraft-material [material-type]
  (case material-type
    :stone Material/STONE
    :metal Material/METAL
    :wood Material/WOOD
    Material/STONE))

(defn- to-forge-properties [properties]
  (-> (to-minecraft-material (block/get-material properties))
      AbstractBlock$Properties/of
      (.strength (block/get-hardness properties)
                (block/get-resistance properties))
      (.lightLevel (fn [_] (block/get-light-level properties)))))

(defprotocol IBlockBridge
  "Bridge between platform-independent blocks and Forge"
  (create-block [this block]
    "Create Forge block from platform block")
  (create-tile-entity [this block world pos state]
    "Create tile entity for block")
  (register-block [this registry block-id block]
    "Register block with Forge")
  (register-tile-entity [this registry block-id tile]
    "Register tile entity with Forge"))

(defrecord ForgeBlockBridge []
  IBlockBridge
  (create-block [_ block]
    (let [props (block/get-properties block)]
      (proxy [Block] [(to-forge-properties props)]
        (createTileEntity [world state]
          (create-tile-entity world (.getPosition world) state))
        
        (use [player hand hit]
          (when-let [result (block/on-activated block hit {:player player :hand hand})]
            true))
        
        (onPlaced [world pos state placer stack hand]
          (block/on-placed block pos {:placer placer :stack stack :hand hand}))
        
        (onRemoved [state1 world pos state2 isMoving]
          (block/on-removed block pos)))))

(defn create-block-bridge []
  (->ForgeBlockBridge))