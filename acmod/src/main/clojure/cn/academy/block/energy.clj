(ns cn.academy.block.energy
  (:require [clojure.tools.logging :as log]))

;; Energy capability implementation
(defn create-energy-storage [state-atom config]
  (reify mcmod.energy/IEnergyStorage
    (receive-energy [_ amount simulate]
      (let [capacity (get config :energy-capacity 0)
            current (get @state-atom :energy 0)
            space (- capacity current)
            transfer (min amount space)]
        (when-not simulate
          (swap! state-atom update :energy + transfer))
        transfer))

    (extract-energy [_ amount simulate]
      (let [current (get @state-atom :energy 0)
            transfer (min amount current)]
        (when-not simulate
          (swap! state-atom update :energy - transfer))
        transfer))

    (get-energy-stored [_]
      (get @state-atom :energy 0))

    (get-max-energy-stored [_]
      (get config :energy-capacity 0))))

;; Energy producer implementation  
(defn create-energy-producer [config]
  (reify mcmod.energy/IEnergyProducer
    (get-energy-output [_]
      (* (get config :max-output 0)
         (get config :base-efficiency 1.0)))
         
    (can-output? [_] true)))

;; Energy consumer implementation
(defn create-energy-consumer [config]
  (reify mcmod.energy/IEnergyConsumer
    (get-energy-required [_]
      (get config :energy-per-tick 0))
      
    (can-receive? [_] true)))