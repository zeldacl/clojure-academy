(ns cn.academy.core.network.messages)

(defprotocol INetworkMessage
  (encode [this buf])
  (decode [this buf])
  (handle [this context]))

(defrecord CatEngineUpdateMessage [pos energy-stored linked?]
  INetworkMessage
  (encode [_ buf]
    (doto buf
      (.writeBlockPos pos)
      (.writeDouble energy-stored)
      (.writeBoolean linked?)))
  
  (decode [_ buf]
    (->CatEngineUpdateMessage
      (.readBlockPos buf)
      (.readDouble buf)
      (.readBoolean buf)))
  
  (handle [this {:keys [player world]}]
    (when (and player world (.isClientSide world))
      (when-let [tile (.getTileEntity world (:pos this))]
        (.setEnergyStored tile (:energy-stored this))
        (.setLinked tile (:linked? this))))))