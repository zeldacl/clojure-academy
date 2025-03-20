(ns cn.academy.block.gui.node-screen-1-12
  (:require [cn.academy.block.gui.node-gui :as core]
            [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.gui.inventory GuiContainer]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.util ResourceLocation]))

(def ^:private TEXTURE_CACHE (atom {}))

(defn- get-or-create-texture [path]
  (if-let [tex (get @TEXTURE_CACHE path)]
    tex
    (let [loc (ResourceLocation. "cljacademy" path)]
      (swap! TEXTURE_CACHE assoc path loc)
      loc)))

(defrecord NodeScreen1_12 [container mcmod-gui]
  GuiContainer
  (initGui []
    (proxy-super initGui)
    (init mcmod-gui))

  (drawScreen [mouse-x mouse-y partial-ticks]
    (proxy-super drawScreen mouse-x mouse-y partial-ticks)
    (render mcmod-gui 
           {:x mouse-x :y mouse-y}
           {:partial-ticks partial-ticks}))

  (drawGuiContainerBackgroundLayer [partial-ticks mouse-x mouse-y]
    (GlStateManager/color 1.0 1.0 1.0 1.0)
    (.bindTexture mc (get-or-create-texture (get-texture mcmod-gui)))
    
    ;; Call protocol render with wrapped data for background
    (render mcmod-gui 
           {:x mouse-x :y mouse-y}
           {:partial-ticks partial-ticks
            :layer :background}))

  (drawGuiContainerForegroundLayer [mouse-x mouse-y]
    ;; Call protocol render with wrapped data for foreground
    (render mcmod-gui
           {:x mouse-x :y mouse-y}
           {:layer :foreground}))

  (mouseClicked [x y btn]
    (proxy-super mouseClicked x y btn)
    (when-let [handler (get-slot-click-handler mcmod-gui)]
      (handler x y btn)))
  
  (keyTyped [c code]
    (proxy-super keyTyped c code)
    (when-let [handler (get-key-handler mcmod-gui)]
      (handler (int c) code nil)))

  (onGuiClosed []
    (proxy-super onGuiClosed)
    (on-close mcmod-gui)))

(defn create-screen [container]
  (->NodeScreen1_12 
    container 
    (core/create-gui container)))