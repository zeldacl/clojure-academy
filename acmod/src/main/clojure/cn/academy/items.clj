(ns cn.academy.items
  (:require [cn.academy.api.item :as item-api]
            [clojure.tools.logging :as log]
            [cn.academy.registry :as registry]
            [cn.academy.core :as core])
  (:import [net.minecraft.item Item]))

;; Core item definitions 
(def core-items
  {"crystal_low" {:max-stack-size 64}
   "crystal_normal" {:max-stack-size 64}
   "crystal_pure" {:max-stack-size 64}
   "data_chip" {:max-stack-size 64}
   "developer_portable" {:max-stack-size 1
                        :max-damage 100}
   "energy_unit" {:max-stack-size 16}
   "calc_chip" {:max-stack-size 64}
   "info_component" {:max-stack-size 64}
   "resonance_component" {:max-stack-size 64}
   "matter_unit" {:max-stack-size 16}
   "matrix_core" {:max-stack-size 1}
   "magnetic_coil" {:max-stack-size 1
                    :max-damage 100}
   "terminal_installer" {:max-stack-size 1}})

;; Generic item constructor
(defn create-item [id props]
  (let [item (proxy [Item] []
               (getMaxStackSize [] (get props :max-stack-size 64))
               (getMaxDamage [] (get props :max-damage 0)))]
    (.setCreativeTab item core/creative-tab)
    (.setRegistryName item (str core/modid ":" id))
    (.setTranslationKey item (str "ac_" id))
    item))

;; Define items
(def app-freq-transmitter (item-api/create-item "app_freq_transmitter"))
(def app-media-player (item-api/create-item "app_media_player"))
(def app-skill-tree (item-api/create-item "app_skill_tree"))
(def brain-component (item-api/create-item "brain_component"))
(def calc-chip (item-api/create-item "calc_chip"))
(def coin (item-api/create-item "coin"))
(def constraint-ingot (item-api/create-item "constraint_ingot"))
(def constraint-plate (item-api/create-item "constraint_plate"))
(def crystal-low (item-api/create-item "crystal_low"))
(def crystal-normal (item-api/create-item "crystal_normal"))
(def crystal-pure (item-api/create-item "crystal_pure"))
(def data-chip (item-api/create-item "data_chip"))
(def developer-portable (item-api/create-item "developer_portable"))
(def energy-convert-component (item-api/create-item "energy_convert_component"))
(def energy-unit (item-api/create-item "energy_unit"))
(def imag-silicon-ingot (item-api/create-item "imag_silicon_ingot"))
(def imag-silicon-piece (item-api/create-item "imag_silicon_piece"))
(def induction-factor (item-api/create-item "induction_factor"))
(def info-component (item-api/create-item "info_component"))
(def logo (item-api/create-item "logo"))
(def mag-hook (item-api/create-item "mag_hook"))
(def magnetic-coil (item-api/create-item "magnetic_coil"))
(def mat-core (item-api/create-item "mat_core"))
(def matter-unit (item-api/create-item "matter_unit"))
(def media-item (item-api/create-item "media_item"))
(def needle (item-api/create-item "needle"))
(def reinforced-iron-plate (item-api/create-item "reinforced_iron_plate"))
(def reso-crystal (item-api/create-item "reso_crystal"))
(def resonance-component (item-api/create-item "resonance_component"))
(def silbarn (item-api/create-item "silbarn"))
(def terminal-installer (item-api/create-item "terminal_installer"))
(def tutorial (item-api/create-item "tutorial"))
(def wafer (item-api/create-item "wafer"))
(def windgen-fan (item-api/create-item "windgen_fan" {:max-stack-size 1 :max-damage 100}))

;; Register items
(defn register-items [event]
  (doseq [item [app-freq-transmitter app-media-player app-skill-tree brain-component calc-chip coin constraint-ingot constraint-plate crystal-low crystal-normal crystal-pure data-chip developer-portable energy-convert-component energy-unit imag-silicon-ingot imag-silicon-piece induction-factor info-component logo mag-hook magnetic-coil mat-core matter-unit media-item needle reinforced-iron-plate reso-crystal resonance-component silbarn terminal-installer tutorial wafer windgen-fan]]
    (item-api/register-item! item event)))

;; Register event handlers
(item-api/register-event-handler! :register-items register-items)

;; Function to initialize item registrations
(defn register-items! [provider]
  (doseq [[item-id props] core-items]
    (registry/register-item! provider item-id #(create-item item-id props))))