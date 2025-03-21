(ns cn.academy.block.multiblock
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.cache :as cache]
            [cn.academy.block.resource :as resource]
            [clojure.tools.logging :as log]))

;; Structure definitions
(def structure-registry (atom {}))

;; Structure pattern definition
(defprotocol IStructurePattern
  (matches? [this world pos])
  (get-components [this world pos])
  (validate-structure [this world pos]))

;; Structure registration
(defn register-structure! [id pattern]
  (swap! structure-registry assoc id pattern))

;; Pattern implementation
(defrecord MultiblockPattern [id blocks relative-positions validation-fn]
  IStructurePattern
  (matches? [_ world pos]
    (cache/with-cache pos [:multiblock-match id]
      (try
        (every? (fn [[block-type [dx dy dz]]]
                 (let [check-pos {:x (+ (:x pos) dx)
                                :y (+ (:y pos) dy)
                                :z (+ (:z pos) dz)}
                       block (mcmod.block/get-block-at world check-pos)]
                   (= (:type block) block-type)))
               (map vector blocks relative-positions))
        (catch Exception e
          (log/error "Error checking multiblock pattern:" (.getMessage e))
          false))))
  
  (get-components [_ world pos]
    (for [[block-type [dx dy dz]] (map vector blocks relative-positions)]
      (let [component-pos {:x (+ (:x pos) dx)
                          :y (+ (:y pos) dy)
                          :z (+ (:z pos) dz)}]
        (mcmod.block/get-block-at world component-pos))))
  
  (validate-structure [this world pos]
    (and (matches? this world pos)
         (if validation-fn
           (validation-fn (get-components this world pos))
           true))))

;; Structure creation helpers
(defn create-pattern
  "Create a new multiblock pattern"
  [id & {:keys [blocks positions validation]}]
  (->MultiblockPattern id blocks positions validation))

;; Default structure patterns
(def machine-patterns
  {"matrix" (create-pattern "matrix"
              :blocks [:matrix-core :matrix-component :matrix-component :matrix-component
                      :matrix-component :matrix-component :matrix-component :matrix-component]
              :positions [[0 0 0] [1 0 0] [-1 0 0] [0 1 0] 
                         [0 -1 0] [0 0 1] [0 0 -1] [1 1 0]]
              :validation #(every? (fn [block]
                                   (resource/can-accept? block :energy 1000))
                                 %))
   
   "energy_cell" (create-pattern "energy_cell"
                  :blocks [:energy-controller :energy-cell :energy-cell
                          :energy-cell :energy-cell]
                  :positions [[0 0 0] [1 0 0] [-1 0 0] 
                             [0 1 0] [0 -1 0]]
                  :validation #(let [total-capacity (reduce + (map (fn [block]
                                                                   (get-in block [:config :energy-capacity]))
                                                                 %))]
                               (>= total-capacity 50000)))})

;; Structure formation
(defn try-form-structure! [world pos]
  (error/with-safe-execution pos :multiblock
    (some (fn [[id pattern]]
            (when (validate-structure pattern world pos)
              (let [components (get-components pattern world pos)]
                (doseq [component components]
                  (swap! (:state component) assoc 
                         :in-multiblock true
                         :multiblock-id id
                         :controller-pos pos))
                {:id id :components components})))
          @structure-registry)))

;; Structure destruction
(defn break-structure! [world pos]
  (when-let [block (mcmod.block/get-block-at world pos)]
    (when-let [multiblock-id (get-in @(:state block) [:multiblock-id])]
      (when-let [pattern (get @structure-registry multiblock-id)]
        (let [components (get-components pattern world pos)]
          (doseq [component components]
            (swap! (:state component) dissoc :in-multiblock :multiblock-id :controller-pos))
          true)))))

;; Initialize multiblock system
(defn init-multiblock! []
  (doseq [[id pattern] machine-patterns]
    (register-structure! id pattern)))