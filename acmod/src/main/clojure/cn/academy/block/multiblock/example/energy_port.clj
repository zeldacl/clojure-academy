(ns cn.academy.block.multiblock.example.energy-port
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-member :as member]
            [cn.academy.energy.api :as energy]
            [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

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
    {energy/ENERGY_CAPABILITY energy-handler}))

(defrecord MultiblockEnergyHandler [multiblock max-transfer]
  energy/IEnergyStorage
  (receive-energy [_ amount simulate]
    (if-let [controller (base/get-controller multiblock)]
      (let [space (- (energy/get-max-energy controller)
                     (energy/get-stored-energy controller))
            transfer (min amount max-transfer space)]
        (when-not simulate
          (energy/add-energy! controller transfer))
        transfer)
      0))
  
  (extract-energy [_ amount simulate]
    (if-let [controller (base/get-controller multiblock)]
      (let [stored (energy/get-stored-energy controller)
            transfer (min amount max-transfer stored)]
        (when-not simulate
          (energy/extract-energy! controller transfer))
        transfer)
      0))
  
  (get-energy-stored [_]
    (if-let [controller (base/get-controller multiblock)]
      (energy/get-stored-energy controller)
      0))
  
  (get-max-energy-stored [_]
    (if-let [controller (base/get-controller multiblock)]
      (energy/get-max-energy controller)
      0))
  
  (can-receive [_] true)
  (can-extract [_] true))

(defn create-energy-handler [multiblock max-transfer]
  (->MultiblockEnergyHandler multiblock max-transfer))

(defn create-energy-port []
  (->EnergyPortTile 
    (atom {:controller nil})
    (create-energy-handler nil 1000)))