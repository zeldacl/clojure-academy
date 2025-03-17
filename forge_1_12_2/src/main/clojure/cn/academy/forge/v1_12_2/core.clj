(ns cn.academy.forge.v1-12-2.core
  (:require [cn.academy.forge.v1-12-2.block.node :as node])
  (:import [net.minecraftforge.fml.common.Mod Mod$EventHandler]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent]))

(gen-class
  :name cn.academy.forge.v1_12_2.AcademyForgeMod
  :prefix "forge-"
  :methods [[preInit [net.minecraftforge.fml.common.event.FMLPreInitializationEvent] void]]
  :annotations [[net.minecraftforge.fml.common.Mod 
                {:modid "academy", :name "Academy Craft", :version "1.0"}]])

(defn forge-preInit [_ event]
  ;; Initialize block factory
  (cn.academy.block.block.BlockNode$Factory/setForgeFactory node/forge-factory))