(ns cn.academy.block.tileentity.tile-cat-engine
  (:require [cn.academy.protocols.block :as block-api]
            [cn.academy.api.energy :as energy-api]
            [cn.academy.energy.api.wireless :as wireless]
            [mcmod.nbt :as nbt]
            [clojure.tools.logging :as log]))

;; Constants
(def ^:const ENERGY_MAX 100000.0)
(def ^:const ENERGY_GEN_PER_TICK 5.0)
(def ^:const ENERGY_BANDWIDTH 200.0)

;; State record
(defrecord CatEngineState [energy rotation last-render this-tick-gen active])

(defn create-initial-state []
  (->CatEngineState 0.0 0.0 0 0.0 false))

(defn tick-engine [state]
  (if (:active state)
    (-> state
        (update :energy #(min (+ % ENERGY_GEN_PER_TICK) ENERGY_MAX))
        (assoc :this-tick-gen ENERGY_GEN_PER_TICK))
    (assoc state :this-tick-gen 0.0)))

(defprotocol ICatEngineTile
  (tick [this])
  (get-energy [this])
  (set-energy! [this amount])
  (get-rotation [this])
  (set-rotation! [this rot])
  (get-last-render [this])
  (set-last-render! [this time])
  (get-this-tick-gen [this])
  (set-this-tick-gen! [this gen])
  (get-active [this])
  (set-active! [this active]))

(defn create []
  (let [state-atom (atom (create-initial-state))]
    (reify 
      ICatEngineTile
      (tick [_]
        (swap! state-atom tick-engine))
      
      (get-energy [_]
        (:energy @state-atom))
      
      (set-energy! [_ amount]
        (swap! state-atom assoc :energy amount))
      
      (get-rotation [_]
        (:rotation @state-atom))
      
      (set-rotation! [_ rot]
        (swap! state-atom assoc :rotation rot))
      
      (get-last-render [_]
        (:last-render @state-atom))
      
      (set-last-render! [_ time]
        (swap! state-atom assoc :last-render time))
      
      (get-this-tick-gen [_]
        (:this-tick-gen @state-atom))
      
      (set-this-tick-gen! [_ gen]
        (swap! state-atom assoc :this-tick-gen gen))
      
      (get-active [_]
        (:active @state-atom))
      
      (set-active! [_ active]
        (swap! state-atom assoc :active active))
      
      block-api/INBTSerializable
      (write-to-nbt [_ tag]
        (let [state @state-atom]
          (doto tag
            (nbt/put-double "energy" (:energy state))
            (nbt/put-double "rotation" (:rotation state))
            (nbt/put-long "lastRender" (:last-render state))
            (nbt/put-double "thisTickGen" (:this-tick-gen state))
            (nbt/put-boolean "active" (:active state)))))
      
      (read-from-nbt [_ tag]
        (reset! state-atom
          (->CatEngineState
            (nbt/get-double tag "energy")
            (nbt/get-double tag "rotation")
            (nbt/get-long tag "lastRender")
            (nbt/get-double tag "thisTickGen")
            (nbt/get-boolean tag "active"))))
      
      wireless/IWirelessGenerator
      (generate-energy [this amount]
        (let [current (get-energy this)
              generated (min amount (- ENERGY_MAX current))]
          (when (pos? generated)
            (set-energy! this (+ current generated)))
          generated))
      
      (get-generation-rate [_]
        ENERGY_GEN_PER_TICK)))))