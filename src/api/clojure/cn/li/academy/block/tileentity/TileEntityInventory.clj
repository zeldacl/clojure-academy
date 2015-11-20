(ns cn.li.academy.block.tileentity.TileEntityInventory
  (:gen-class :extends net.minecraft.tileentity.TileEntity
              :implements [net.minecraft.inventory.IInventory]
              :init init
              :state state
              :constructors {[String int] []}
              :exposes-methods {readFromNBT read-from-nbt,
                                writeToNBT write-to-nbt})
  (:import (net.minecraft.item ItemStack)
           (net.minecraft.nbt NBTTagCompound NBTTagList)
           (net.minecraft.entity.player EntityPlayer)))

(defn -init
  [^String inv-name size]
  [[] (atom {:inv-name inv-name :inventory (make-array ItemStack size)})])

(defn -getSizeInventory [this]
  (alength (:inventory (.state this))))

(defn -getStackInSlot [this slot]
  (aget (:inventory (.state this)) slot))

(defn -decrStackSize [this slot count]
  (let [stack (.getStackInSlot this slot)
        new-stack (if stack
                    (if (> (.stackSize this) count)
                      (let [s (.splitStack stack count)]
                        (.markDirty this)
                        s)
                      (do
                        (.setInventorySlotContents this slot nil)
                        nil))
                    stack)]
    new-stack))

(defn -getStackInSlotOnClosing [this slot]
  (let [stack (.getStackInSlot this slot)]
    (.setInventorySlotContents this slot nil)
    stack))

(defn -setInventorySlotContents [this slot ^ItemStack stack]
  (let [inventory (:inventory (.state this))]
    (aset inventory slot stack)
    (when (and stack (> (.stackSize stack) (.getInventoryStackLimit this)))
      (set! (.stackSize stack) (.getInventoryStackLimit this)))
    (.markDirty this)))

(defn -readFromNBT [this ^NBTTagCompound nbt]
  (.read-from-nbt this nbt)
  (let [items (.getTagList nbt "Items" 10)
        inventory (:inventory (.state this))
        inv-length (alength inventory)]
    (dotimes [i (.tagCount items)]
      (let [item (.getCompoundTagAt items i)
            slot (.getByte item "Slot")]
        (when (> inv-length slot -1)
          (aset inventory slot (ItemStack/loadItemStackFromNBT item)))))))

(defn -writeToNBT [this ^NBTTagCompound nbt]
  (.write-to-nbt this nbt)
  (let [items (NBTTagList.)
        inventory (:inventory (.state this))
        inv-length (alength inventory)]
    (dotimes [i inv-length]
      (when-not (aget inventory i)
        (let [item (NBTTagCompound.)]
          (.setByte item "Slot" i)
          (.writeToNBT (aget inventory i) item)
          (.appendTag items item))))
    (.setTag nbt "Items" items)))

(defn -getInventoryName [this]
  (:inv-name (.state this)))

(defn -hasCustomInventoryName [this]
  false)

(defn -getInventoryStackLimit [this ^EntityPlayer player]
  64)

(defn -isUseableByPlayer [this]
  true)

(defn -openInventory [this])

(defn -closeInventory [this])

(defn -isItemValidForSlot [this slot ^ItemStack stack]
  true)
