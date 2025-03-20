(ns cn.academy.forge.render
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.client.renderer.model IModelGeometry IBakedModel]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client.renderer IRenderTypeBuffer]))

(defrecord ForgeModelLoader [models]
  IModelLoader
  (load-model [this path]
    (when-let [model (get @models path)]
      model))
      
  (load-texture [this path]
    (ResourceLocation. path))
    
  (register-model [this model-id model]
    (swap! models assoc model-id model))
    
  (register-texture [this texture-id texture]
    ;; Textures are registered via resource packs
    ))

(defrecord ForgeModelGeometry [vertices faces texture-coords]
  IModelGeometry
  (get-vertices [this]
    vertices)
    
  (get-faces [this]
    faces)
    
  (get-texture-coords [this]
    texture-coords)
    
  (apply-transform [this transform]
    (update this :vertices #(map transform %))))

(defrecord ForgeRenderState [matrix-stack texture]
  IRenderState
  (push-matrix [this]
    (.push ^MatrixStack matrix-stack)
    this)
    
  (pop-matrix [this]
    (.pop ^MatrixStack matrix-stack)
    this)
    
  (translate [this x y z]
    (.translate ^MatrixStack matrix-stack x y z)
    this)
    
  (rotate [this angle x y z]
    (.rotate ^MatrixStack matrix-stack angle x y z)
    this)
    
  (scale [this x y z]
    (.scale ^MatrixStack matrix-stack x y z)
    this)
    
  (bind-texture [this texture]
    (assoc this :texture texture)))

(defrecord ForgeRenderer [buffer]
  IRenderer
  (render [this state data]  
    (let [matrix (.last ^MatrixStack (:matrix-stack state))
          normal (.normal matrix)
          pos (.pose matrix)]
      ;; Render using buffer and state
      ))
      
  (should-render-offscreen? [this]
    false))

(defrecord ForgeTextureAtlas [sprite-map]
  ITextureAtlas
  (register-sprite [this location]
    (swap! sprite-map assoc location 
           (TextureAtlasSprite. location)))
           
  (get-sprite [this location]
    (get @sprite-map location))
    
  (stitch [this]
    ;; Atlas stitching handled by Minecraft
    ))

(defrecord ForgeAnimatedSprite [^TextureAtlasSprite sprite]
  IAnimatedSprite
  (get-frame [this age]
    (.getFrameLocation sprite age))
    
  (get-random-frame [this]  
    (.getRandomFrameLocation sprite))
    
  (update-animation [this]
    (.tick sprite)))

;; Factory functions
(defn create-model-loader []
  (->ForgeModelLoader (atom {})))
  
(defn create-model-geometry [vertices faces uvs]
  (->ForgeModelGeometry vertices faces uvs))
  
(defn create-render-state [matrix-stack]
  (->ForgeRenderState matrix-stack nil))
  
(defn create-renderer [buffer]
  (->ForgeRenderer buffer))
  
(defn create-texture-atlas []
  (->ForgeTextureAtlas (atom {})))
  
(defn create-animated-sprite [sprite]
  (->ForgeAnimatedSprite sprite))

;; Utility functions for converting between coordinate systems
(defn minecraft->model-coords [[x y z]]
  [(/ x 16.0) (/ y 16.0) (/ z 16.0)])
  
(defn model->minecraft-coords [[x y z]]
  [(* x 16.0) (* y 16.0) (* z 16.0)])