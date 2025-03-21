(ns cn.academy.tech-system.energy-system.network.core
  (:require [mcmod.protocols :refer [IEnergyNetwork IEnergyNode IEnergyStorage]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Copy network.clj content with updated namespace references
// ...existing content from energy/network.clj...