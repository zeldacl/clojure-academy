(ns forge-impl.gui-impl
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget Widget]
           [net.minecraft.client.renderer IRenderTypeBuffer]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.util.text StringTextComponent]))

(defrecord ForgeGui [^Screen screen matrix-stack]
  IGui
  (init [_]
    (.init screen))
  
  (render [_ mouse-x mouse-y partial-ticks]
    (.render screen matrix-stack mouse-x mouse-y partial-ticks))
  
  (on-mouse-clicked [_ mouse-x mouse-y button]
    (.mouseClicked screen mouse-x mouse-y button))
  
  (on-key-pressed [_ key-code scan-code modifiers]
    (.keyPressed screen key-code scan-code modifiers))
  
  (on-close [_]
    (.onClose screen))
  
  (draw-background [_ renderer mouse-x mouse-y]
    (.renderBackground screen matrix-stack))
  
  (draw-foreground [_ renderer mouse-x mouse-y]
    (.renderForeground screen matrix-stack mouse-x mouse-y))
  
  (handle-mouse-click [_ mouse-x mouse-y button]
    (.mouseClicked screen mouse-x mouse-y button))
  
  (handle-key-press [_ key scancode modifiers]
    (.keyPressed screen key scancode modifiers)))

(defrecord ForgeGuiComponent [^Widget widget]
  IGuiComponent  
  (init-component [_]
    (.init widget))
  
  (render [_ mouse-x mouse-y partial-ticks]
    (.render widget matrix-stack mouse-x mouse-y partial-ticks))
  
  (is-mouse-over? [_ mouse-x mouse-y]
    (.isMouseOver widget mouse-x mouse-y))
  
  (on-mouse-clicked [_ mouse-x mouse-y button]
    (.mouseClicked widget mouse-x mouse-y button)))

(defn create-gui [screen]
  (->ForgeGui screen (MatrixStack.)))

(defn create-gui-component [widget]
  (->ForgeGuiComponent widget))