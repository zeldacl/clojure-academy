(ns cn.academy.block.multiblock.example.registry
  (:require [cn.academy.block.multiblock.example.processing-machine :as machine]
            [cn.academy.block.multiblock.example.controller-block :as controller]
            [cn.academy.block.multiblock.example.casing-block :as casing]
            [cn.academy.block.multiblock.example.energy-port :as energy]
            [cn.academy.api.registry :as registry]))

(defn register-blocks []
  (let [properties (registry/block-properties
                    {:material :metal
                     :hardness 3.0
                     :resistance 3.0})]
    
    ;; Register controller block
    (registry/register-block 
      (str machine/MACHINE_ID "_controller")
      #(controller/->ControllerBlock)
      #(controller/->ControllerTile 
         (machine/create-machine)
         (machine/create-renderer %))
      properties)
    
    ;; Register casing block
    (registry/register-block
      (str machine/MACHINE_ID "_casing")
      #(casing/->CasingBlock)
      #(casing/->CasingTile (atom {}))
      properties)
    
    ;; Register energy port block
    (registry/register-block
      (str machine/MACHINE_ID "_energy_port") 
      #(energy/->EnergyPortBlock)
      #(energy/->EnergyPortTile 
         (atom {})
         (machine/create-energy-handler %))
      properties)))