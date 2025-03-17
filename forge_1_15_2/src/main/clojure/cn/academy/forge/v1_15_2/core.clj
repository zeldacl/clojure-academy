(ns cn.academy.forge.v1_15_2.core
  (:require [cn.academy.api.block :as block-api]
            [cn.academy.forge.v1_15_2.block :refer [forge-factory]])
  (:import [net.minecraftforge.fml.common Mod]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(gen-class
  :name cn.academy.forge.v1_15_2.AcademyForgeMod
  :prefix "forge-"
  :state state
  :init init
  :annotations [[net.minecraftforge.fml.common.Mod "academy"]])

(defn forge-init []
  [[] (atom {})])

(defn forge-setup [this ^FMLCommonSetupEvent event]
  (block-api/set-forge-factory! forge-factory))

(defn -clinit []
  (let [mod-bus (.getModEventBus (FMLJavaModLoadingContext/get))]
    (.addListener mod-bus (reify java.util.function.Consumer
                           (accept [_ event]
                             (forge-setup nil event))))))