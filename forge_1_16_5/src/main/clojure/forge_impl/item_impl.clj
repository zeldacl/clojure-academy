(ns forge-impl.item-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item Item$Properties]
           [net.minecraft.util ActionResult]))

(defrecord ForgeItem [^Item item]
  IItem
  (get-item-properties [this]
    (.getProperties item))
  
  (get-max-stack-size [this]
    (.getMaxStackSize item))
  
  (on-item-use [this context]
    (.use item 
         (:world context)
         (:player context)
         (:hand context)))
  
  (on-item-right-click [this context]
    (.onItemRightClick item
                      (:world context)
                      (:player context)
                      (:hand context)))
  
  (get-use-duration [this]
    (.getUseDuration item)))