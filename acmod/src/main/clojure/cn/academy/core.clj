(ns cn.academy.core
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cn.academy.registry :as registry]
            [cn.academy.block.registry :as block-reg]
            [mcmod.protocols :refer :all])
  (:import [net.minecraft.creativetab CreativeTabs]
           [net.minecraft.item ItemStack]
           [net.minecraftforge.fml.common.network NetworkRegistry]
           [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.common.config Configuration]
           [net.minecraft.util ResourceLocation]))

(def modid "academy")
(def version "1.0.0")
(def debug true)

(def channel (NetworkRegistry/INSTANCE.newSimpleChannel modid))
(def config (atom nil))
(def mod-instance (atom nil))

;; Creative tab for Academy Craft items
(def creative-tab 
  (proxy [CreativeTabs] ["AcademyCraft"]
    (createIcon []
      (ItemStack. net.minecraft.init.Items/DIAMOND)))) ;; Placeholder icon

(defn init-network []
  ;; Network message registration will go here
  nil)

(defn init-config [config-file]
  (reset! config (Configuration. config-file))
  (.save @config))

(defn init []
  (log/info "Initializing AcademyCraft core module")
  (MinecraftForge/EVENT_BUS.register (proxy [Object] [])))

(defn post-init []
  (when @config
    (.save @config)))

(defn init-mod []
  (let [registry (registry/register-all)]
    (reset! mod-instance registry)
    (println "AcademyCraft Core Initialized")))

(def MOD-ID "acmod")

(defn register-block! [block-id block-def]
  (block-reg/create-block block-id block-def))

(defn register-tile-entity! [block-id te-type te-def]
  (block-reg/register-tile-entity! block-id te-type te-def))

(defn load-blocks! []
  ;; Register basic blocks
  (register-block! "machine_frame" 
    {:material :iron :hardness 4.0})
  
  ;; Register blocks with tile entities  
  (register-block! "energy_generator"
    {:material :iron 
     :hardness 3.5
     :has-tile-entity true})
  
  ;; Register tile entities
  (register-tile-entity! "energy_generator" 
    :energy_generator
    (cn.academy.block.block.energy-generator/->EnergyGeneratorTile 
      (atom 0) (atom 10000))))

(defn init! []
  (load-blocks!))