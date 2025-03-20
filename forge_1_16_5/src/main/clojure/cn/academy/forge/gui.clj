(ns cn.academy.forge.gui
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.inventory.container Container Slot]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]))

(defrecord ForgeSlot [^Slot delegate handler]
  ISlot
  (get-index [this]
    (.slotNumber delegate))
    
  (get-x [this]
    (.xPos delegate))
    
  (get-y [this]
    (.yPos delegate))
    
  (is-valid? [this item]
    (is-valid? handler item))
  
  (on-slot-changed [this]
    (.onSlotChanged delegate)))

(defrecord ForgeContainer [delegate handler player]
  IContainer
  (get-slot-count [this]
    (.inventorySlots delegate size))
    
  (get-slot [this index]
    (when-let [slot (.getSlot delegate index)]
      (->ForgeSlot slot handler)))
      
  (transfer-stack [this slot-index]
    (.transferStackInSlot delegate player slot-index))
    
  (can-interact? [this]
    (.canInteractWith delegate player)))

(defrecord ForgeScreen [^ContainerScreen delegate config container]
  IScreen
  (init [this]
    (.init delegate))
    
  (render-background [this width height]
    (let [matrix-stack (MatrixStack.)]
      (.renderBackground delegate matrix-stack)
      (when-let [texture (get-texture config)]
        (.bindTexture delegate 
                     (ResourceLocation. texture))
        (.blit delegate
               matrix-stack
               (.guiLeft delegate)
               (.guiTop delegate)
               0 0
               width
               height))))
               
  (render-foreground [this mouse-x mouse-y]
    (let [matrix-stack (MatrixStack.)]
      (.renderLabels delegate matrix-stack mouse-x mouse-y)))
      
  (handle-click [this x y button]
    (.mouseClicked delegate x y button)))

(defn create-container [handler player]
  (let [forge-container (proxy [Container] [ContainerType/GENERIC_9x9]
                         (canInteractWith [player]
                           (can-interact? handler player)))]
    (->ForgeContainer forge-container handler player)))

(defn create-screen [config container player]
  (let [forge-screen (proxy [ContainerScreen] [(.delegate container) 
                                             (.inventory player)
                                             (get-title config)])]
    (->ForgeScreen forge-screen config container)))

(defrecord ForgeGuiHandler []
  IGuiFactory
  (create-container [this menu-type player inventory pos]
    (case menu-type
      :machine (create-container (create-machine-container inventory) player)
      :generic (create-container (create-basic-container inventory) player)))
      
  (create-screen [this screen-type container player]
    (case screen-type
      :machine (create-screen machine-screen-config container player)
      :generic (create-screen generic-screen-config container player))))