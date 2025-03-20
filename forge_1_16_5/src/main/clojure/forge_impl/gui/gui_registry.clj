(ns forge-impl.gui.gui-registry
  (:require [mcmod.protocols :refer :all]
            [forge-impl.gui.gui-adapter :as gui-adapter])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.inventory.container Container]
           [net.minecraft.util.text StringTextComponent]
           [net.minecraftforge.fml.network NetworkHooks]
           [net.minecraftforge.fml.client.gui GuiUtils]))

;; Track registered GUI factories
(def ^:private gui-factories (atom {}))

;; Register GUI factory
(defn register-gui-factory! [gui-id factory]
  (swap! gui-factories assoc gui-id factory))

;; Create screen for container
(defn create-container-screen [^Container container]
  (let [gui-id (.getType container)
        factory (get @gui-factories gui-id)]
    (when factory
      (factory container))))

;; Screen factory registration helper
(defn register-screen-factory! [mod-id screen-id factory]
  (let [id (str mod-id ":" screen-id)]
    (register-gui-factory! id factory)
    id))

;; Handle GUI opening on client side
(defn open-gui! [^Container container]
  (let [screen (create-container-screen container)]
    (when screen
      (.displayGuiScreen Minecraft/getInstance screen))))