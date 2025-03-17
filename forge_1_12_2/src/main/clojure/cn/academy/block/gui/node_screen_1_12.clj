(ns cn.academy.block.gui.node-screen-1-12
  (:require [cn.academy.block.gui.node-gui :as core])
  (:import [net.minecraft.client.gui.inventory GuiContainer]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.util ResourceLocation]))

(defrecord NodeScreen1_12 [container core-gui]
  GuiContainer
  (drawScreen [this mouse-x mouse-y partial-ticks]
    (proxy-super drawScreen mouse-x mouse-y partial-ticks)
    (core/render-tooltips core-gui nil mouse-x mouse-y))

  (drawGuiContainerBackgroundLayer [_ partial-ticks mouse-x mouse-y]
    (GlStateManager/color 1.0 1.0 1.0 1.0)
    (core/render-background core-gui nil mouse-x mouse-y)
    (core/render-slots core-gui nil)
    (let [tile (.-tile container)
          energy (.getEnergy tile)
          max-energy (.getMaxEnergy tile)]
      (core/render-energy-bar core-gui nil energy max-energy)))

  (drawGuiContainerForegroundLayer [this mouse-x mouse-y]
    (let [title "Node"
          player-inv "Inventory"]
      (.drawString fontRenderer title 8 6 4210752)
      (.drawString fontRenderer player-inv 8 72 4210752))))

(defn create-screen [container]
  (->NodeScreen1_12 
    container 
    (core/create-gui container 176 166)))