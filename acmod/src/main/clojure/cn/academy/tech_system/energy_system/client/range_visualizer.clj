(ns cn.academy.tech-system.energy-system.client.range-visualizer
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.wireless :as network]
            [mcmod.client.render :as render]
            [mcmod.math :as math]))

(def ^:private visualization-state
  (atom {:active-nodes #{}
         :show-ranges false}))

(defn toggle-range-visualization! []
  (swap! visualization-state update :show-ranges not))

(defn add-active-node! [node]
  (swap! visualization-state update :active-nodes conj node))

(defn remove-active-node! [node]
  (swap! visualization-state update :active-nodes disj node))

(defn- render-range-sphere! [pos range alpha]
  (let [x (:x pos)
        y (:y pos)
        z (:z pos)]
    (render/push-matrix)
    (render/translate x y z)
    (render/set-color 0.2 0.6 1.0 alpha)
    (render/enable-blend)
    (render/sphere range 32 32)
    (render/disable-blend)
    (render/pop-matrix)))

(defn- render-connection-line! [start-pos end-pos alpha]
  (render/push-matrix)
  (render/set-color 0.2 0.6 1.0 alpha)
  (render/enable-blend)
  (render/line-width 2.0)
  (render/begin-lines)
  (render/vertex (:x start-pos) (:y start-pos) (:z start-pos))
  (render/vertex (:x end-pos) (:y end-pos) (:z end-pos))
  (render/end)
  (render/line-width 1.0)
  (render/disable-blend)
  (render/pop-matrix))

(defn render-node-ranges! [partial-ticks]
  (when (:show-ranges @visualization-state)
    (doseq [node (:active-nodes @visualization-state)]
      (let [pos (wireless/get-position node)
            range (wireless/get-range node)
            alpha (+ 0.1 (* 0.1 (Math/sin (* (System/currentTimeMillis) 0.003))))]
        ;; Render range sphere
        (render-range-sphere! pos range alpha)
        
        ;; Render connections to other nodes in network
        (when-let [net-id (network/get-node-network (:id node))]
          (when-let [net (network/get-network net-id)]
            (doseq [other (network/get-nodes net)
                    :when (not= node other)]
              (render-connection-line! 
                (wireless/get-position node)
                (wireless/get-position other)
                (* alpha 0.7)))))))))