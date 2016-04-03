(ns cn.li.forge-api.tileentities
  (:use [cn.li.forge-api.utils :only [setfield getfield]])
  (:use [cn.li.forge-api.core :only [defclass]])
  (:use [cn.li.forge-api.nbt :only [nbt->atom! atom->nbt! map->nbt nbt->map]])
  (:import (net.minecraft.inventory IInventory)
           (net.minecraft.item ItemStack)
           (net.minecraft.tileentity TileEntity)
           (net.minecraft.network.play.server S35PacketUpdateTileEntity)
           (net.minecraft.nbt NBTTagCompound)))


(defmacro deftileentity [class-name & options]
  (let [metadata (apply hash-map options)
        prefix (str class-name "-")
        sync-data (get metadata :sync-data [])
        metadata (assoc-in metadata [:exposes-methods 'readFromNBT] 'superReadFromNBT)
        metadata (assoc-in metadata [:exposes-methods 'writeFromNBT] 'superWriteFromNBT)
        metadata (assoc metadata :extends TileEntity)
        ]
    `(do
       (defclass ~class-name ~metadata)
       (defn ~(symbol (str prefix "readFromNBT")) [~'this ~'nbt]
         (~'.superReadFromNBT ~'this ~'nbt)
         (nbt->atom! ~'@this ~'nbt))
       (defn ~(symbol (str prefix "writeFromNBT")) [~'this ~'nbt]
         (~'.superReadFromNBT ~'this ~'nbt)
         (atom->nbt! ~'@this ~'nbt))
       (defn ~(symbol (str prefix "getDescriptionPacket")) [~'this]
         (S35PacketUpdateTileEntity. (.xCoord ~'this) (.yCoord ~'this) (.zCoord ~'this) 1
                                     (map->nbt (select-keys (deref ~'@this) ~sync-data) (NBTTagCompound.))))
       (defn ~(symbol (str prefix "onDataPacket")) [~'this ~'net ~'pkt]
         (swap! ~'@this merge (nbt->map (.func_148857_g ~'pkt))))
       )))


(defmacro deftileentityinventory [class-name inv-name size & options]
  (let [metadata (apply hash-map options)
        prefix (str class-name "-")
        ;metadata (assoc-in metadata [:interfaces (count (get metadata :interfaces []))] 'IInventory)
        ;metadata (assoc-in metadata [:fields :inventory] (into [] (repeat size nil)))
        ]
    `(do
       (deftileentity ~class-name
                      :interfaces ~(conj (get metadata :interfaces []) 'net.minecraft.inventory.IInventory)
                      :fields ~(assoc (get metadata :field {}) :inventory (into [] (repeat size nil)))
                      :sync-data ~(:sync-data metadata))
       (def ~(symbol (str prefix "getSizeInventory"))
         (constantly ~size))
       (defn ~(symbol (str prefix "getStackInSlot")) [~'this ~'slot]
         (get (getfield ~'this :inventory) ~'slot))
       (defn ~(symbol (str prefix "decrStackSize")) [~'this ~'slot ~'count]
         (let [^ItemStack stack# (.getStackInSlot ~'this ~'slot)]
           (if stack#
             (do
               (if (<= (.stackSize stack#) ~'count)
                 (do
                   (.setInventorySlotContents ~'this ~'slot nil)
                   (.markDirty ~'this)
                   nil)
                 (let [s# (.splitStack stack# count)]
                   (.markDirty ~'this)
                   (if (= (.stackSize stack#) 0)
                     (do
                       (.setInventorySlotContents ~'this ~'slot nil)
                       nil)
                     s#))))
             stack#)))
       (defn ~(symbol (str prefix "getStackInSlotOnClosing")) [~'this ~'slot]
         (let [^ItemStack stack# (.getStackInSlot ~'this ~'slot)]
           (if stack#
             (.setInventorySlotContents ~'this ~'slot nil))
           stack#))
       (defn ~(symbol (str prefix "setInventorySlotContents")) [~'this ~'slot ^ItemStack ~'stack]
         (if (and ~'stack (> (.stackSize ~'stack) (.getInventoryStackLimit ~'this)))
           (set! (.stackSize ~'stack) (.getInventoryStackLimit ~'this)))
         (setfield ~'this :inventory (assoc (getfield ~'this :inventory) ~'slot ~'stack))
         (.markDirty ~'this))
       (def ~(symbol (str prefix "getInventoryName"))
         (constantly ~inv-name))
       (def ~(symbol (str prefix "hasCustomInventoryName"))
         (constantly false))
       (def ~(symbol (str prefix "getInventoryStackLimit"))
         (constantly 64))
       (def ~(symbol (str prefix "isUseableByPlayer"))
         (constantly true))
       (def ~(symbol (str prefix "openInventory"))
         (constantly nil))
       (def ~(symbol (str prefix "closeInventory"))
         (constantly nil))
       (def ~(symbol (str prefix "isItemValidForSlot"))
         (constantly true)))))
