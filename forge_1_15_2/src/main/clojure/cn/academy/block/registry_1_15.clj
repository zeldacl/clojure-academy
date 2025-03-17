(ns cn.academy.block.registry-1-15
  (:require [cn.academy.block.node-block-1-15 :as node]
            [cn.academy.block.container.node-container-1-15 :as container]
            [cn.academy.block.gui.node-screen-1-15 :as screen])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraftforge.fml.network NetworkHooks]
           [net.minecraft.inventory.container ContainerType]
           [net.minecraftforge.fml RegistryObject]
           [net.minecraftforge.fml.client.registry ClientRegistry]))

(def node-basic-type 
  (RegistryObject/of
    (ResourceLocation. "academy" "node_basic")
    ForgeRegistries/BLOCKS))

(def node-standard-type 
  (RegistryObject/of
    (ResourceLocation. "academy" "node_standard")
    ForgeRegistries/BLOCKS))

(def node-advanced-type
  (RegistryObject/of
    (ResourceLocation. "academy" "node_advanced")
    ForgeRegistries/BLOCKS))

(def node-container-type
  (ContainerType. 
    (reify net.minecraft.inventory.container.ContainerType$IFactory
      (create [_ window-id inv player]
        (container/create-container node-container-type window-id inv player)))))

(defn register-blocks []
  ;; Register blocks
  (doto ForgeRegistries/BLOCKS
    (.register (node/create-node-block :basic))
    (.register (node/create-node-block :standard))
    (.register (node/create-node-block :advanced)))
  
  ;; Register container type
  (.register ForgeRegistries/CONTAINERS
            (ResourceLocation. "academy" "node")
            node-container-type))

(defn register-screen-factory []
  ;; Register screen factory
  (ClientRegistry/registerScreenFactory 
    node-container-type
    (fn [container]
      (screen/create-screen container))))