(ns forge-impl.item-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.item Item ItemStack]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ActionResultType Hand]
           [net.minecraft.util.text StringTextComponent]))

(defrecord ForgeItem [^Item item]
  IItem
  (get-item-properties [_]
    (.getProperties item))
  
  (get-max-stack-size [_]
    (.getMaxStackSize item))
  
  (get-max-damage [_]
    (.getMaxDamage item))
  
  (on-item-use [_ context]
    (let [result (.onItemRightClick item 
                                   (:world context)
                                   (:player context)
                                   (:hand context))]
      {:action-result (.getType result)
       :resulting-stack (.getObject result)}))
  
  (on-item-right-click [_ context]
    (let [result (.onItemUse item
                            (:context context))]
      (.getType result)))
  
  (get-use-duration [_]
    (.getUseDuration item (ItemStack. item)))
  
  (get-creative-tab [_]
    (.getGroup item))
  
  (has-effect? [_ stack]
    (.hasEffect stack))
  
  (is-repairable [_]
    (.isRepairable item))
  
  (on-hit-entity [_ target attacker]
    (.hitEntity item 
               (ItemStack. item)
               target
               attacker)))