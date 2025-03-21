(ns cn.academy.block.hologram
  (:require [cn.academy.block.multiblock :as multiblock]
            [cn.academy.block.error :as error]
            [mcmod.protocols :refer [IRenderProvider IHologramRenderer]]
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

;; Hologram implementation
(defrecord StructureHologram [id pattern world pos]
  IHologramRenderer
  (render [_]
    (let [blocks (multiblock/get-components pattern world pos)
          style (get hologram-types :structure-preview)]
      (doseq [block blocks]
        (let [pos (mcmod.block/get-pos block)]
          (mcmod.render/render-hologram pos 
                                      (:color style)
                                      (:scale style)
                                      (:pulse style))))))
  
  (update-state! [_ data]
    (swap! hologram-state assoc-in [:active-holograms id :data] data))
  
  (cleanup! [_]
    (swap! hologram-state update :active-holograms dissoc id)))

(defrecord StatusHologram [id block metrics]
  IHologramRenderer
  (render [_]
    (let [style (get hologram-types :status-display)
          state @(:state block)
          status-text (apply format 
                            (:format metrics)
                            (map #(get state %) (:values metrics)))]
      (mcmod.render/render-text-hologram
        (mcmod.block/get-pos block)
        status-text
        (:color style)
        (:scale style)
        (:pulse style))))
  
  (update-state! [_ data]
    (swap! hologram-state assoc-in [:active-holograms id :data] data))
  
  (cleanup! [_]
    (swap! hologram-state update :active-holograms dissoc id)))

;; Hologram management
(defn create-structure-preview! [world pos pattern]
  (let [id (str "structure-" (random-uuid))
        hologram (->StructureHologram id pattern world pos)]
    (swap! hologram-state assoc-in [:active-holograms id] 
           {:type :structure
            :hologram hologram})
    id))

(defn create-status-display! [block metrics]
  (let [id (str "status-" (random-uuid))
        hologram (->StatusHologram id block metrics)]
    (swap! hologram-state assoc-in [:active-holograms id]
           {:type :status
            :hologram hologram})
    id))

(defn remove-hologram! [id]
  (when-let [hologram (get-in @hologram-state [:active-holograms id :hologram])]
    (.cleanup! hologram)))

;; Rendering loop
(defn render-holograms! [render-provider]
  (doseq [[id {:keys [hologram]}] (:active-holograms @hologram-state)]
    (error/with-safe-execution id :hologram
      (.render hologram))))

;; Initialize hologram system
(defn init-holograms! [render-provider]
  (.register-render-handler! render-provider render-holograms!))