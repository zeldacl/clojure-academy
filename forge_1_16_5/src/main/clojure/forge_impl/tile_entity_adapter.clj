(ns forge-impl.tile-entity-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.tileentity TileEntity TileEntityType]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraft.network.play.server SUpdateTileEntityPacket]
           [net.minecraft.world World]
           [net.minecraft.block Block]))

(defn create-tile-entity-type [mcmod-te block]
  (-> (TileEntityType$Builder/create
        (fn []
          (proxy [TileEntity] []
            (tick []
              (tick mcmod-te))

            (save [tag]
              (let [data (save mcmod-te)]
                (doseq [[k v] data]
                  (.putString tag (name k) (str v)))
                tag))

            (load [state tag]
              (let [data (into {} (for [k (.keySet tag)]
                                  [(keyword k) (.getString tag k)]))]
                (load mcmod-te {:block-state state :data data})))

            (getUpdatePacket []
              (let [data (get-update-packet mcmod-te)
                    tag (CompoundNBT.)]
                (.putString tag "data" (str data))
                (SUpdateTileEntityPacket. (.getBlockPos this) -1 tag)))

            (onDataPacket [net packet]
              (let [tag (.getTag packet)
                    data (read-string (.getString tag "data"))]
                (handle-update-packet mcmod-te {:network net :data data})))))
        (into-array Block [block]))
      (.build)
      (assoc :block block)))