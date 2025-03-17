(ns cn.academy.block.matrix-nbt-adapter
  (:require [cn.academy.block.matrix-nbt :as nbt])
  (:import [net.minecraft.nbt CompoundNBT ListNBT]
           [net.minecraft.item ItemStack]))

(defprotocol IForgeNBTAdapter
  (write-to-compound [this compound])
  (read-from-compound [this compound])
  (serialize-item-stack [this stack])
  (deserialize-item-stack [this nbt]))

(defrecord ForgeNBTAdapter [nbt-handler]
  IForgeNBTAdapter
  (write-to-compound [_ compound]
    (let [tag (CompoundNBT.)]
      (nbt/write-to-nbt nbt-handler tag)
      (.put compound "matrix" tag))
    compound)
  
  (read-from-compound [_ compound]
    (when-let [tag (.getCompound compound "matrix")]
      (nbt/read-from-nbt nbt-handler tag)))
  
  (serialize-item-stack [_ stack]
    (when stack
      (.serializeNBT stack)))
  
  (deserialize-item-stack [_ tag]
    (when tag
      (ItemStack/read tag))))

(defn create-nbt-adapter [nbt-handler]
  (->ForgeNBTAdapter nbt-handler))