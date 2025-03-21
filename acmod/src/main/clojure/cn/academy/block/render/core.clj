(ns cn.academy.block.render.core
  (:require [mcmod.protocols :refer [IRenderProvider IRenderHandler]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Render state tracking
(def render-state
  (atom {:render-queue []
         :active-handlers {}}))

;; Core renderer implementation
(defrecord Renderer [id config state-atom]
  IRenderHandler
  (queue-render! [_ render-data]
    (swap! render-state update :render-queue conj 
           (assoc render-data :renderer id)))
  
  (clear-queue! [_]
    (swap! render-state update :render-queue
           (fn [queue] (remove #(= (:renderer %) id) queue))))
  
  (update! [this]
    (error/with-error-handling id :render
      (doseq [data (filter #(= (:renderer %) id) 
                          (:render-queue @render-state))]
        ((:render-fn config) data @state-atom))
      (.clear-queue! this))))

;; Hologram renderer
(defrecord HologramRenderer [id]
  IRenderHandler
  (queue-render! [_ data]
    (swap! render-state update :render-queue conj 
           (assoc data 
                  :renderer id
                  :type :hologram)))
  
  (clear-queue! [_]
    (swap! render-state update :render-queue
           (fn [queue] (remove #(and (= (:renderer %) id)
                                    (= (:type %) :hologram)) 
                             queue))))
  
  (update! [this]
    (error/with-error-handling id :hologram
      (doseq [{:keys [pos color scale pulse]} 
              (filter #(and (= (:renderer %) id)
                           (= (:type %) :hologram))
                     (:render-queue @render-state))]
        (mcmod.render/render-hologram pos color scale pulse))
      (.clear-queue! this))))

;; Block overlay renderer
(defrecord BlockOverlayRenderer [id]
  IRenderHandler
  (queue-render! [_ data]
    (swap! render-state update :render-queue conj
           (assoc data
                  :renderer id 
                  :type :overlay)))
  
  (clear-queue! [_]
    (swap! render-state update :render-queue  
           (fn [queue] (remove #(and (= (:renderer %) id)
                                    (= (:type %) :overlay))
                             queue))))
  
  (update! [this]
    (error/with-error-handling id :overlay
      (doseq [{:keys [pos block-type overlay-type]} 
              (filter #(and (= (:renderer %) id)
                           (= (:type %) :overlay))
                     (:render-queue @render-state))]
        (mcmod.render/render-block-overlay! pos block-type overlay-type))
      (.clear-queue! this))))

;; Renderer provider implementation  
(defrecord RenderProvider []
  IRenderProvider
  (register-renderer! [_ renderer]
    (swap! render-state assoc-in [:active-handlers (:id renderer)] renderer))
  
  (get-renderer [_ id]
    (get-in @render-state [:active-handlers id]))
  
  (update-renderers! [_]
    (doseq [renderer (vals (:active-handlers @render-state))]
      (.update! renderer))))

;; Create renderer provider instance
(def render-provider (->RenderProvider))

;; Helper functions for creating renderers
(defn create-hologram-renderer
  "Create a hologram renderer"
  [id]
  (let [renderer (->HologramRenderer id)]
    (.register-renderer! render-provider renderer)
    renderer))

(defn create-overlay-renderer
  "Create a block overlay renderer" 
  [id]
  (let [renderer (->BlockOverlayRenderer id)]
    (.register-renderer! render-provider renderer)
    renderer))

(defn create-custom-renderer
  "Create a custom renderer with render function"
  [id render-fn]
  (let [renderer (->Renderer id 
                            {:render-fn render-fn}
                            (atom {}))]
    (.register-renderer! render-provider renderer)
    renderer))

;; Initialize rendering system
(defn init-render-system! []
  (reset! render-state {:render-queue []
                        :active-handlers {}}))