(ns forge-impl.gui.node-gui-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.client.gui.widget TextFieldWidget Button]
           [net.minecraft.util.text StringTextComponent]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.systems RenderSystem]))

(def ^:private TEXTURE_CACHE (atom {}))

(defn- get-or-create-texture [path]
  (if-let [tex (get @TEXTURE_CACHE path)]
    tex
    (let [loc (ResourceLocation. "cljacademy" path)]
      (swap! TEXTURE_CACHE assoc path loc)
      loc)))

(defn create-node-screen [mcmod-gui container]
  (proxy [ContainerScreen] [container 
                          (StringTextComponent. (get-title mcmod-gui))
                          (.getPlayer container)]

    (init []
      (proxy-super init)
      (init mcmod-gui))

    (render [^MatrixStack matrix mouse-x mouse-y partial-ticks]
      (renderBackground matrix)
      (proxy-super render matrix mouse-x mouse-y partial-ticks)
      
      ;; Set up standard MC rendering state
      (RenderSystem/color4f 1.0 1.0 1.0 1.0)
      (.bind minecraft.client.texture.TextureManager 
            (get-or-create-texture (get-texture mcmod-gui)))
      
      ;; Call protocol render with wrapped data
      (render mcmod-gui 
             {:x mouse-x :y mouse-y}
             {:matrix-stack matrix
              :partial-ticks partial-ticks}))

    (mouseClicked [x y btn]
      (proxy-super mouseClicked x y btn)
      (when-let [handler (get-slot-click-handler mcmod-gui)]
        (handler x y btn)))
    
    (keyPressed [key scancode modifiers]
      (proxy-super keyPressed key scancode modifiers) 
      (when-let [handler (get-key-handler mcmod-gui)]
        (handler key scancode modifiers)))

    (onClose []
      (proxy-super onClose)
      (on-close mcmod-gui))))

(defn create-screen [container]
  (let [mcmod-gui (create-gui container)]
    (create-node-screen mcmod-gui container)))

(defn create-text-field [font x y width text]
  (TextFieldWidget. font x y width 12 
                   (StringTextComponent. text)))

(defn create-button [x y width text callback]
  (Button. x y width 20
          (StringTextComponent. text)
          callback))