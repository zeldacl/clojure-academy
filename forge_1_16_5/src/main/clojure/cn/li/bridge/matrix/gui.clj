(ns cn.li.bridge.matrix.gui
  (:require [cn.li.bridge.matrix.api :as api]
            [cn.li.bridge.gui.api :as gui])
  (:import [net.minecraft.client.gui.screen Screen]
           [net.minecraft.client.gui.widget Button TextFieldWidget]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.util.text StringTextComponent]))

(def ^:private matrix-gui-texture 
  (ResourceLocation. "li:textures/gui/matrix.png"))

(def ^:private gui-size {:width 176 :height 166})
(def ^:private text-color 0x404040)

;; GUI component positions
(def ^:private components
  {:title {:x 8 :y 6}
   :network-id {:x 28 :y 20}
   :password {:x 28 :y 40}
   :energy {:x 8 :y 60}
   :plates {:x 8 :y 80}
   :core {:x 8 :y 100}
   :connect-btn {:x 100 :y 40 :width 70 :height 20}
   :disconnect-btn {:x 100 :y 40 :width 70 :height 20}})

(defrecord MatrixScreen [matrix container]
  gui/IGui
  (init [this]
    (let [{:keys [network-id password]} (api/sync-with-client matrix)]
      (.addButton this 
                  (Button. (:x (:connect-btn components))
                          (:y (:connect-btn components))
                          (:width (:connect-btn components))
                          (:height (:connect-btn components))
                          (StringTextComponent. "Connect")
                          (fn [_] 
                            (api/connect-to-network matrix network-id password))))
      (.addButton this
                  (Button. (:x (:disconnect-btn components))
                          (:y (:disconnect-btn components))
                          (:width (:disconnect-btn components))
                          (:height (:disconnect-btn components))
                          (StringTextComponent. "Disconnect")
                          (fn [_]
                            (api/leave-network matrix))))))
  
  (render [this mouse-data render-data]
    (let [{:keys [matrix-stack]} render-data
          {:keys [x y]} mouse-data
          state (api/sync-with-client matrix)
          {:keys [energy capacity formed? core-level plates]} (:state state)]
      
      ;; Render background
      (.bindTexture minecraft matrix-gui-texture)
      (.blit this matrix-stack 
             (get-in components [:title :x])
             (get-in components [:title :y])
             0 0 
             (:width gui-size)
             (:height gui-size))
      
      ;; Render text components
      (.drawString this matrix-stack font
                  (StringTextComponent. "Matrix Control")
                  (get-in components [:title :x])
                  (get-in components [:title :y])
                  text-color)
      
      ;; Render energy bar
      (.drawString this matrix-stack font
                  (StringTextComponent. (str "Energy: " energy "/" capacity))
                  (get-in components [:energy :x])
                  (get-in components [:energy :y])
                  text-color)
      
      ;; Render structure info
      (.drawString this matrix-stack font
                  (StringTextComponent. (str "Plates: " (count plates)))
                  (get-in components [:plates :x])
                  (get-in components [:plates :y])
                  text-color)
      
      (.drawString this matrix-stack font
                  (StringTextComponent. (str "Core Level: " core-level))
                  (get-in components [:core :x])
                  (get-in components [:core :y])
                  text-color)))
  
  (on-close [_]
    nil)
  
  (get-title [_]
    (StringTextComponent. "Matrix"))
  
  (get-texture [_]
    matrix-gui-texture))

(defn create-screen [matrix container]
  (->MatrixScreen matrix container))