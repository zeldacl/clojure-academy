(ns cn.academy.block.multiblock.example.energy-port
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.interaction :as interaction]
            [cn.academy.block.multiblock.capabilities :as caps]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.util Direction]
           [net.minecraftforge.common.capabilities Capability CapabilityInject]
           [net.minecraftforge.energy IEnergyStorage]))

(defrecord EnergyPortBlock []
  block-api/IBlock
  (on-placed [_ world pos state player hand]
    (when-let [tile (block-api/get-tile-entity world pos)]
      (block-api/mark-dirty! tile)))
  
  (on-broken [_ world pos state]
    (interaction/handle-block-broken world pos))
  
  (has-tile-entity? [_] true))

(defrecord EnergyPortTile [state-atom energy-handler]
  base/IMultiblockMember
  (get-controller [_]
    (:controller @state-atom))
  
  (set-controller [this controller]
    (swap! state-atom assoc :controller controller)
    (block-api/mark-dirty! this))
  
  (can-connect? [_ other]
    (contains? #{"controller" "casing"}
              (base/get-member-type other)))
  
  (get-member-type [_]
    "energy_port")
  
  block-api/ITileEntity
  (load-data [_ data]
    (reset! state-atom {:controller (:controller data)}))
  
  (save-data [_]
    @state-atom)
  
  (get-capabilities [_]
    {IEnergyStorage energy-handler}))