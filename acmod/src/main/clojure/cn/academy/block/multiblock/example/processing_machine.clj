(ns cn.academy.block.multiblock.example.processing-machine
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.block.multiblock.recipes :as recipes]
            [cn.academy.block.multiblock.energy :as energy]
            [mcmod.direction :as dir]))

(def MACHINE_ID "processing_machine")
(def MAX_ENERGY 50000)
(def ENERGY_USE_RATE 20)

;; Structure validation
(def structure-requirements
  {:controller 1
   :casing 8
   :energy_port 1})

(defn validate-structure [blocks]
  (validation/check-block-types blocks
                               {"controller" 1
                                "casing" 8
                                "energy_port" 1}))

(def machine-validator
  (validation/create-validator structure-requirements validate-structure))

;; Machine implementation
(defrecord ProcessingMachine [state-atom energy-handler recipe-handler]
  base/IMultiblockMember
  (get-member-type [_] "machine")
  (can-connect? [_ other] true)
  
  machine/IMachineState
  (is-active? [_] 
    (get-in @state-atom [:active]))
  (set-active! [_ active] 
    (swap! state-atom assoc :active active))
  (can-work? [this]
    (and (is-active? this)
         (> (energy/get-stored-energy energy-handler) 0)))
  
  energy/IEnergyHandler
  (get-stored-energy [_]
    (get-in @state-atom [:energy] 0))
  (get-max-energy [_]
    energy/MAX_ENERGY)
  (add-energy! [_ amount]
    (swap! state-atom update :energy 
           #(min (+ (or % 0) amount) 
                 energy/MAX_ENERGY)))
  (extract-energy! [_ amount]
    (swap! state-atom update :energy 
           #(max (- (or % 0) amount) 0)))
  
  recipes/IRecipeHandler
  (can-process? [_ recipe]
    (and (machine/is-active? this)
         (>= (energy/get-stored-energy energy-handler)
             (:energy-required recipe))))
  (start-recipe! [_ recipe]
    (swap! state-atom assoc 
           :current-recipe recipe
           :progress 0))
  (get-progress [_]
    (get-in @state-atom [:progress] 0))
  
  base/IDirectionalMachine
  (get-facing [_]
    (get-in @state-atom [:facing] (dir/north)))
  (set-facing! [_ facing]
    (swap! state-atom assoc :facing facing))
  
  Object
  (toString [_]
    (str "ProcessingMachine[energy=" (energy/get-stored-energy energy-handler)
         ", active=" (machine/is-active? this)
         ", facing=" (base/get-facing this) "]")))

;; Factory functions
(defn create-machine
  "Create a new processing machine"
  []
  (->ProcessingMachine (atom {:energy 0
                             :active false
                             :facing (dir/north)})
                      (energy/create-handler)
                      (recipes/create-handler)))

(defn create-energy-handler [machine]
  (caps/->MultiblockEnergyHandler machine 1000))

(defn create-gui-handler [machine recipe-handler]
  (gui/->MultiblockGuiHandler machine recipe-handler))

(defn create-renderer [machine]
  (render/->MultiblockStructureRenderer 
    (:state-atom machine)
    machine-validator))