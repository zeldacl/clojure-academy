(ns cn.academy.block.multiblock.example.processing-machine
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.machine-state :as machine]
            [cn.academy.block.multiblock.recipes :as recipes]
            [cn.academy.block.multiblock.network-sync :as network]
            [cn.academy.block.multiblock.capabilities :as caps]
            [cn.academy.block.multiblock.validation :as validation]
            [cn.academy.block.multiblock.gui :as gui]
            [cn.academy.block.multiblock.render :as render]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.util Direction]))

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
(defrecord ProcessingMachine [state-atom]
  base/IMultiblockController
  (is-complete? [this]
    (validation/validate machine-validator 
                        (base/get-blocks this)))

  machine/IMachineState
  (is-active? [_]
    (:active @state-atom))
  
  (set-active! [_ active]
    (swap! state-atom assoc :active active))
  
  (get-energy [_]
    (:energy @state-atom))
  
  (add-energy! [_ amount]
    (swap! state-atom update :energy 
           #(min (+ % amount) MAX_ENERGY)))
  
  (use-energy! [_ amount]
    (when (>= (:energy @state-atom) amount)
      (swap! state-atom update :energy - amount)
      true))
  
  (can-work? [this]
    (and (machine/is-active? this)
         (>= (machine/get-energy this) ENERGY_USE_RATE)))
  
  (tick-machine! [this]
    (when (can-work? this)
      (machine/use-energy! this ENERGY_USE_RATE)
      true))

  network/INetworkSync
  (get-sync-data [_]
    {:energy (:energy @state-atom)
     :active (:active @state-atom)})
  
  (handle-sync-data [_ data]
    (swap! state-atom merge data))
  
  (should-sync? [_]
    true))

;; Factory functions
(defn create-machine []
  (->ProcessingMachine (atom {:energy 0
                             :active false})))

(defn create-energy-handler [machine]
  (caps/->MultiblockEnergyHandler machine 1000))

(defn create-gui-handler [machine recipe-handler]
  (gui/->MultiblockGuiHandler machine recipe-handler))

(defn create-renderer [machine]
  (render/->MultiblockStructureRenderer 
    (:state-atom machine)
    machine-validator)))