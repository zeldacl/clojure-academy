(ns cn.academy.block.matrix-nbt-adapter
  (:require [cn.academy.block.matrix-nbt :as nbt])
  (:import [net.minecraft.nbt NBTTagCompound NBTTagList]
           [net.minecraft.item ItemStack]))

(defprotocol IForgeNBTAdapter
  (write-to-compound [this compound])
  (read-from-compound [this compound])
  (serialize-item-stack [this stack])
  (deserialize-item-stack [this nbt]))

(defrecord ForgeNBTAdapter [nbt-handler]
  IForgeNBTAdapter
  (write-to-compound [_ compound]
    (let [tag (NBTTagCompound.)]
      (nbt/write-to-nbt nbt-handler tag)
      (.setTag compound "matrix" tag))
    compound)
  
  (read-from-compound [_ compound]
    (when-let [tag (.getCompoundTag compound "matrix")]
      (nbt/read-from-nbt nbt-handler tag)))
  
  (serialize-item-stack [_ stack]
    (when stack
      (let [tag (NBTTagCompound.)]
        (.writeToNBT stack tag)
        tag)))
  
  (deserialize-item-stack [_ tag]
    (when tag
      (ItemStack/loadItemStackFromNBT tag))))

(defn create-nbt-adapter [nbt-handler]
  (->ForgeNBTAdapter nbt-handler))