(ns cn.li.bridge.gui.bridge
  (:require [cn.li.bridge.gui.api :as gui]
            [cn.li.bridge.container.api :as container])
  (:import [net.minecraft.inventory.container Container ContainerType]
           [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget Button Widget]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.common.extensions IForgeContainerType]))

(defprotocol IGuiBridge
  "Bridge between platform-independent GUI and Forge"
  (create-container [this container-def player world pos]
    "Create Forge container from platform definition")
  (create-screen [this screen-def container]
    "Create Forge screen from platform definition")
  (register-container [this registry container-id container-def]
    "Register container with Forge registry")
  (register-screen [this container-type screen-def]
    "Register screen factory for container type"))

(deftype ForgeContainerAdapter [container-def]
  Container
  (stillValid [_ player]
    (container/is-valid? container-def player))
  
  (removed [_ player]
    (container/on-closed container-def player))
  
  (broadcastChanges [_]
    (container/sync-data container-def))
  
  (clickMenuButton [_ player button-id]
    (container/on-button-clicked container-def player button-id)))

(defrecord ForgeGuiBridge []
  IGuiBridge
  (create-container [_ container-def player world pos]
    (ForgeContainerAdapter. container-def))
  
  (create-screen [_ screen-def container]
    (proxy [Screen] []
      (render [matrix-stack mouse-x mouse-y delta]
        (gui/render screen-def 
                   {:x mouse-x :y mouse-y}
                   {:matrix-stack matrix-stack}))
      
      (init []
        (gui/init screen-def))
      
      (onClose []
        (gui/on-close screen-def))))
  
  (register-container [_ registry container-id container-def]
    (let [container-type (IForgeContainerType/create 
                          (fn [window-id player world extra-data]
                            (create-container container-def player world extra-data)))]
      (.register registry container-id container-type)))
  
  (register-screen [_ container-type screen-def]
    (fn [container]
      (create-screen screen-def container))))

(defn create-gui-bridge []
  (->ForgeGuiBridge))