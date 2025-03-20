(ns cn.academy.block.block.energy-generator
  (:require [cn.academy.block.registry :as reg]
            [mcmod.protocols :refer :all]
            [cn.academy.blocks.helpers :refer [create-block-base]]))

(defrecord EnergyGeneratorTile [energy max-energy]
  ITileEntity
  (tick [this]
    (swap! energy #(min (+ % 10) @max-energy)))
  
  (save [this]
    {:energy @energy
     :max-energy @max-energy})
  
  (load [this data]
    (reset! energy (:energy data))
    (reset! max-energy (:max-energy data)))

  (get-update-packet [this]
    {:energy @energy})
  
  (handle-update-packet [this packet]
    (reset! energy (:energy packet))))

(defn create-energy-generator []
  (let [block-properties {:material :iron
                         :hardness 3.5
                         :resistance 17.5
                         :light-level 7
                         :has-tile-entity true
                         :on-activated (fn [pos data]
                                       (let [tile (.getTileEntity (:world data) pos)]
                                         (when tile
                                           (prn "Energy: " @(:energy tile)))))}]
    (reg/create-block "energy_generator" block-properties)))