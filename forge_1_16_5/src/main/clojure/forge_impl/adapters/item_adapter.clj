(ns forge-impl.adapters.item-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item Item$Properties]
           [net.minecraft.util ActionResult ActionResultType]
           [net.minecraft.util.math.BlockPos]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]))

(defn- create-item-properties [mcmod-item]
  (let [props (Item$Properties.)]
    (doto props
      (.maxStackSize (get-max-stack-size mcmod-item))
      (.maxDamage (get-max-damage mcmod-item)))
    (when-let [tab (get-creative-tab mcmod-item)]
      (.group props tab))
    props))

(defn create-forge-item [mcmod-item]
  (proxy [Item] [(create-item-properties mcmod-item)]
    (use [world player hand]
      (let [result (on-item-use mcmod-item 
                               {:world world
                                :player player
                                :hand hand})]
        (ActionResult/resultPass (.getItemStack player hand))))
    
    (onItemRightClick [world player hand]
      (let [result (on-item-right-click mcmod-item
                                      {:world world
                                       :player player
                                       :hand hand})]
        (ActionResult/resultPass (.getItemStack player hand))))
    
    (getUseDuration [stack]
      (get-use-duration mcmod-item))
    
    (hasEffect [stack]
      (has-effect? mcmod-item stack))))