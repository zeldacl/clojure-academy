(ns forge-impl.gui.wireless-matrix-screen
  (:import [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.util.text StringTextComponent]
           [com.mojang.blaze3d.matrix MatrixStack]))

(def ^:private GUI_TEXTURE (ResourceLocation. "cljacademy" "textures/gui/wireless_matrix.png"))

(defn create-screen [container width height]
  (proxy [ContainerScreen] [container (StringTextComponent. "Wireless Matrix") (.getPlayer container)]
    (render [^MatrixStack matrix-stack mouse-x mouse-y partial-ticks]
      (this.renderBackground matrix-stack)
      (proxy-super render matrix-stack mouse-x mouse-y partial-ticks)
      
      ;; Draw energy bar
      (let [energy-stored (.get-energy-stored container)
            max-energy (.get-max-energy-stored container)
            energy-scale (int (* 50 (/ energy-stored max-energy)))]
        
        ;; Draw energy bar background
        (.bind this.minecraft.textureManager GUI_TEXTURE)
        (this.blit matrix-stack 
                  (+ this.leftPos 100) 
                  (+ this.topPos 20) 
                  176 0 16 50)
        
        ;; Draw energy bar foreground
        (this.blit matrix-stack 
                  (+ this.leftPos 100)
                  (+ (+ this.topPos 20) (- 50 energy-scale))
                  192 (- 50 energy-scale)
                  16 energy-scale)
        
        ;; Draw energy text
        (.drawString this.font matrix-stack 
                    (str energy-stored "/" max-energy " FE")
                    (+ this.leftPos 120)
                    (+ this.topPos 35)
                    16777215))))) ; White color