(ns cn.academy.forge-1-16.gui-bridge
  (:require [cn.academy.gui.core :as gui])
  (:import [net.minecraft.inventory.container Container Slot]
           [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.util.text StringTextComponent]))

(defprotocol IGuiBridge
  "Bridge between platform-independent GUI and Forge"
  (create-container [this gui player]
    "Create container from GUI")
  (create-screen [this container]
    "Create screen for container")
  (register-type [this registry id factory]
    "Register container type"))

(defn- wrap-slot [gui-slot]
  (proxy [Slot] [(:inventory gui-slot) (gui/get-index gui-slot) (gui/get-x gui-slot) (gui/get-y gui-slot)]
    (mayPlace [stack]
      (gui/is-valid? gui-slot stack))
    (setChanged []
      nil)))

(defn- handle-slot-transfer [gui slot player]
  (when-let [result (gui/handle-click gui slot nil)]
    (case (:type result)
      :to-player (add-to-player player (get-slot-stack gui (:slot result)))
      :to-matrix (add-to-gui gui (:slot result) (get-player-stack player)))))

(defrecord ForgeGuiBridge []
  IGuiBridge
  (create-container [_ gui player]
    (proxy [Container] [nil]
      (stillValid [_]
        true)
      
      (quickMoveStack [_ slot]
        (handle-slot-transfer gui slot player)
        nil)))
  
  (create-screen [_ container]
    (let [gui (:gui container)
          {:keys [width height]} (gui/get-size gui)]
      (proxy [ContainerScreen] [container (StringTextComponent. (gui/get-title gui)) width height]
        (render [matrix-stack mouse-x mouse-y partial]
          (proxy-super render matrix-stack mouse-x mouse-y partial)
          
          ; Render background
          (let [{:keys [texture regions]} (gui/render-background (:renderer gui))]
            (.bind net.minecraft.client.Minecraft/getInstance
                   (.getTextureManager)
                   (ResourceLocation. texture))
            (doseq [[x y w h u v] regions]
              (.blit this matrix-stack x y u v w h)))
          
          ; Render foreground
          (when-let [elements (gui/render-foreground (:renderer gui))]
            (doseq [{:keys [x y w h u v]} elements]
              (.blit this matrix-stack x y u v w h)))
          
          ; Render tooltips  
          (when-let [{:keys [lines]} (gui/render-tooltips (:renderer gui) mouse-x mouse-y)]
            (doseq [line lines]
              (.renderTooltip this matrix-stack line mouse-x mouse-y)))))))
  
  (register-type [_ registry id factory]
    (let [type (Container$Type. factory)]
      (.register registry 
                 (ResourceLocation. "academy" id)
                 type)
      type)))

(defn create-bridge []
  (->ForgeGuiBridge))