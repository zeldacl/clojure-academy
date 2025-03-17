(ns cn.academy.forge-1-16.block
  (:require [cn.academy.api.block :as block-api])
  (:import [net.minecraft.block.material Material]
           [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.util.text TranslationTextComponent]
           [net.minecraft.util ActionResultType]))

(defrecord ForgeBlock116Factory []
  block-api/ForgeBlockFactory
  (create-block-properties [_]
    (reify block-api/BlockProperties
      (create-boolean-property [_ name]
        (net.minecraft.state.BooleanProperty/create name))
      
      (create-integer-property [_ name min max]
        (net.minecraft.state.IntegerProperty/create name min max))
      
      (get-block-material [_ name]
        (case name
          "rock" Material/ROCK
          Material/AIR))))

  (create-block-container [_ material]
    (let [properties (-> (AbstractBlock$Properties/create material)
                        (.hardnessAndResistance 2.5))]
      (proxy [Block] [properties]
        (createTileEntity [state world]
          ((:create-tile-entity block-api/*forge-factory*) world 0))
        
        (use [state world pos player hand face hit]
          (let [result ((:on-block-activated block-api/*forge-factory*) 
                        this world pos state player hand face 
                        (.getX hit) (.getY hit) (.getZ hit))]
            (if result
              ActionResultType/SUCCESS
              ActionResultType/PASS))))))

  (create-block-pos [_ x y z]
    (net.minecraft.util.math.BlockPos. x y z))

  (create-item-stack [_ block count meta]
    (net.minecraft.item.ItemStack. block count)))

(defprotocol IForgeBlock
  (create-tile-entity [this world meta])
  (on-block-activated [this world pos state player hand facing hit-x hit-y hit-z])
  (is-opaque-cube [this state])
  (get-render-type [this state]))

(defn send-message [player text-id & args]
  (.sendMessage player
    (TranslationTextComponent. text-id (into-array Object args))
    (java.util.UUID/randomUUID)))