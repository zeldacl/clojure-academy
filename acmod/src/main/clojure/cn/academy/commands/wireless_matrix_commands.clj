(ns cn.academy.commands.wireless-matrix-commands
  (:require [mcmod.commands :refer [ICommand]]
            [mcmod.capabilities :as cap]
            [mcmod.logging :as log])
  (:import [net.minecraft.command CommandSource]
           [net.minecraft.util.math BlockPos]))

(defrecord CheckEnergyCommand []
  ICommand
  (get-name [this] "check-energy")
  
  (get-permission-level [this] 0)
  
  (execute [this context args]
    (let [source (.getSource context)
          world (.getLevel source)
          pos (.getBlockPos source)
          tile (.getTileEntity world pos)]
      (if (and tile (cap/has-capability? tile "forge:energy" nil))
        (let [energy-storage (cap/get-capability tile "forge:energy" nil)
              current (cap/get-energy-stored energy-storage)
              max (cap/get-max-energy-stored energy-storage)]
          (.sendSuccess source 
                       (str "Energy: " current "/" max " FE")
                       true)
          1)
        (do
          (.sendFailure source "No energy storage at this position")
          0)))))

(defrecord SetEnergyCommand []
  ICommand
  (get-name [this] "set-energy")
  
  (get-permission-level [this] 2)
  
  (execute [this context args]
    (let [source (.getSource context)
          world (.getLevel source)
          pos (.getBlockPos source)
          tile (.getTileEntity world pos)
          amount (get args :amount 0)]
      (if (and tile (cap/has-capability? tile "forge:energy" nil))
        (let [energy-storage (cap/get-capability tile "forge:energy" nil)]
          (cap/receive-energy energy-storage amount false)
          (.sendSuccess source 
                       (str "Set energy to " amount " FE")
                       true)
          1)
        (do
          (.sendFailure source "No energy storage at this position")
          0)))))