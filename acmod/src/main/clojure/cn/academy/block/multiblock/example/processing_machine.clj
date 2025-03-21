(ns cn.academy.block.multiblock.example.processing-machine
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [mcmod.protocols :refer [IEnergyStorage IRecipeProcessor IDirectionalMachine]]
            [mcmod.direction :as dir]))

(def ^:const MACHINE_ID "processing_machine")
(def ^:const MAX_ENERGY 50000)
(def ^:const ENERGY_USE_RATE 20)

;; Machine implementation
(defrecord ProcessingMachine [state-atom]
  base/IMultiblockMember
  (get-member-type [_] "machine")
  (can-connect? [_ other] true)
  
  IEnergyStorage
  (get-energy-stored [_]
    (get-in @state-atom [:energy] 0))
  
  (get-max-energy-stored [_]
    MAX_ENERGY)
  
  (receive-energy [_ amount simulate]
    (let [current (get-in @state-atom [:energy] 0)
          space (- MAX_ENERGY current)
          accepted (min amount space)]
      (when-not simulate
        (swap! state-atom update :energy + accepted))
      accepted))
  
  (extract-energy [_ amount simulate]
    (let [current (get-in @state-atom [:energy] 0)
          extracted (min amount current)]
      (when-not simulate
        (swap! state-atom update :energy - extracted))
      extracted))
  
  IRecipeProcessor
  (can-process? [_ recipe]
    (and (:active @state-atom)
         (>= (get-in @state-atom [:energy] 0) (:energy-required recipe))))
         
  (start-processing [_ recipe]
    (swap! state-atom assoc 
           :current-recipe recipe
           :progress 0))
           
  (get-progress [_]
    (get-in @state-atom [:progress] 0))
  
  (update-progress! [this delta]
    (let [recipe (get-in @state-atom [:current-recipe])]
      (when (and recipe (can-process? this recipe))
        (swap! state-atom update :energy - ENERGY_USE_RATE)
        (swap! state-atom update :progress + delta))))
  
  IDirectionalMachine
  (get-facing [_]
    (get-in @state-atom [:facing] (dir/north)))
    
  (set-facing! [_ facing]
    (swap! state-atom assoc :facing facing)))

;; Factory functions
(defn create-machine []
  (->ProcessingMachine 
    (atom {:energy 0
           :active false
           :facing (dir/north)
           :current-recipe nil
           :progress 0})))