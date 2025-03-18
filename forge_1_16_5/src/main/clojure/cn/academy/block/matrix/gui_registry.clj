(ns cn.academy.block.matrix.gui-registry
  (:require [cn.academy.block.matrix.gui-adapter :as gui-adapter]
            [cn.academy.forge-1-16.screen-bridge :as screen])
  (:import [net.minecraft.client.gui ScreenManager]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraftforge.fml.common Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus]))

(gen-class
  :name cn.academy.block.matrix.GuiRegistry
  :methods [[registerScreens [net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]]
  :prefix "gui-"
  :main false)

(defn gui-registerScreens [_ event]
  (.enqueueWork event
    (fn []
      (ScreenManager/register
        gui-adapter/matrix-container-type
        (fn [container title]
          (let [gui (get-matrix-gui container)
                adapter (gui-adapter/->MatrixGuiAdapter gui)]
            (screen/create-base-screen adapter container (gui-adapter/get-title adapter))))))))