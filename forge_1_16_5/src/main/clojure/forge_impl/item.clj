(ns forge-impl.item
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item]
           [net.minecraft.item.ItemStack]))

(defrecord ForgeItem [^Item item]
  IItem
  (get-item-properties [this]
    (.getProperties item))
  
  (get-max-stack-size [this]
    (.getMaxStackSize item))
  
  (on-item-use [this context]
    (.onItemUse item
                (:item-use-context context)))
  
  (on-item-right-click [this context]
    (.onItemRightClick item
                      (:world context)
                      (:player context)
                      (:hand context)))
  
  (get-use-duration [this]
    (.getUseDuration (ItemStack. item))))