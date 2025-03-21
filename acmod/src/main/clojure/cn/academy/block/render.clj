(ns cn.academy.block.render
  (:require [mcmod.protocols :refer [IRenderer ITextureProvider IModelProvider]]
            [clojure.tools.logging :as log]))

;; Renderer state tracking
(def render-state
  (atom {:textures {}
         :models {}
         :render-queue []}))

;; Block renderer implementation
(defrecord BlockRenderer [block texture-provider model-provider]
  IRenderer
  (init! [_]
    (let [model-id (str (:id block) "_model")
          texture-id (str (:id block) "_texture")]
      (when-let [model (.load-model! model-provider model-id)]
        (swap! render-state assoc-in [:models (:id block)] model))
      (when-let [texture (.load-texture! texture-provider texture-id)]
        (swap! render-state assoc-in [:textures (:id block)] texture))))
  
  (render! [this pos data]
    (when-let [model (get-in @render-state [:models (:id block)])]
      (let [texture (get-in @render-state [:textures (:id block)])
            state (get data :block-state)
            alpha (get data :alpha 1.0)]
        (.render-model! model-provider model pos 
                       {:texture texture
                        :state state
                        :alpha alpha}))))
  
  (cleanup! [_]
    (swap! render-state update :models dissoc (:id block))
    (swap! render-state update :textures dissoc (:id block))))

;; Hologram renderer implementation  
(defrecord HologramRenderer [texture-provider]
  IRenderer
  (init! [_]
    (when-let [texture (.load-texture! texture-provider "hologram")]
      (swap! render-state assoc :hologram-texture texture)))
  
  (render! [_ pos data]
    (when-let [texture (:hologram-texture @render-state)]
      (let [{:keys [color scale pulse]} data]
        (.render-quad! texture-provider pos 
                      {:texture texture
                       :color color
                       :scale scale
                       :pulse pulse}))))
  
  (cleanup! [_]
    (swap! render-state dissoc :hologram-texture)))

;; Text renderer implementation
(defrecord TextRenderer [texture-provider]
  IRenderer
  (init! [_]
    (when-let [texture (.load-texture! texture-provider "font")]
      (swap! render-state assoc :font-texture texture)))
  
  (render! [_ pos data]
    (when-let [texture (:font-texture @render-state)]
      (let [{:keys [text color scale]} data]
        (.render-text! texture-provider pos text
                      {:texture texture
                       :color color
                       :scale scale}))))
  
  (cleanup! [_]
    (swap! render-state dissoc :font-texture)))

;; Renderer factory functions
(defn create-block-renderer [block texture-provider model-provider]
  (->BlockRenderer block texture-provider model-provider))

(defn create-hologram-renderer [texture-provider]
  (->HologramRenderer texture-provider))

(defn create-text-renderer [texture-provider]
  (->TextRenderer texture-provider))

;; Render queue management
(defn queue-render! [renderer pos data]
  (swap! render-state update :render-queue conj 
         {:renderer renderer :pos pos :data data}))

(defn process-render-queue! []
  (doseq [{:keys [renderer pos data]} (:render-queue @render-state)]
    (.render! renderer pos data))
  (swap! render-state assoc :render-queue []))

;; Initialize render system
(defn init-renderers! [texture-provider model-provider]
  (doseq [renderer (vals (:renderers @render-state))]
    (.init! renderer)))