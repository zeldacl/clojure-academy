(ns cn.academy.block.multiblock.machine-state
  (:require [mcmod.energy :as energy]))

(defprotocol IMachineState
  "Protocol for machine state management"
  (is-active? [this] "Check if machine is active")
  (set-active! [this active] "Set machine active state")
  (can-work? [this] "Check if machine can work")
  (consume-energy! [this amount] "Try to consume energy"))

(defn has-energy?
  "Check if machine has enough energy"
  [machine amount]
  (>= (energy/get-stored-energy machine) amount))

(defn get-energy
  "Get stored energy in machine"
  [machine]
  (energy/get-stored-energy machine))

(defn use-energy!
  "Try to use energy from machine"
  [machine amount]
  (when (has-energy? machine amount)
    (energy/extract-energy! machine amount true)
    true))

(defprotocol IMachineProgress 
  "Protocol for machine progress tracking"
  (get-progress [this] "Get current progress")
  (set-progress! [this progress] "Set current progress")
  (get-max-progress [this] "Get maximum progress"))

(defrecord MachineState [state-atom max-energy energy-use-rate]
  IMachineState
  (is-active? [_]
    (get-in @state-atom [:active] false))
  
  (set-active! [_ active]
    (swap! state-atom assoc :active active))
  
  (can-work? [this]
    (and (is-active? this)
         (has-energy? this energy-use-rate)))
  
  (consume-energy! [this amount]
    (use-energy! this amount))
  
  energy/IEnergyStorage
  (get-stored-energy [_]
    (get-in @state-atom [:energy] 0))
  
  (get-max-energy [_]
    max-energy)
  
  (receive-energy! [_ amount simulate]
    (let [space (- max-energy (get-in @state-atom [:energy] 0))
          transfer (min amount space)]
      (when-not simulate
        (swap! state-atom update :energy 
               #(min (+ (or % 0) transfer) max-energy)))
      transfer))
  
  (extract-energy! [_ amount simulate]
    (let [stored (get-in @state-atom [:energy] 0)
          transfer (min amount stored)]
      (when-not simulate
        (swap! state-atom update :energy 
               #(max (- (or % 0) transfer) 0)))
      transfer)))

(defn create-machine-state
  "Create a new machine state instance"
  ([max-energy]
   (create-machine-state max-energy 20))
  ([max-energy energy-use-rate]
   (->MachineState (atom {:energy 0
                         :active false})
                   max-energy
                   energy-use-rate)))