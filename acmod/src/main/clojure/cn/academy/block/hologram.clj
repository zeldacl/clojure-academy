(ns cn.academy.block.hologram
  (:require [cn.academy.block.multiblock :as multiblock]
            [cn.academy.block.env :as env]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Hologram types
(def hologram-types
  {:structure-preview {:color [0.2 0.6 1.0 0.4]
                      :pulse true
                      :scale 1.0}
   :status-display {:color [0.0 1.0 0.0 0.8]
                   :pulse false
                   :scale 0.5}
   :error-indicator {:color [1.0 0.0 0.0 0.8]
                    :pulse true
                    :scale 0.5}})

;; Hologram state tracking
(def hologram-state
  (atom {:active-holograms {}
         :render-queue []}))

;; Hologram creation
(defprotocol IHologram
  (render [this world pos])
  (update! [this data])
  (dispose! [this]))

;; Hologram implementation
(defrecord StructureHologram [id pattern]
  IHologram
  (render [_ world pos]
    (let [components (multiblock/get-components pattern world pos)
          style (get hologram-types :structure-preview)]
      (doseq [component components]
        (let [comp-pos (mcmod.block/get-pos component)]
          (mcmod.client/render-hologram! 
           comp-pos 
           (:color style)
           (:scale style)
           (:pulse style))))))
  
  (update! [_ data]
    (swap! hologram-state assoc-in [:active-holograms id :data] data))
  
  (dispose! [_]
    (swap! hologram-state update :active-holograms dissoc id)))

(defrecord StatusHologram [id block metrics]
  IHologram
  (render [_ world pos]
    (let [style (get hologram-types :status-display)
          state @(:state block)
          status-text (apply format 
                            (:format metrics)
                            (map #(get state %) (:values metrics)))]
      (mcmod.client/render-text-hologram!
       pos
       status-text
       (:color style)
       (:scale style)
       (:pulse style))))
  
  (update! [this data]
    (swap! hologram-state assoc-in [:active-holograms id :data] data))
  
  (dispose! [_]
    (swap! hologram-state update :active-holograms dissoc id)))

;; Hologram management
(defn create-structure-preview! [world pos pattern]
  (let [id (str "structure-" (random-uuid))
        hologram (->StructureHologram id pattern)]
    (swap! hologram-state assoc-in [:active-holograms id] 
           {:type :structure
            :hologram hologram
            :world world
            :pos pos})
    id))

(defn create-status-display! [block metrics]
  (let [id (str "status-" (random-uuid))
        hologram (->StatusHologram id block metrics)]
    (swap! hologram-state assoc-in [:active-holograms id]
           {:type :status
            :hologram hologram
            :block block})
    id))

(defn remove-hologram! [id]
  (when-let [hologram (get-in @hologram-state [:active-holograms id :hologram])]
    (dispose! hologram)))

;; Rendering loop
(defn render-holograms! []
  (doseq [[id {:keys [hologram world pos]}] (:active-holograms @hologram-state)]
    (error/with-safe-execution id :hologram
      (render hologram world pos))))

;; Initialize hologram system
(defn init-holograms! []
  (mcmod.client/register-render-handler!
   (fn [partial-ticks]
     (render-holograms!))))