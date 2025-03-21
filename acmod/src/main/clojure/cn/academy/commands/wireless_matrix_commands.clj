(ns cn.academy.commands.wireless-matrix-commands
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.capabilities :as cap]
            [mcmod.world :as world]
            [mcmod.position :as position]
            [mcmod.logging :as log]))

(defrecord CheckEnergyCommand []
  ICommand
  (get-name [this] "check-energy")
  
  (get-permission-level [this] 0)
  
  (execute [this context args]
    (let [source (:source context)
          world-obj (world/get-world source)
          pos (position/get-block-pos source)
          tile (world/get-tile-entity world-obj pos)]
      (if (and tile (cap/has-capability? tile "forge:energy" nil))
        (let [energy-storage (cap/get-capability tile "forge:energy" nil)
              current (cap/get-energy-stored energy-storage)
              max (cap/get-max-energy-stored energy-storage)]
          (world/send-success source 
                       (str "Energy: " current "/" max " FE")
                       true)
          1)
        (do
          (world/send-failure source "No energy storage at this position")
          0)))))

(defrecord SetEnergyCommand []
  ICommand
  (get-name [this] "set-energy")
  
  (get-permission-level [this] 2)
  
  (execute [this context args]
    (let [source (:source context)
          world-obj (world/get-world source)
          pos (position/get-block-pos source)
          tile (world/get-tile-entity world-obj pos)
          amount (get args :amount 0)]
      (if (and tile (cap/has-capability? tile "forge:energy" nil))
        (let [energy-storage (cap/get-capability tile "forge:energy" nil)]
          (cap/receive-energy energy-storage amount false)
          (world/send-success source 
                       (str "Set energy to " amount " FE")
                       true)
          1)
        (do
          (world/send-failure source "No energy storage at this position")
          0)))))