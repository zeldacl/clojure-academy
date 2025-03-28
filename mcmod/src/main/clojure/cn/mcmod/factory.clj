(ns cn.mcmod.factory
  (:require [cn.mcmod.protocols :refer :all]))

(defn create-block
  "Create a block implementation with given properties"
  [props]
  (reify IBlock
    (get-position [this] (:position props))
    (get-properties [this] props)
    (get-material [this] (:material props))
    (get-hardness [this] (:hardness props 3.0))
    (get-resistance [this] (:resistance props 3.0))
    (get-light-level [this] (:light-level props 0))
    (on-activated [this world pos player hand]
      (when-let [handler (:on-activated props)]
        (handler world pos player hand)))
    (on-placed [this world pos placer]
      (when-let [handler (:on-placed props)]
        (handler world pos placer)))
    (on-broken [this world pos]
      (when-let [handler (:on-broken props)]
        (handler world pos)))
    (on-removed [this pos]
      (when-let [handler (:on-removed props)]
        (handler pos)))
    (get-render-type [this]
      (:render-type props :solid))
    (is-opaque? [this]
      (:opaque? props true))))

(defn create-item
  "Create an item implementation with given properties"
  [props]
  (reify IItem
    (get-item-properties [_] props)
    (get-max-stack-size [_] (:max-stack-size props 64))
    (get-max-damage [_] (:max-damage props 0))
    (on-item-use [_ context]
      (when-let [handler (:on-use props)]
        (handler context)))
    (on-item-right-click [_ context]
      (when-let [handler (:on-right-click props)]
        (handler context)))
    (get-use-duration [_]
      (:use-duration props 0))
    (get-creative-tab [_]
      (:creative-tab props))
    (has-effect? [_ stack]
      (:has-effect? props false))))

(defn create-tile-entity
  "Create a tile entity implementation with given properties"
  [props]
  (let [state (atom {})]
    (reify IBlockEntity
      (load-data [_ tag]
        (when-let [handler (:on-load props)]
          (handler state tag)))
      
      (save-data [_]
        (merge @state (:additional-data props)))
      
      (get-update-packet [_]
        (when-let [handler (:get-update props)]
          (handler @state)))
      
      (handle-update-packet [_ packet]
        (when-let [handler (:handle-update props)]
          (handler state packet)))
      
      (on-load [_]
        (when-let [handler (:on-load props)]
          (handler state)))
      
      (on-unload [_]
        (when-let [handler (:on-unload props)]
          (handler state)))
      
      (get-capability [_ cap side]
        (when-let [caps (:capabilities props)]
          (get-in caps [cap side])))
          
      (read-from-nbt [_ nbt]
        (when-let [handler (:read-nbt props)]
          (handler state nbt)))
          
      (write-to-nbt [_ nbt]
        (when-let [handler (:write-nbt props)]
          (handler state nbt)))
          
      (mark-dirty [_]
        (when-let [handler (:mark-dirty props)]
          (handler state)))
          
      (get-capabilities [_ side]
        (when-let [caps (:capabilities props)]
          (get caps side)))
          
      (get-position [_]
        (:position props))
        
      (get-block-type [_]
        (:block-type props))
        
      (get-block-state [_]
        (:block-state props)))))

(defn create-inventory
  "Create an inventory implementation with given size"
  [size]
  (let [slots (atom (vec (repeat size nil)))]
    (reify IInventory
      (get-size [_] size)

      (get-stack-in-slot [_ slot]
        (get @slots slot))

      (remove-stack-in-slot [_ slot amount]
        (when-let [stack (get @slots slot)]
          (let [to-remove (min amount (:count stack))
                new-count (- (:count stack) to-remove)
                new-stack (if (pos? new-count)
                           (assoc stack :count new-count)
                           nil)]
            (swap! slots assoc slot new-stack)
            (assoc stack :count to-remove))))

      (set-inventory-slot [_ slot stack]
        (swap! slots assoc slot stack))

      (is-empty? [_]
        (every? nil? @slots))

      (mark-dirty [_]))))
