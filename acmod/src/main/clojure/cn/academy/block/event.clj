(ns cn.academy.block.event
  (:require [cn.academy.block.network.core :as network]
            [cn.academy.block.gui.core :as gui]
            [clojure.tools.logging :as log]))

;; Event handlers registry
(def event-handlers (atom {}))

(defn register-handler! [event-type handler]
  (swap! event-handlers update event-type
         (fn [handlers] (conj (or handlers #{}) handler))))

;; Event dispatch
(defn dispatch-event! [event-type & args]
  (when-let [handlers (get @event-handlers event-type)]
    (doseq [handler handlers]
      (try
        (apply handler args)
        (catch Exception e
          (log/error "Error in event handler:" (.getMessage e)))))))

;; Block event handlers
(defn handle-block-activated [block world pos player hand]
  (dispatch-event! :block-activated block world pos player hand)
  (gui/open-gui! (:type block) player block world))

(defn handle-block-broken [block world pos]
  (dispatch-event! :block-broken block world pos)
  (when-let [state (:state block)]
    (network/send-message! :block-destroyed block)))

(defn handle-neighbor-changed [block world pos neighbor-pos]
  (dispatch-event! :neighbor-changed block world pos neighbor-pos))

;; Machine state event handlers
(defn handle-energy-changed [block old-energy new-energy]
  (dispatch-event! :energy-changed block old-energy new-energy)
  (network/send-message! :update-energy block new-energy))

(defn handle-progress-changed [block old-progress new-progress]
  (dispatch-event! :progress-changed block old-progress new-progress)
  (network/send-message! :update-progress block new-progress))

(defn handle-recipe-completed [block recipe]
  (dispatch-event! :recipe-completed block recipe))

;; Multiblock event handlers
(defn handle-structure-formed [controller members]
  (dispatch-event! :structure-formed controller members)
  (network/send-message! :multiblock-formed controller members))

(defn handle-structure-broken [controller members]
  (dispatch-event! :structure-broken controller members)
  (doseq [member members]
    (network/send-message! :multiblock-broken member)))

;; Register default handlers
(defn init-event-handlers! []
  ;; Block events
  (register-handler! :block-activated 
    (fn [block world pos player hand]
      (log/debug "Block activated:" (:type block))))
  
  (register-handler! :block-broken
    (fn [block world pos]
      (log/debug "Block broken:" (:type block))))
  
  ;; Machine events
  (register-handler! :energy-changed
    (fn [block old new]
      (log/debug "Energy changed for" (:type block) ":" old "->" new)))
  
  (register-handler! :progress-changed
    (fn [block old new]
      (log/debug "Progress changed for" (:type block) ":" old "->" new)))
  
  ;; Multiblock events  
  (register-handler! :structure-formed
    (fn [controller members]
      (log/debug "Multiblock structure formed:" (:type controller))))
  
  true)