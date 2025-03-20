(ns forge-impl.block.node-block-adapter
  (:require [mcmod.protocols :refer :all]
            [forge-impl.block.block-state-adapter :as state-adapter])
  (:import [net.minecraft.block Block AbstractBlock$Properties BlockState]
           [net.minecraft.block.material Material]
           [net.minecraft.state StateContainer StateHolder IProperty]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.world World IBlockReader]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util Direction Hand ActionResultType]
           [net.minecraft.tileentity TileEntity]))

;; Convert mcmod block properties to Forge block properties
(defn- create-block-properties [mcmod-block]
  (let [props (AbstractBlock$Properties/create Material/IRON)]
    (-> props
        (.hardnessAndResistance (float (get-hardness mcmod-block))
                               (float (get-resistance mcmod-block)))
        (.setLightLevel (fn [_] (float (get-light-level mcmod-block))))
        (.notSolid))))

;; Create Forge node block implementation
(defn create-forge-node-block [mcmod-block]
  (let [state-container (atom nil)]
    (proxy [Block] [(create-block-properties mcmod-block)]
      (createBlockState []
        (let [container (state-adapter/create-state-container 
                         this 
                         (get-state-properties mcmod-block))]
          (reset! state-container container)
          container))
      
      (getStateForPlacement [context]
        (let [state (.getDefaultState this)
              world (.getWorld context)
              pos (.getPos context)]
          (state-adapter/update-state-from-world 
            state mcmod-block world pos)))
      
      (getDefaultState []
        (state-adapter/create-default-state 
          @state-container mcmod-block))
      
      (use [state world pos player hand face hit]
        (let [result (on-activated mcmod-block
                                {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                                {:world world
                                 :player player
                                 :hand hand
                                 :hit-face face
                                 :hit-vec hit})]
          (if result
            ActionResultType/SUCCESS
            ActionResultType/PASS)))
      
      (onBlockPlacedBy [world pos state placer stack]
        (on-placed mcmod-block
                  {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                  {:world world
                   :player placer
                   :stack stack}))
      
      (onReplaced [state world pos newState isMoving]
        (on-removed mcmod-block
                  {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}))
      
      (hasTileEntity [state]
        true)
      
      (createTileEntity [state world]
        (let [te-supplier (:tile-entity-supplier (get-properties mcmod-block))]
          (when te-supplier
            (te-supplier))))
      
      (getActualState [state world pos]
        (state-adapter/update-state-from-world 
          state mcmod-block world pos)))))

;; Block registry method
(defmethod forge-impl.block-adapter/create-forge-block "node"
  [mcmod-block]
  (create-forge-node-block mcmod-block))