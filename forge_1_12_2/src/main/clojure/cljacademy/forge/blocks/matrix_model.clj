(ns cljacademy.forge.blocks.matrix-model
  (:require [cljacademy.blocks.matrix-render :as core-render])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer.texture TextureMap]
           [net.minecraftforge.client.model IModel]))

(defn create-model-loader []
  (reify IModel
    (^void getTextures [this]
      [(ResourceLocation. "cljacademy:textures/blocks/wireless_matrix")])
    
    (^IModel retexture [this textures]
      this)
    
    (^IModel process [this state]
      this)))