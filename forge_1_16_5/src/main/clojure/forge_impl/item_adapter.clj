(ns forge-impl.item-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item Item$Properties ItemStack]
           [net.minecraft.entity Entity]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ActionResult ActionResultType Hand]
           [net.minecraft.util.text StringTextComponent]
           [net.minecraft.creativetab CreativeTabs]))

(defrecord ForgeItemStack [^ItemStack stack]
  IItemStack
  (get-count [_]
    (.getCount stack))
  
  (set-count [_ amount]
    (.setCount stack amount))
  
  (get-item [_]
    (.getItem stack))
  
  (get-damage [_]
    (.getDamage stack))
  
  (set-damage [_ damage]
    (.setDamage stack damage))
  
  (get-tag [_]
    (.getTag stack))
  
  (set-tag [_ tag]
    (.setTag stack tag)))

(defn- create-item-properties [mcmod-item]
  (let [props (Item$Properties.)]
    (-> props
        (.maxStackSize (get-max-stack-size mcmod-item))
        (.maxDamage (get-max-damage mcmod-item)))))

(defn create-forge-item [mcmod-item]
  (proxy [Item] [(create-item-properties mcmod-item)]
    (use [world player hand]
      (let [result (on-item-use mcmod-item
                              {:world world
                               :player player
                               :hand hand})]
        (if result
          (ActionResult/resultSuccess (.getHeldItem player hand))
          (ActionResult/resultPass (.getHeldItem player hand)))))
    
    (onItemRightClick [world player hand]
      (let [result (on-item-right-click mcmod-item
                                     {:world world
                                      :player player
                                      :hand hand})]
        (if result
          (ActionResult/resultSuccess (.getHeldItem player hand))
          (ActionResult/resultPass (.getHeldItem player hand)))))
    
    (getUseDuration [stack]
      (get-use-duration mcmod-item))
    
    (getCreativeTabs []
      [(get-creative-tab mcmod-item)])
    
    (hasEffect [stack]
      (has-effect? mcmod-item stack))
    
    (isRepairable []
      (is-repairable mcmod-item))
    
    (hitEntity [stack target attacker]
      (on-hit-entity mcmod-item target attacker))))

(defn init! []
  ;; Any needed initialization for items
  nil)