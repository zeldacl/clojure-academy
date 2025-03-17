(ns cn.academy.block.registry-1-16
  (:require [cn.academy.block.node-block-1-16 :as node]
            [cn.academy.block.container.node-container-1-16 :as container]
            [cn.academy.block.gui.node-screen-1-16 :as screen])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraftforge.fml.network NetworkHooks]
           [net.minecraft.inventory.container ContainerType]
           [net.minecraftforge.fml RegistryObject DeferredRegister]
           [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]))

(def BLOCKS (DeferredRegister/create ForgeRegistries/BLOCKS "academy"))
(def CONTAINERS (DeferredRegister/create ForgeRegistries/CONTAINERS "academy"))

(def node-basic
  (.register BLOCKS "node_basic"
    (fn [] (node/create-node-block :basic))))

(def node-standard
  (.register BLOCKS "node_standard"
    (fn [] (node/create-node-block :standard))))

(def node-advanced
  (.register BLOCKS "node_advanced"
    (fn [] (node/create-node-block :advanced))))

(def node-container
  (.register CONTAINERS "node"
    (fn []
      (ContainerType.
        (reify net.minecraft.inventory.container.ContainerType$IFactory
          (create [_ window-id inv player]
            (container/create-container node-container window-id inv player)))))))

(defn register-blocks []
  ;; Register registries
  (let [mod-bus (.get (FMLJavaModLoadingContext/get) "modEventBus")]
    (.register BLOCKS mod-bus)
    (.register CONTAINERS mod-bus)))

(defn register-screen-factory []
  ;; Register screen factory on client side
  (ClientRegistry/registerScreenFactory 
    @node-container
    (fn [container]
      (screen/create-screen container))))