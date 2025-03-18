(ns cn.academy.block.gui.cat-engine-gui
  (:require [cn.academy.energy.api.wireless-helper :as wireless-helper]
            [cn.academy.block.tileentity.tile-cat-engine :as tile-engine]
            [cn.academy.client.gui.base :as gui-base])
  (:import [net.minecraft.client.gui GuiScreen]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.client.resources I18n]))

(def ^:private GUI_TEX 
  (ResourceLocation. "cljacademy" "textures/gui/cat_engine.png"))

(def ^:private GUI_WIDTH 176)
(def ^:private GUI_HEIGHT 166)

(defprotocol ICatEngineGui
  (draw-background [this mouse-x mouse-y partial-ticks])
  (draw-foreground [this mouse-x mouse-y])
  (draw-energy-info [this])
  (draw-network-info [this]))

(defrecord CatEngineGui [engine gui-base]
  ICatEngineGui
  (draw-background [_ mouse-x mouse-y partial-ticks]
    (gui-base/bind-texture GUI_TEX)
    (gui-base/draw-textured-rect 0 0 GUI_WIDTH GUI_HEIGHT 0 0))
  
  (draw-foreground [this mouse-x mouse-y]
    (draw-energy-info this)
    (draw-network-info this))
  
  (draw-energy-info [_]
    (let [energy (tile-engine/get-energy engine)
          max-energy tile-engine/ENERGY_MAX
          gen-rate (tile-engine/get-this-tick-gen engine)]
      (gui-base/draw-string 
        (I18n/format "gui.cat_engine.energy" 
                    (int energy) 
                    (int max-energy))
        8 20 0x404040)
      (gui-base/draw-string
        (I18n/format "gui.cat_engine.gen_rate"
                    (format "%.1f" gen-rate))
        8 32 0x404040)))
  
  (draw-network-info [_]
    (if (wireless-helper/is-generator-linked? engine)
      (let [node (wireless-helper/get-linked-node engine)
            network (wireless-helper/get-wireless-net node)]
        (gui-base/draw-string
          (I18n/format "gui.cat_engine.linked_to"
                      (wireless-helper/get-network-name network))
          8 50 0x404040))
      (gui-base/draw-string
        (I18n/format "gui.cat_engine.not_linked")
        8 50 0x404040)))

  GuiScreen
  (drawScreen [this mouse-x mouse-y partial-ticks]
    (draw-background this mouse-x mouse-y partial-ticks)
    (draw-foreground this mouse-x mouse-y)))

(defn create [engine]
  (->CatEngineGui engine (gui-base/create)))