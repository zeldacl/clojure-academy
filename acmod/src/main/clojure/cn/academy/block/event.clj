(ns cn.academy.block.event
  (:require [cn.academy.block.network.core :as network]
            [cn.academy.block.gui.core :as gui]
            [mcmod.protocols :refer [IEventHandler IEventBus]]
            [clojure.tools.logging :as log]))

;; Event bus implementation
(def event-bus
  (let [handlers (atom {})]
    (reify IEventBus
      (register-handler [_ event-type handler]
        (swap! handlers update event-type 
               (fn [existing] (conj (or existing #{}) handler))))
      
      (post-event [_ event]
        (when-let [handlers (get @handlers (:type event))]
          (doseq [handler handlers]
            (try
              (handler event)
              (catch Exception e
                (log/error "Error in event handler:" (.getMessage e))))))))))

;; Block event handling
(defn handle-block-activated [block world pos player hand]
  (let [event {:type :block-activated
               :block block
               :world world
               :pos pos
               :player player
               :hand hand}]
    (.post-event event-bus event)
    (gui/open-gui! (:type block) player block world)))

(defn handle-block-broken [block world pos]
  (let [event {:type :block-broken
               :block block 
               :world world
               :pos pos}]
    (.post-event event-bus event)
    (when-let [state (:state block)]
      (network/send-message! :block-destroyed block))))

(defn handle-neighbor-changed [block world pos neighbor-pos]
  (let [event {:type :neighbor-changed
               :block block
               :world world
               :pos pos
               :neighbor-pos neighbor-pos}]
    (.post-event event-bus event)))

;; Machine event handling
(defn handle-energy-changed [block old-energy new-energy]
  (let [event {:type :energy-changed
               :block block
               :old-value old-energy
               :new-value new-energy}]
    (.post-event event-bus event)
    (network/send-message! :update-energy block new-energy)))

(defn handle-progress-changed [block old-progress new-progress]
  (let [event {:type :progress-changed
               :block block
               :old-value old-progress
               :new-value new-progress}]
    (.post-event event-bus event)
    (network/send-message! :update-progress block new-progress)))

(defn handle-recipe-completed [block recipe]
  (let [event {:type :recipe-completed
               :block block
               :recipe recipe}]
    (.post-event event-bus event)))

;; Multiblock event handling
(defn handle-structure-formed [controller members]
  (let [event {:type :structure-formed
               :controller controller
               :members members}]
    (.post-event event-bus event)
    (network/send-message! :multiblock-formed controller members)))

(defn handle-structure-broken [controller members]
  (let [event {:type :structure-broken
               :controller controller
               :members members}]
    (.post-event event-bus event)
    (doseq [member members]
      (network/send-message! :multiblock-broken member))))

;; Register default handlers
(defn init-event-handlers! []
  (doto event-bus
    (.register-handler :block-activated
      (fn [{:keys [block]}]
        (log/debug "Block activated:" (:type block))))
    
    (.register-handler :block-broken
      (fn [{:keys [block]}]
        (log/debug "Block broken:" (:type block))))
    
    (.register-handler :energy-changed
      (fn [{:keys [block old-value new-value]}]
        (log/debug "Energy changed for" (:type block) ":" old-value "->" new-value)))
    
    (.register-handler :progress-changed
      (fn [{:keys [block old-value new-value]}]
        (log/debug "Progress changed for" (:type block) ":" old-value "->" new-value)))
    
    (.register-handler :structure-formed
      (fn [{:keys [controller]}]
        (log/debug "Multiblock structure formed:" (:type controller))))))