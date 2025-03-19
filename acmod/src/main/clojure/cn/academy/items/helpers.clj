(ns cn.academy.items.helpers
  (:require [mcmod.protocols :refer :all]))

(defn create-item-base
  "Creates a base item with default properties"
  [& {:keys [max-stack-size use-duration]
      :or {max-stack-size 64
           use-duration 0}}]
  (reify IItem
    (get-item-properties [this]
      {:max-stack-size max-stack-size
       :use-duration use-duration})
    
    (get-max-stack-size [this]
      max-stack-size)
    
    (on-item-use [this context]
      true)
    
    (on-item-right-click [this context]
      true)
    
    (get-use-duration [this]
      use-duration)))