(ns mcmod.util
  (:require [mcmod.protocols :refer :all]
            [mcmod.mod-registry :as mr]))

(defn register-mod-block
  "Register a block with a specific mod ID"
  [registry mod-id block-id block]
  (register-block registry block-id block)
  (swap! (:mod-blocks registry) update mod-id assoc block-id block))

(defn register-mod-item
  "Register an item with a specific mod ID"
  [registry mod-id item-id item]
  (register-item registry item-id item)
  (swap! (:mod-items registry) update mod-id assoc item-id item))

(defn register-mod-tile-entity
  "Register a tile entity with a specific mod ID"
  [registry mod-id te-id te]
  (register-tile-entity registry te-id te)
  (swap! (:mod-tile-entities registry) update mod-id assoc te-id te))

(defn get-all-mod-blocks
  "Get all blocks registered for all mods"
  [registry]
  (mapcat (fn [[mod-id blocks]] 
            (map (fn [[block-id block]] 
                  {:mod-id mod-id :block-id block-id :block block}) 
                 blocks))
         @(:mod-blocks registry)))

(defn get-all-mod-items
  "Get all items registered for all mods"
  [registry]
  (mapcat (fn [[mod-id items]]
            (map (fn [[item-id item]]
                  {:mod-id mod-id :item-id item-id :item item})
                 items))
         @(:mod-items registry)))