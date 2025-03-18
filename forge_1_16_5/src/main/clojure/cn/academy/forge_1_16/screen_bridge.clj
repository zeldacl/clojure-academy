(ns cn.academy.forge-1-16.screen-bridge
  (:import [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.util.text StringTextComponent]))

(defprotocol IScreenProvider
  "Protocol for screen lifecycle"
  (get-texture [this]
    "Get screen texture location")
  (get-title [this]
    "Get screen title")
  (render-background [this screen matrix-stack mouse-x mouse-y partial]
    "Render screen background")
  (render-foreground [this screen matrix-stack mouse-x mouse-y]
    "Render screen foreground")
  (render-tooltip [this screen matrix-stack mouse-x mouse-y]
    "Render screen tooltips"))

(defn create-base-screen [provider container title]
  (proxy [ContainerScreen] [container (StringTextComponent. title)]
    (render [matrix-stack mouse-x mouse-y partial]
      (proxy-super render matrix-stack mouse-x mouse-y partial)
      (when-let [texture (get-texture provider)]
        (.bind net.minecraft.client.Minecraft/getInstance
               (.getTextureManager)
               (ResourceLocation. texture)))
      (render-background provider this matrix-stack mouse-x mouse-y partial)
      (render-foreground provider this matrix-stack mouse-x mouse-y)
      (render-tooltip provider this matrix-stack mouse-x mouse-y))))

(defn draw-texture [screen x y width height u v]
  (.blit screen x y u v width height))