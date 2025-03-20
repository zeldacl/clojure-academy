(ns cn.academy.block.multiblock.machine-state
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.api.block :as block-api]))

(defprotocol IMachineState
  "Protocol for managing machine state"
  (is-active? [this] "Check if machine is active")
  (set-active! [this active] "Set machine active state")
  (get-energy [this] "Get stored energy")
  (add-energy! [this amount] "Add energy to machine")
  (use-energy! [this amount] "Use energy from machine")
  (can-work? [this] "Check if machine can operate")
  (tick-machine! [this] "Perform machine tick"))

(defrecord MultiblockMachineState [state-atom max-energy work-energy-rate]
  IMachineState
  (is-active? [_]
    (:active @state-atom))
  
  (set-active! [_ active]
    (swap! state-atom assoc :active active))
  
  (get-energy [_]
    (:energy @state-atom))
  
  (add-energy! [_ amount]
    (swap! state-atom update :energy 
           #(min (+ % amount) max-energy)))
  
  (use-energy! [_ amount]
    (when (>= (:energy @state-atom) amount)
      (swap! state-atom update :energy - amount)
      true))
  
  (can-work? [this]
    (and (is-active? this)
         (>= (get-energy this) work-energy-rate)))
  
  (tick-machine! [this]
    (when (can-work? this)
      (use-energy! this work-energy-rate)
      true))

  base/IMultiblockMember
  (get-multiblock-data [_]
    {:energy (get-energy this)
     :active (is-active? this)})
  
  (load-data [_ data]
    (reset! state-atom 
            {:energy (or (:energy data) 0)
             :active (or (:active data) false)}))
  
  (save-data [_]
    @state-atom))