(ns cn.academy.forge.v1-16-5.core
  (:require [cn.academy.forge.v1-16-5.block.node :as node])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(gen-class
  :name cn.academy.forge.v1_16_5.AcademyForgeMod
  :prefix "forge-"
  :state state
  :init init
  :methods [[setup [net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]])

(defn forge-init []
  [[] (atom {})])

(defn forge-setup [this event]
  ;; Initialize block factory
  (cn.academy.block.block.BlockNode$Factory/setForgeFactory node/forge-factory))

;; Register setup method to event bus
(.addListener (FMLJavaModLoadingContext/get) 
             (reify java.util.function.Consumer
               (accept [this event]
                 (forge-setup nil event))))