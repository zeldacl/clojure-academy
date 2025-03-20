(ns cn.academy.block.block.machine-block
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.registry :as reg]
            [cn.academy.blocks.helpers :refer [create-block-base]]))

(defrecord MachineTile [power max-power active]
  ITileEntity
  (tick [this]
    (when @active
      (swap! power #(min (+ % 5) @max-power))))
  
  (save [this]
    {:power @power
     :max-power @max-power
     :active @active})
  
  (load [this data]
    (reset! power (:power data))
    (reset! max-power (:max-power data))
    (reset! active (:active data)))
  
  (get-update-packet [this]
    {:power @power
     :active @active})
  
  (handle-update-packet [this packet]
    (reset! power (:power packet))
    (reset! active (:active packet))))

(defrecord MachineBlock [active]
  IBlock
  (get-properties [this]
    {:material :iron
     :hardness 3.5
     :resistance 17.5
     :light-level (if @active 15 0)
     :has-tile-entity true})
  
  (get-material [_] :iron)
  
  (get-hardness [_] 3.5)
  
  (get-resistance [_] 17.5)
  
  (get-light-level [this]
    (if @active 15 0))
  
  (on-activated [this pos data]
    (when-let [tile (.getTileEntity (:world data) pos)]
      (swap! (:active tile) not)))
  
  (on-placed [this pos data]
    (let [tile (->MachineTile (atom 0) (atom 1000) (atom false))]
      (.setTileEntity (:world data) pos tile)))
  
  (on-removed [this pos]
    nil))

(defn register! []
  (let [block (->MachineBlock (atom false))]
    (reg/create-block "machine_block" block)
    (reg/register-tile-entity! "machine_block" :machine 
      (->MachineTile (atom 0) (atom 1000) (atom false)))))