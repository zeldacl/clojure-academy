(ns cn.academy.block.component)

;; Factory functions for creating components
(defrecord BlockComponent [id type state-atom]
  mcmod.protocols/IBlockComponent  ; Use protocol from mcmod
  (update! [_] 
    nil)
  
  (get-capability [_ _ _] 
    nil)
  
  (serialize [_]
    @state-atom)
  
  (deserialize! [_ data]
    (reset! state-atom data))
  
  (get-state [_]
    @state-atom))

(defrecord InventoryComponent [size inventory]
  mcmod.protocols/IBlockComponent
  (update! [_] nil)
  (get-capability [_ type _] 
    (when (= type :inventory) this))
  (serialize [_] 
    {:inventory @inventory})
  (deserialize! [_ data]
    (reset! inventory (:inventory data)))
  (get-state [_]
    {:size size :inventory @inventory})

  mcmod.protocols/IInventory  
  (get-inventory [_] @inventory)
  (get-slot [_ slot] 
    (get @inventory slot))
  (set-slot! [_ slot item]
    (swap! inventory assoc slot item))
  (get-size [_] size))

(defrecord EnergyComponent [capacity energy]
  mcmod.protocols/IBlockComponent
  (update! [_] nil)
  (get-capability [_ type _]
    (when (= type :energy) this))
  (serialize [_]
    {:energy @energy})
  (deserialize! [_ data]
    (reset! energy (:energy data)))
  (get-state [_]
    {:capacity capacity :energy @energy})
  
  mcmod.protocols/IEnergyStorage
  (get-energy [_] @energy)
  (get-capacity [_] capacity)
  (receive-energy [_ amount simulate?]
    (let [space (- capacity @energy)
          accept (min amount space)]
      (when (and (pos? accept) (not simulate?))
        (swap! energy + accept))
      accept))
  (extract-energy [_ amount simulate?]
    (let [available @energy
          extract (min amount available)]
      (when (and (pos? extract) (not simulate?))
        (swap! energy - extract))
      extract)))

;; Factory functions
(defn create-component [id type & {:as state}]
  (->BlockComponent id type (atom state)))

(defn create-inventory [size]
  (->InventoryComponent size (atom {})))

(defn create-energy [capacity]
  (->EnergyComponent capacity (atom 0)))