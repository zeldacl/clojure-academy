(ns cn.academy.block.tick
  (:require [cn.academy.block.recipe.core :as recipes]
            [cn.academy.block.network.core :as network]
            [clojure.tools.logging :as log]))

;; Block tick handlers
(defmulti handle-tick :type)

;; Default handler
(defmethod handle-tick :default [block]
  nil)

;; Energy producer tick handling
(defmethod handle-tick :phase-generator [block]
  (let [state (:state block)
        config (:config block)]
    (when (:active @state)
      (let [output (* (:max-output config)
                     (:base-efficiency config)
                     (:efficiency @state))]
        (swap! state update :energy + output)
        (network/send-message! :update-energy block output)))))

(defmethod handle-tick :wind-generator [block]
  (let [state (:state block)
        config (:config block)]
    (when (:active @state)
      (let [output (:max-output config)]
        (swap! state update :energy + output)
        (network/send-message! :update-energy block output)))))

;; Processing machine tick handling
(defn handle-processor-tick [block]
  (let [state (:state block)
        inventory (mcmod.capability/get-capability block :inventory nil)]
    (if-let [current-recipe (:current-recipe @state)]
      ;; Continue processing current recipe
      (when (recipes/can-process? block current-recipe)
        (swap! state update :progress + 1)
        (when (>= (:progress @state) (:process-time current-recipe))
          (recipes/process-recipe! block current-recipe)
          (swap! state assoc 
                 :progress 0
                 :current-recipe nil)
          (network/send-message! :update-progress block 0)))
      
      ;; Try to start new recipe
      (when-let [recipe (recipes/find-matching-recipe 
                         (:type block)
                         (mcmod.inventory/get-contents inventory))]
        (when (recipes/can-process? block recipe)
          (swap! state assoc 
                 :current-recipe recipe
                 :progress 0)
          (network/send-message! :update-progress block 0))))))

(defmethod handle-tick :metal-former [block]
  (handle-processor-tick block))

(defmethod handle-tick :imag-fusor [block]
  (handle-processor-tick block))

;; Energy distribution handling
(defn distribute-energy! [block]
  (when-let [energy-cap (mcmod.capability/get-capability block :energy nil)]
    (let [connected (mcmod.energy/get-connected-blocks block)
          available (mcmod.energy/get-energy-stored energy-cap)]
      (when (pos? available)
        (let [per-block (quot available (count connected))]
          (doseq [target connected]
            (when-let [target-cap (mcmod.capability/get-capability target :energy nil)]
              (let [transferred (mcmod.energy/transfer-energy! energy-cap target-cap per-block)]
                (when (pos? transferred)
                  (network/send-message! :update-energy target transferred))))))))))

;; Main tick handler
(defn tick! [block]
  (try
    (handle-tick block)
    (when (get-in block [:config :distributes-energy])
      (distribute-energy! block))
    (catch Exception e
      (log/error "Error during block tick:" (.getMessage e)))))