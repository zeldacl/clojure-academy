(ns cn.academy.forge.v1_16_5.bridge.block
  (:require [cn.academy.block.core :as block]
            [cn.academy.forge.v1_16_5.bridge.nbt :as nbt]
            [cn.academy.forge.v1_16_5.bridge.capability :as cap])
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
        (createTileEntity [world pos state]
          (create-tile-entity world pos state))
        
        (use [world pos state player hand side hit-x hit-y hit-z]
          (when-let [result (block/on-activated block 
                            {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                            {:id (.getUUID player)
                             :sneaking? (.isSneaking player)}
                            {:hand hand :side side})]
            (case (:action result)
              :open-gui (do (open-gui player world (:pos result))
                           true)
              false)))
        
        (setPlacedBy [world pos state placer stack]
          (block/on-placed block
                          {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}  
                          {:id (.getUUID placer)}
                          {:stack stack}))
        
        (onReplaced [oldState newState world pos block-flags]
          (when-not (= (.getBlock oldState) (.getBlock newState))
            (block/on-removed block 
                            {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}))))))
  
  (create-tile-entity [_ block world pos state]
    (proxy [TileEntity] []
      (load [state tag]
        (block/load-data block (nbt/read-nbt tag))
        (block/on-load block))
      
      (save [tag]
        (nbt/write-nbt tag (block/save-data block)))
      
      (getCapability [cap dir]
        (when-let [caps (block/get-capabilities block)]
          (cap/create-capability-provider caps)))))
  
  (register-block [_ registry block-id block]
    (.register registry
              (ResourceLocation. "academy" block-id)
              (create-block block)))
  
  (register-tile-entity [_ registry block-id tile]
    (.register registry
              (ResourceLocation. "academy" block-id)
              tile)))

;; Factory for creating block bridges
(defn create-forge-factory []
  {:bridge (->ForgeBlockBridge)})

(defn create-bridge []
  (->ForgeBlockBridge))