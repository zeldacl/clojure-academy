(ns cn.academy.block.registry-1-12
  (:require [cn.academy.block.node-block-1-12 :as node]
            [cn.academy.block.container.node-container-1-12 :as container]
            [cn.academy.block.gui.node-screen-1-12 :as screen])
  (:import [net.minecraftforge.fml.common.registry.GameRegistry]
           [net.minecraftforge.fml.common.network NetworkRegistry]
           [net.minecraftforge.fml.relauncher Side]
           [net.minecraft.util ResourceLocation]))

(defn register-blocks []
  ;; Register node blocks
  (doto (node/create-node-block :basic)
    (GameRegistry/register "node_basic")
    (GameRegistry/register (ResourceLocation. "academy" "node_basic")))
  
  (doto (node/create-node-block :standard)
    (GameRegistry/register "node_standard")
    (GameRegistry/register (ResourceLocation. "academy" "node_standard")))
  
  (doto (node/create-node-block :advanced)
    (GameRegistry/register "node_advanced")
    (GameRegistry/register (ResourceLocation. "academy" "node_advanced"))))

(defn register-gui-handler []
  (NetworkRegistry/INSTANCE registerGuiHandler
    @cn.academy.AcademyCraft/INSTANCE
    (reify net.minecraftforge.fml.common.network.IGuiHandler
      (getServerGuiElement [_ id player world x y z]
        (container/create-container (.getTileEntity world x y z) player))
      
      (getClientGuiElement [_ id player world x y z]
        (screen/create-screen
          (container/create-container (.getTileEntity world x y z) player))))))