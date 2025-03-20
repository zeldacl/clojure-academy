(ns cn.academy.block.component)

(defprotocol IBlockComponent
  "Core component protocol for all block-related components"
  (update! [this] "Update component state each tick")
  (get-capability [this type side] "Get capability for given type and side")
  (serialize [this] "Convert component state to NBT data")
  (deserialize! [this data] "Load component state from NBT data")
  (get-state [this] "Get current component state"))

(defrecord BlockComponent [id type state-atom]
  IBlockComponent
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

(defprotocol IInventoryComponent
  "Component for blocks with inventories"
  (get-inventory [this])
  (get-slot [this slot])
  (set-slot! [this slot item])
  (get-size [this]))

(defrecord InventoryComponent [size inventory]
  IBlockComponent
  (update! [_] nil)
  (get-capability [_ type _] 
    (when (= type :inventory) this))
  (serialize [_] 
    {:inventory @inventory})
  (deserialize! [_ data]
    (reset! inventory (:inventory data)))
  (get-state [_]
    {:size size :inventory @inventory})

  IInventoryComponent  
  (get-inventory [_] @inventory)
  (get-slot [_ slot] 
    (get @inventory slot))
  (set-slot! [_ slot item]
    (swap! inventory assoc slot item))
  (get-size [_] size))

(defprotocol IEnergyComponent
  "Component for blocks that handle energy"
  (get-energy [this])
  (get-capacity [this])
  (receive-energy [this amount simulate?])
  (extract-energy [this amount simulate?]))

(defrecord EnergyComponent [capacity energy]
  IBlockComponent
  (update! [_] nil)
  (get-capability [_ type _]
    (when (= type :energy) this))
  (serialize [_]
    {:energy @energy})
  (deserialize! [_ data]
    (reset! energy (:energy data)))
  (get-state [_]
    {:capacity capacity :energy @energy})
  
  IEnergyComponent
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