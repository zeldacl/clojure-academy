(ns cn.academy.forge.v1_16_5.bridge.tile-entity
  (:require [cn.academy.block.tile :as tile]
            [cn.academy.component.core :as component]
            [cn.academy.forge.v1_16_5.bridge.nbt :as nbt]
            [cn.academy.forge.v1_16_5.bridge.capability :as cap])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.util Direction]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.common.capabilities Capability]
           [net.minecraftforge.common.util LazyOptional]))

(defprotocol ITileEntityBridge
  "Bridge between platform-independent tile entities and Forge"
  (create-tile-type [this factory block]
    "Create a TileEntityType for the given factory and block")
  (create-tile-entity [this tile-data]
    "Create a Forge TileEntity from platform data")
  (register-tile-type [this registry id type]
    "Register the TileEntityType"))

(deftype ForgeTileEntityAdapter [tile-data capabilities]
  TileEntity
  (load [_ block-state tag]
    (let [data (nbt/read-nbt tag)]
      (tile/load-data tile-data data)
      (tile/on-load tile-data)))
  
  (save [_ tag]
    (nbt/write-nbt tag (tile/save-data tile-data)))
  
  (setRemoved [_]
    (tile/on-removed tile-data)
    (doseq [[_ lazy-opt] @capabilities]
      (.invalidate lazy-opt))
    (reset! capabilities {}))
  
  (getCapability [_ cap side]
    (let [side-dir (if side (.get side) nil)
          capability-key [cap side-dir]]
      (if-let [capability (get @capabilities capability-key)]
        capability
        (if-let [tile-caps (tile/get-capabilities tile-data side-dir)]
          (let [provider (cap/create-capability-provider tile-caps)
                lazy-opt (.getCapability provider cap side)]
            (swap! capabilities assoc capability-key lazy-opt)
            lazy-opt)
          LazyOptional/EMPTY))))
  
  (onLoad [_]
    (tile/on-load tile-data))
  
  (clearRemoved [_]
    (tile/on-clear-removed tile-data))
  
  (getBlockPos [this]
    (let [pos (proxy-super getBlockPos)]
      {:x (.getX pos)
       :y (.getY pos)
       :z (.getZ pos)}))
  
  (markDirty [this]
    (tile/mark-dirty tile-data)
    (proxy-super markDirty)))

(deftype ForgeTileEntityBridge []
  ITileEntityBridge
  (create-tile-type [_ factory block]
    (TileEntityType/Builder/of
      (reify java.util.function.Supplier
        (get [_] (factory)))
      (into-array Block [block])
      .build))
  
  (create-tile-entity [_ tile-data]
    (->ForgeTileEntityAdapter tile-data (atom {})))
  
  (register-tile-type [_ registry id type]
    (.register registry
              (ResourceLocation. "academy" id)
              type)))

;; Public API
(defn create-bridge []
  (->ForgeTileEntityBridge))

(defn create-adapter [tile-data]
  (->ForgeTileEntityAdapter tile-data (atom {})))

(defn create-tile-type [block factory]
  (.create-tile-type (create-bridge) factory block))