(ns cn.academy.blocks
  (:require [cn.academy.registry :as registry]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.block.material Material]))

;; Core block definitions
(def core-blocks 
  {"crystal_ore" {:material Material/ROCK
                  :hardness 3.0
                  :resistance 5.0
                  :light-level 0.0
                  :harvest-level 2}

   "constraint_metal" {:material Material/IRON
                      :hardness 4.0
                      :resistance 10.0
                      :harvest-level 2}

   "machine_frame" {:material Material/IRON
                   :hardness 5.0
                   :resistance 10.0
                   :harvest-level 1}

   "phase_gen" {:material Material/IRON
                :hardness 3.5
                :resistance 8.0
                :has-tile-entity true
                :harvest-level 1}

   "matrix" {:material Material/IRON
            :hardness 4.0
            :resistance 8.0
            :has-tile-entity true
            :harvest-level 1}

   "node_basic" {:material Material/IRON
                :hardness 3.0
                :resistance 5.0
                :has-tile-entity true
                :harvest-level 1}})

;; Function to initialize block registrations
(defn register-blocks! [provider]
  (doseq [[block-id props] core-blocks]
    (registry/register-block! provider block-id props)))

;; Define blocks
(def ability-interferer (block-api/create-block "ability_interferer"))
(def cat-engine (block-api/create-block "cat_engine"))
(def constraint-metal (block-api/create-block "constraint_metal" {:hardness 4.0 :harvest-level 1}))
(def crystal-ore (block-api/create-block "crystal_ore" {:hardness 3.0 :harvest-level 2}))
(def dev-advanced (block-api/create-block "dev_advanced"))
(def dev-normal (block-api/create-block "dev_normal"))
(def imag-fusor (block-api/create-block "imag_fusor"))
(def imag-phase (block-api/create-block "imag_phase"))
(def imagsil-ore (block-api/create-block "imagsil_ore" {:hardness 3.75 :harvest-level 2}))
(def machine-frame (block-api/create-block "machine_frame"))
(def matrix (block-api/create-block "matrix"))
(def metal-former (block-api/create-block "metal_former"))
(def node-advanced (block-api/create-block "node_advanced"))
(def node-basic (block-api/create-block "node_basic"))
(def node-standard (block-api/create-block "node_standard"))
(def phase-gen (block-api/create-block "phase_gen"))
(def reso-ore (block-api/create-block "reso_ore" {:hardness 3.0 :harvest-level 2}))
(def solar-gen (block-api/create-block "solar_gen"))
(def windgen-base (block-api/create-block "windgen_base"))
(def windgen-main (block-api/create-block "windgen_main"))
(def windgen-pillar (block-api/create-block "windgen_pillar"))

;; Define item blocks
(def item-ability-interferer (block-api/create-item-block ability-interferer))
(def item-cat-engine (block-api/create-item-block cat-engine))
(def item-constraint-metal (block-api/create-item-block constraint-metal))
(def item-crystal-ore (block-api/create-item-block crystal-ore))
(def item-dev-advanced (block-api/create-item-block dev-advanced))
(def item-dev-normal (block-api/create-item-block dev-normal))
(def item-imag-fusor (block-api/create-item-block imag-fusor))
(def item-imag-phase (block-api/create-item-block imag-phase))
(def item-imagsil-ore (block-api/create-item-block imagsil-ore))
(def item-machine-frame (block-api/create-item-block machine-frame))
(def item-matrix (block-api/create-item-block matrix))
(def item-metal-former (block-api/create-item-block metal-former))
(def item-node-advanced (block-api/create-item-block node-advanced))
(def item-node-basic (block-api/create-item-block node-basic))
(def item-node-standard (block-api/create-item-block node-standard))
(def item-phase-gen (block-api/create-item-block phase-gen))
(def item-reso-ore (block-api/create-item-block reso-ore))
(def item-solar-gen (block-api/create-item-block solar-gen))
(def item-windgen-base (block-api/create-item-block windgen-base))
(def item-windgen-main (block-api/create-item-block windgen-main))
(def item-windgen-pillar (block-api/create-item-block windgen-pillar))

;; Register blocks and items
(defn register-blocks [event]
  (doseq [block [ability-interferer cat-engine constraint-metal crystal-ore dev-advanced dev-normal imag-fusor imag-phase imagsil-ore machine-frame matrix metal-former node-advanced node-basic node-standard phase-gen reso-ore solar-gen windgen-base windgen-main windgen-pillar]]
    (block-api/register-block! block event)))

(defn register-items [event]
  (doseq [item [item-ability-interferer item-cat-engine item-constraint-metal item-crystal-ore item-dev-advanced item-dev-normal item-imag-fusor item-imag-phase item-imagsil-ore item-machine-frame item-matrix item-metal-former item-node-advanced item-node-basic item-node-standard item-phase-gen item-reso-ore item-solar-gen item-windgen-base item-windgen-main item-windgen-pillar]]
    (block-api/register-item! item event)))

;; Register event handlers
(block-api/register-event-handler! :register-blocks register-blocks)
(block-api/register-event-handler! :register-items register-items)