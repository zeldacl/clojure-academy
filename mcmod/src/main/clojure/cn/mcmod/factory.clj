(ns mcmod.factory
  (:require [mcmod.protocols :refer :all]))

(defn create-block
  "Create a block implementation with given properties"
  [props]
  (reify IBlock
    (get-properties [_] props)
    (get-material [_] (:material props))
    (get-hardness [_] (:hardness props 3.0))
    (get-resistance [_] (:resistance props 3.0))
    (get-light-level [_] (:light-level props 0))
    (on-activated [_ pos data] 
      (when-let [handler (:on-activated props)]
        (handler pos data)))
    (on-placed [_ pos data]
      (when-let [handler (:on-placed props)]
        (handler pos data)))
    (on-removed [_ pos]
      (when-let [handler (:on-removed props)]
        (handler pos)))
    (get-render-type [_]
      (:render-type props :solid))
    (is-opaque? [_]
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
    (reify ITileEntity
      (tick [_]
        (when-let [handler (:on-tick props)]
          (handler state)))
      
      (save [_]
        (merge @state (:additional-data props)))
      
      (load [_ data]
        (reset! state data))
      
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
          (get-in caps [cap side]))))))

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