(ns forge-impl.adapters.tile-entity-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraft.network NetworkManager]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]
           [net.minecraft.util Direction]))

(defn- convert-nbt-to-map [^CompoundNBT nbt]
  (into {} (for [key (.keySet nbt)]
             [(keyword key) (.get nbt key)])))

(defn- convert-map-to-nbt [data]
  (let [nbt (CompoundNBT.)]
    (doseq [[k v] data]
      (.put nbt (name k) v))
    nbt))

(defn create-tile-entity [mcmod-te]
  (proxy [TileEntity] []
    (tick []
      (tick mcmod-te))
    
    (save [nbt]
      (let [data (save mcmod-te)]
        (doseq [[k v] data]
          (.put nbt (name k) v))))
    
    (load [state nbt]
      (load mcmod-te (convert-nbt-to-map nbt)))
    
    (getUpdatePacket []
      (when-let [data (get-update-packet mcmod-te)]
        (let [nbt (convert-map-to-nbt data)]
          (SUpdateTileEntityPacket. (.getPos this) -1 nbt))))
    
    (onDataPacket [net packet]
      (let [nbt (.getNbtCompound packet)
            data (convert-nbt-to-map nbt)]
        (handle-update-packet mcmod-te data)))

    (onLoad []
      (on-load mcmod-te))
    
    (onChunkUnloaded []
      (on-unload mcmod-te))
    
    (getCapability [cap dir]
      (get-capability mcmod-te cap (when dir (keyword (.name dir)))))))

(defn create-tile-entity-type [mcmod-te block]
  (-> (TileEntityType$Builder/create
       (fn [] (create-tile-entity mcmod-te))
       (into-array [block]))
      .build))