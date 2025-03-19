(ns forge-impl.gui-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget Button]
           [net.minecraft.util.text StringTextComponent]
           [com.mojang.blaze3d.matrix MatrixStack]))

(defn create-screen [mcmod-gui container]
  (proxy [Screen] [(StringTextComponent. (get-title mcmod-gui))]
    (init []
      (proxy-super init)
      (init mcmod-gui))
    
    (render [^MatrixStack matrix-stack mouse-x mouse-y delta]
      (proxy-super render matrix-stack mouse-x mouse-y delta)
      (render mcmod-gui 
             {:x mouse-x :y mouse-y}
             {:matrix-stack matrix-stack
              :delta delta}))

    (onClose []
      (proxy-super onClose)
      (on-close mcmod-gui))
    
    (mouseClicked [x y btn]
      (proxy-super mouseClicked x y btn)
      (on-button-click mcmod-gui btn {:x x :y y}))))