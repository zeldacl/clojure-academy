(ns cn.academy.forge.world
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.world World IWorld]
           [net.minecraft.util math.BlockPos]
           [net.minecraft.block BlockState]
           [net.minecraft.entity Entity]))

(defrecord ForgeWorld [^World delegate]
  IWorld
  (get-block [this pos]
    (let [[x y z] pos
          block-pos (BlockPos. x y z)]
      (.getBlockState delegate block-pos)))
      
  (set-block [this pos block]
    (let [[x y z] pos
          block-pos (BlockPos. x y z)]
      (.setBlockState delegate block-pos block)))
      
  (get-tile-entity [this pos]
    (let [[x y z] pos
          block-pos (BlockPos. x y z)]
      (.getTileEntity delegate block-pos)))
      
  (spawn-entity [this entity]
    (.spawnEntity delegate entity))
    
  (is-remote [this]
    (.isRemote delegate))
    
  (get-redstone-power [this pos]
    (let [[x y z] pos
          block-pos (BlockPos. x y z)]
      (.getRedstonePowerFromNeighbors delegate block-pos))))

(defrecord ForgeEntity [^Entity delegate]
  IEntity
  (get-position [this]
    [(.getPosX delegate)
     (.getPosY delegate)
     (.getPosZ delegate)])
     
  (set-position [this x y z]
    (.setPosition delegate x y z))
    
  (get-motion [this]
    [(.getMotionX delegate)
     (.getMotionY delegate)
     (.getMotionZ delegate)])
     
  (set-motion [this x y z]
    (.setMotion delegate x y z))
    
  (is-alive [this]  
    (.isAlive delegate))
    
  (get-world [this]
    (->ForgeWorld (.world delegate)))
    
  (get-bounding-box [this]
    (.getBoundingBox delegate))
    
  (damage [this source amount]
    (.attackEntityFrom delegate source amount)))

;; Factory functions
(defn wrap-world [world]
  (->ForgeWorld world))
  
(defn wrap-entity [entity]
  (->ForgeEntity entity))

;; Position conversion utilities  
(extend-protocol IBlockPos
  BlockPos
  (pos->long [this]
    (.toLong this))
    
  (long->pos [value]
    (BlockPos/fromLong value)))

;; Block state bridging
(extend-protocol IBlock
  BlockState
  (get-properties [this]
    {:material (.getMaterial this)
     :hardness (.getBlockHardness this nil nil)
     :light-level (.getLightValue this)})
     
  (on-placed [this world pos placer]
    (.onBlockPlacedBy this world (BlockPos. pos) placer))
    
  (on-broken [this world pos]
    (.onBlockHarvested this world (BlockPos. pos)))
    
  (on-activated [this world pos player hand]
    (.onBlockActivated this world (BlockPos. pos) player hand)))