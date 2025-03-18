(ns cn.academy.forge-1-16.gui-registry
  (:require [cn.academy.gui.core :as gui]
            [cn.academy.forge-1-16.gui-bridge :as bridge])
  (:import [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.common.extensions IForgeContainerType]
           [net.minecraft.inventory.container ContainerType]
           [net.minecraft.util ResourceLocation]))

(def registry (bridge/create-registry))

(defn register-screen-factory [factory]
  (gui/register-screen registry factory))

(defn register-container-factory [factory]
  (gui/register-container registry factory))

(defn create-container [id player pos]
  (gui/create-menu registry id player pos))

(defn open-screen [container player]
  (gui/open-screen registry container player))

(defn register-container-type [id factory]
  (ContainerType/register 
    (str "academy:" id)
    (reify IForgeContainerType
      (create [_ windowId player data]
        (create-container id player nil)))))