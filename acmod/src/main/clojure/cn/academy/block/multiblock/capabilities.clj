(ns cn.academy.block.multiblock.capabilities
  (:require [cn.academy.block.multiblock.machine-state :as machine]
            [mcmod.protocols.energy :refer [IEnergyHandler]]))

(defrecord MultiblockEnergyHandler [machine max-transfer]
  IEnergyHandler
  (receive-energy [_ amount simulate]
    (let [space-left (- (:max-energy machine)
                       (machine/get-energy machine))
          amount (min amount max-transfer space-left)]
      (when (and (pos? amount) (not simulate))
        (machine/add-energy! machine amount))
      amount))
  
  (extract-energy [_ amount simulate]
    (let [energy (machine/get-energy machine)
          amount (min amount max-transfer energy)]
      (when (and (pos? amount) (not simulate))
        (machine/use-energy! machine amount))
      amount))
  
  (get-energy-stored [_]
    (machine/get-energy machine))
  
  (get-max-energy-stored [_]
    (:max-energy machine))
  
  (can-extract? [_]
    true)
  
  (can-receive? [_]
    true))