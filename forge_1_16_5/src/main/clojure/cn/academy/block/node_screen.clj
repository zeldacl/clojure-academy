(ns cn.academy.block.node-screen
  (:require [cn.academy.block.node :as node])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget Button]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.util.text StringTextComponent]))

(def ^:private node-gui-texture 
  (ResourceLocation. "academy" "textures/gui/node.png"))

(defrecord NodeScreen [container title]
  Screen
  (init [this]
    (proxy-super init)
    ; Add widgets initialization here
    nil)
  
  (render [this matrix mouse-x mouse-y partial-ticks]
    (proxy-super render matrix mouse-x mouse-y partial-ticks)
    ; Add rendering code here
    nil)
  
  (mouseClicked [this mouse-x mouse-y button]
    (proxy-super mouseClicked mouse-x mouse-y button)
    ; Add mouse handling here
    true))

(defrecord NodeContainer [tile-entity player]
  ; Add container implementation here
  )