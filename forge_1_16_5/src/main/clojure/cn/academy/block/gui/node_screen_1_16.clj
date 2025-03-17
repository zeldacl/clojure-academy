(ns cn.academy.block.gui.node-screen-1-16
  (:require [cn.academy.block.gui.node-gui :as core])
  (:import [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.client.gui.widget Button]
           [net.minecraft.util.text StringTextComponent TranslationTextComponent]
           [com.mojang.blaze3d.systems RenderSystem]
           [com.mojang.blaze3d.matrix MatrixStack]))

(defrecord NodeScreen1_16 [container core-gui]
  ContainerScreen
  (render [this matrix mouse-x mouse-y partial-ticks]
    (proxy-super render matrix mouse-x mouse-y partial-ticks)
    (core/render-tooltips core-gui matrix mouse-x mouse-y))

  (renderBg [this matrix partial-ticks mouse-x mouse-y]
    (RenderSystem/color4f 1.0 1.0 1.0 1.0)
    (core/render-background core-gui matrix leftPos topPos)
    (core/render-slots core-gui matrix)
    (let [tile (.-tile container)
          energy (.getEnergy tile)
          max-energy (.getMaxEnergy tile)]
      (core/render-energy-bar core-gui matrix energy max-energy)))

  (renderLabels [this matrix mouse-x mouse-y]
    (let [title (TranslationTextComponent. "block.academy.node")
          player-inv (TranslationTextComponent. "container.inventory")]
      (.drawString font title 8 6 4210752)
      (.drawString font player-inv 8 72 4210752))))

(defn create-screen [container]
  (->NodeScreen1_16 
    container 
    (core/create-gui container 176 166)))