(ns cn.li.bridge.matrix.structure
  (:require [clojure.tools.logging :as log]
            [cn.li.bridge.matrix.api :as api])
  (:import [net.minecraft.util.math BlockPos]
           [net.minecraft.block Block]
           [net.minecraft.world World]))

(def required-structure
  {:core {:pos [0 0 0]
          :block "academy:matrix_core"}
   :plates [{:pos [-1 0 0] :block "academy:matrix_plate"}
            {:pos [1 0 0] :block "academy:matrix_plate"}
            {:pos [0 0 -1] :block "academy:matrix_plate"}
            {:pos [0 0 1] :block "academy:matrix_plate"}]})

(defn get-relative-pos [base-pos [x y z]]
  (BlockPos. (+ (.getX base-pos) x)
             (+ (.getY base-pos) y)
             (+ (.getZ base-pos) z)))

(defn get-block-at [world pos]
  (.getBlock (.getBlockState world pos)))

(defn validate-block [world pos block-id]
  (let [block (get-block-at world pos)
        registry-name (.toString (.getRegistryName block))]
    (= registry-name block-id)))

(defn validate-matrix-structure
  "Validate the Matrix multiblock structure at the given position"
  [world pos]
  (try
    (let [core-valid? (validate-block world pos (:block (:core required-structure)))
          plates-valid? (every? (fn [{:keys [pos block]}]
                                 (let [plate-pos (get-relative-pos pos pos)]
                                   (validate-block world plate-pos block)))
                               (:plates required-structure))]
      (and core-valid? plates-valid?))
    (catch Exception e
      (log/error "Error validating matrix structure:" (.getMessage e))
      false)))

(defn get-structure-blocks
  "Get all block positions that are part of the matrix structure"
  [pos]
  (cons pos
        (map #(get-relative-pos pos (:pos %))
             (:plates required-structure))))

(defn is-part-of-structure?
  "Check if a position is part of a matrix structure"
  [base-pos check-pos]
  (some #(= check-pos (get-relative-pos base-pos (:pos %)))
        (conj (:plates required-structure)
              (:core required-structure))))

(defn- relative-to-absolute [base-pos [rx ry rz]]
  (BlockPos. (+ (.getX base-pos) rx)
             (+ (.getY base-pos) ry)
             (+ (.getZ base-pos) rz)))

(defrecord MatrixStructure [matrix]
  api/IMatrixStructure
  (validate-structure [_ world pos]
    (every? (fn [offset]
              (let [block-pos (relative-to-absolute pos offset)
                    block-state (.getBlockState world block-pos)]
                (.is block-state (Block/getBlockFromName "li:matrix_block"))))
            (api/get-structure matrix)))
  
  (form-structure [this world pos]
    (when (api/validate-structure this world pos)
      (let [blocks (api/get-structure-blocks this)]
        (doseq [block-pos blocks]
          (let [block-state (.getBlockState world block-pos)]
            (.setBlockState world block-pos 
                           (.with block-state (Block/getBlockFromName "li:matrix_formed") true)))))))
  
  (break-structure [_ world pos]
    (let [blocks (api/get-structure matrix)]
      (doseq [offset blocks]
        (let [block-pos (relative-to-absolute pos offset)
              block-state (.getBlockState world block-pos)]
          (.setBlockState world block-pos 
                         (.with block-state (Block/getBlockFromName "li:matrix_formed") false))))))

  (get-structure-blocks [_ world pos]
    (map #(relative-to-absolute pos %) 
         (api/get-structure matrix))))

(defn create-structure [matrix]
  (->MatrixStructure matrix))