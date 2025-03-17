(ns cn.academy.block.matrix-gui-adapter
  (:require [cn.academy.block.matrix-gui :as gui])
  (:import [net.minecraft.inventory.container Container ContainerType Slot]
           [net.minecraft.client.gui.screen.inventory ContainerScreen]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraftforge.fml.network NetworkHooks]))

(defprotocol IForgeGuiAdapter
  (create-container-type [this])
  (create-container [this matrix player])
  (create-screen [this container])
  (open-gui [this player world pos]))

(defrecord ForgeMatrixContainer [matrix-gui]
  Container
  (canInteractWith [_ player]
    true)
  
  (transferStackInSlot [this player index]
    (let [slot (.getSlot this index)]
      (when (.getHasStack slot)
        (let [stack (.getStack slot)]
          (if (< index 4)  ; Matrix slots
            (merge-into-player-inventory this stack)
            (merge-into-matrix-slots this stack)))))))

(defrecord ForgeMatrixScreen [container matrix-gui ^ResourceLocation texture]
  ContainerScreen
  (render [this matrix-stack mouse-x mouse-y partial-ticks]
    (let [{:keys [width height]} (gui/draw-background matrix-gui 
                                                    (.width this) 
                                                    (.height this))]
      (.renderBackground this matrix-stack)
      (.bind TextureManager (.texture this))
      (.blit this matrix-stack 
             (.x this) (.y this) 
             0 0 width height))
    (let [{:keys [title energy-text]} (gui/draw-foreground matrix-gui mouse-x mouse-y)]
      (.drawString this matrix-stack 
                  (.font this) title 
                  (+ (.x this) 8) (+ (.y this) 6)
                  4210752)
      (.drawString this matrix-stack
                  (.font this) energy-text
                  (+ (.x this) 8) (+ (.y this) 73)
                  4210752))))

(defrecord ForgeGuiAdapter [matrix-gui]
  IForgeGuiAdapter  
  (create-container-type [_]
    (ContainerType/Builder. 
      (reify ContainerType$IFactory
        (create [_ windowId player world]
          (create-container matrix (.getTileEntity world (.getPos player)) player)))))
  
  (create-container [_ matrix player]
    (let [container (->ForgeMatrixContainer matrix-gui)]
      (doseq [{:keys [x y]} (gui/get-inventory-slots matrix-gui)]
        (.addSlot container (Slot. (.inventory matrix) x y)))
      container))
  
  (create-screen [_ container]
    (->ForgeMatrixScreen container matrix-gui 
                        (ResourceLocation. "academy:textures/gui/matrix.png")))
  
  (open-gui [this player world pos]
    (NetworkHooks/openGui player 
                         (reify INamedContainerProvider
                           (createMenu [_ windowId playerInv player]
                             (create-container this 
                                             (.getTileEntity world pos) 
                                             player))))))

(defn create-adapter [matrix-gui]
  (->ForgeGuiAdapter matrix-gui))