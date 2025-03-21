(ns cn.academy.block.client.render
  (:require [clojure.tools.logging :as log]))

;; Render state management
(def render-state (atom {}))

(defn register-renderer! [block-id renderer-fn]
  (swap! render-state assoc block-id renderer-fn))

;; Default renderers
(def default-renderers
  {"phase_generator" (fn [block world pos]
                      (mcmod.client/render-rotating-block block world pos))
   
   "metal_former" (fn [block world pos]
                   (mcmod.client/render-machine block world pos))
   
   "imag_fusor" (fn [block world pos]
                 (mcmod.client/render-machine block world pos))
   
   "matrix_core" (fn [block world pos]
                  (when-let [state (get-in block [:state])]
                    (when (:formed @state)
                      (mcmod.client/render-multiblock-structure block world pos))))})

;; Initialize renderers
(defn init-renderers! []
  (doseq [[id renderer] default-renderers]
    (register-renderer! id renderer))
  true)

;; Rendering dispatch
(defn render-block! [block world pos]
  (when-let [renderer (get @render-state (:type block))]
    (try
      (renderer block world pos)
      (catch Exception e
        (log/error "Error rendering block:" (:type block) (.getMessage e))))))