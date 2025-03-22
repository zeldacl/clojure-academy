(ns cn.academy.forge-1-12.block
  (:require [cn.academy.protocols.block :as block-api])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.block Block]
           [net.minecraft.block.state BlockStateContainer IBlockState]
           [net.minecraft.util.text TextComponentTranslation]))

(defrecord ForgeBlock112Factory []
  block-api/ForgeBlockFactory
  (create-block-properties [_]
    (reify block-api/BlockProperties
      (create-boolean-property [_ name]
        (Block/POWERED))  ; Example property, add more as needed
      
      (create-integer-property [_ name min max]
        (Block/AGE))  ; Example property, add more as needed
      
      (get-block-material [_ name]
        (case name
          "rock" Material/ROCK
          Material/AIR))))  ; Default to AIR if unknown

  (create-block-container [_ material]
    (proxy [Block] [material]
      (createTileEntity [world state]
        ((:create-tile-entity block-api/*forge-factory*) world (.getMetaFromState this state)))
      
      (onBlockPlaced [world pos state face hit-x hit-y hit-z meta]
        ((:on-block-placed block-api/*forge-factory*) this world pos state face hit-x hit-y hit-z meta))
      
      (getActualState [state world pos]
        ((:get-actual-state block-api/*forge-factory*) this state world pos))))

  (create-block-pos [_ x y z]
    (net.minecraft.util.math.BlockPos. x y z))

  (create-item-stack [_ block count meta]
    (net.minecraft.item.ItemStack. block count meta)))

(defn create-forge-factory []
  (->ForgeBlock112Factory))

(defprotocol IForgeBlock
  (create-tile-entity [this world meta])
  (on-block-activated [this world pos state player hand facing hit-x hit-y hit-z])
  (is-opaque-cube [this state])
  (get-render-type [this state]))

(defn send-message [player text-id & args]
  (.sendMessage player
    (TextComponentTranslation. text-id (into-array Object args))))