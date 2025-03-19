(ns forge-impl.item-converter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item Item$Properties]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ActionResult Hand]))

(defn create-item-properties [item]
  (let [properties (get-item-properties item)]
    (doto (Item$Properties.)
          (.maxStackSize (get-max-stack-size item)))))

(defn convert-to-forge-item [mcmod-item]
  (proxy [Item] [(create-item-properties mcmod-item)]
    (use [world player hand]
      (on-item-use mcmod-item
                  {:world world
                   :player player
                   :hand hand}))
    
    (onItemRightClick [world player hand]
      (on-item-right-click mcmod-item
                          {:world world
                           :player player
                           :hand hand}))
    
    (getUseDuration [stack]
      (get-use-duration mcmod-item))))