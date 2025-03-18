(ns cn.academy.forge-1-16.gui-bridge
  (:require [cn.academy.gui.core :as gui])
  (:import [net.minecraft.inventory container Container]
           [net.minecraft.client.gui.screen Screen]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.fml.network NetworkHooks]
           [net.minecraft.client.gui.widget Widget]))

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

(defrecord ForgeContainer [container player]
  Container
  (canInteractWith [_ player]
    (gui/can-interact? container player))
  
  (transferStackInSlot [_ player index]
    (gui/transfer-stack container index player))
  
  (getSlot [_ index]
    (get (gui/get-slots container) index)))

(defrecord ForgeScreen [screen container player]
  Screen
  (init [this]
    (proxy-super init)
    (gui/init screen))
  
  (render [this matrix-stack mouse-x mouse-y partial-ticks]
    (proxy-super render matrix-stack mouse-x mouse-y partial-ticks)
    (gui/render screen 
                {:matrix matrix-stack
                 :mouse-x mouse-x
                 :mouse-y mouse-y
                 :partial-ticks partial-ticks}
                {:container container
                 :player player}))
  
  (tick [this]
    (proxy-super tick)
    (gui/tick screen))
  
  (onClose [this]
    (proxy-super onClose)
    (gui/on-close screen)))

(defrecord ForgeGuiRegistry [screens containers]
  gui/IGuiRegistry
  (register-screen [this factory]
    (swap! screens assoc 
           (gui/get-screen-id factory)
           factory))
  
  (register-container [this factory]
    (swap! containers assoc
           (gui/get-container-id factory) 
           factory))
  
  (create-menu [this id player pos]
    (when-let [factory (get @containers id)]
      (->ForgeContainer
        (gui/create-container factory id player pos)
        player)))
  
  (open-screen [this container player]
    (when-let [factory (get @screens (gui/get-screen-id container))]
      (let [screen (->ForgeScreen 
                    (gui/create-screen factory container player)
                    container
                    player)]
        (NetworkHooks/openGui player
                            (reify net.minecraft.inventory.container.INamedContainerProvider
                              (createMenu [_ id player _]
                                (create-menu this id player nil))
                              (getDisplayName [_]
                                (.getDisplayName player)))
                            (fn [buf]
                              nil))))))

(defn create-registry []
  (->ForgeGuiRegistry (atom {}) (atom {})))