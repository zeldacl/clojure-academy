(ns forge-impl.entity-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.entity Entity]
           [net.minecraft.util.math vector3d BlockPos]
           [net.minecraft.world World]
           [net.minecraft.util DamageSource]))

(defrecord ForgeEntity [^Entity entity]
  IEntity
  (get-position [_]
    (let [pos (.getPositionVec entity)]
      {:x (.x pos) :y (.y pos) :z (.z pos)}))
  
  (set-position [_ x y z]
    (.setPosition entity x y z))
  
  (get-motion [_]
    (let [motion (.getMotion entity)]
      {:x (.x motion) :y (.y motion) :z (.z motion)}))
  
  (set-motion [_ x y z]
    (.setMotion entity (Vector3d. x y z)))
  
  (is-alive [_]
    (.isAlive entity))
  
  (get-world [_]
    (.getEntityWorld entity))
  
  (get-bounding-box [_]
    (.getBoundingBox entity))
  
  (damage [_ source amount]
    (.attackEntityFrom entity source amount)))

(defrecord ForgeWorld [^World world]
  IWorld
  (get-block [_ pos]
    (.getBlockState world (BlockPos. (:x pos) (:y pos) (:z pos))))
  
  (set-block [_ pos block]
    (.setBlockState world 
                   (BlockPos. (:x pos) (:y pos) (:z pos))
                   block))
  
  (get-tile-entity [_ pos]
    (.getTileEntity world (BlockPos. (:x pos) (:y pos) (:z pos))))
  
  (spawn-entity [_ entity]
    (.addEntity world (:entity entity)))
  
  (is-remote [_]
    (.isRemote world))
  
  (get-redstone-power [_ pos]
    (.getRedstonePowerFromNeighbors world (BlockPos. (:x pos) (:y pos) (:z pos)))))

(defn create-entity-wrapper [entity]
  (->ForgeEntity entity))

(defn create-world-wrapper [world]
  (->ForgeWorld world))