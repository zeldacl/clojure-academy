(ns forge-impl.gui.node-gui-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget TextFieldWidget Button]
           [net.minecraft.util.text StringTextComponent TranslationTextComponent]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client.gui.screen.inventory ContainerScreen]))

(def ^:private TEXTURE (ResourceLocation. "acmod" "textures/gui/node.png"))

(defn create-forge-node-gui [mcmod-gui container width height]
  (proxy [ContainerScreen] [container 
                           (StringTextComponent. "Wireless Node")
                           (.getPlayer container)]
    (init []
      (proxy-super init)
      (init mcmod-gui))

    (render [matrix-stack mouse-x mouse-y partial-ticks]
      (proxy-super render matrix-stack mouse-x mouse-y partial-ticks)
      (draw-background mcmod-gui matrix-stack mouse-x mouse-y)
      (draw-foreground mcmod-gui matrix-stack mouse-x mouse-y))

    (mouseClicked [mouse-x mouse-y button]
      (proxy-super mouseClicked mouse-x mouse-y button)
      (handle-mouse-click mcmod-gui mouse-x mouse-y button))
    
    (keyPressed [key scancode modifiers]
      (proxy-super keyPressed key scancode modifiers)
      (handle-key-press mcmod-gui key scancode modifiers))
    
    (onClose []
      (proxy-super onClose)
      (on-close mcmod-gui))))

(defn create-text-field [font x y width text]
  (TextFieldWidget. font x y width 12 
                   (StringTextComponent. text)))

(defn create-button [x y width text callback]
  (Button. x y width 20
          (StringTextComponent. text)
          callback)))