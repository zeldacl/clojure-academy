(ns cn.academy.blocks.block-matrix.utils
  "Utility functions for the wireless matrix block.
   Contains common helpers, validation, and error handling logic."
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]))

;; Common constants
(def ^:const MAX_PLATES 3)
(def ^:const ENERGY_UPDATE_INTERVAL 20) ;; ticks

;;; Error Handling

(defmacro with-error-handling
  "Execute body with error handling. Returns result or nil on error."
  [message & body]
  `(try
     ~@body
     (catch Exception e#
       (log/error ~message (.getMessage e#))
       (log/debug e# "Stack trace")
       nil)))

(defn safely
  "Safely execute a function with arguments, returns nil on error."
  [f & args]
  (try
    (apply f args)
    (catch Exception e
      (log/error "Error in safe function execution:" (.getMessage e))
      nil)))

;;; Validation Functions

(defn validate-item
  "Validate an item against a predicate function."
  [item predicate]
  (when (and item (predicate item))
    item))

(defn validate-energy
  "Validate energy amount, ensuring it's within bounds."
  [amount min max]
  (cond
    (< amount min) min
    (> amount max) max
    :else amount))

(defn validate-network-id
  "Validate a network ID."
  [network-id]
  (when (and network-id (not (str/blank? network-id)))
    network-id))

(defn validate-index
  "Validate an index against a range.
   Returns the index if valid, nil otherwise."
  [index max-index]
  (when (and (number? index) (>= index 0) (< index max-index))
    index))

;;; Calculation Functions

(defn calculate-energy-capacity
  "Calculate energy capacity based on core level."
  [base-capacity multiplier core-level]
  (int (* base-capacity multiplier core-level)))

(defn calculate-transfer-rate
  "Calculate transfer rate based on core level."
  [base-rate multiplier core-level]
  (int (* base-rate multiplier core-level)))

(defn calculate-energy-consumption
  "Calculate energy consumption based on core level and node count."
  [core-level node-count]
  (let [base-consumption 10
        core-factor 1.5
        node-factor 0.5]
    (int (+ (* base-consumption core-level core-factor)
            (* node-count node-factor)))))

(defn calculate-activation-energy
  "Calculate energy needed for activation."
  [core-level]
  (int (* 100 core-level)))

;;; Position and Coordinate Utilities

(defn pos->long
  "Convert a position map to a long value for serialization"
  [{:keys [x y z]}]
  (bit-or (bit-shift-left (bit-or (bit-shift-left (bit-and x 0x3FFFFFF) 26) 
                                  (bit-and z 0x3FFFFFF)) 26)
          (bit-and y 0x3FFFFFF)))

(defn long->pos
  "Convert a serialized long position back to a map"
  [value]
  (let [x (bit-shift-right value 52)
        z (bit-and (bit-shift-right value 26) 0x3FFFFFF)
        y (bit-and value 0x3FFFFFF)]
    {:x x :y y :z z}))

(defn calc-distance
  "Calculate distance between two positions."
  [pos1 pos2]
  (let [dx (- (:x pos1) (:x pos2))
        dy (- (:y pos1) (:y pos2))
        dz (- (:z pos1) (:z pos2))]
    (Math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))))

(defn in-range?
  "Check if a position is within range of another position."
  [pos1 pos2 range]
  (<= (calc-distance pos1 pos2) range))

(defn pos->string
  "Convert position to string representation."
  [pos]
  (str (:x pos) "," (:y pos) "," (:z pos)))

(defn string->pos
  "Convert string to position."
  [s]
  (let [[x y z] (str/split s #",")]
    {:x (Integer/parseInt x)
     :y (Integer/parseInt y)
     :z (Integer/parseInt z)}))

(defn random-sphere-point [center radius]
  "Generate random point on sphere surface"
  (let [theta (* 2 Math/PI (rand))
        phi (* Math/PI (rand))
        x (* radius (Math/sin phi) (Math/cos theta))
        y (* radius (Math/sin phi) (Math/sin theta))
        z (* radius (Math/cos phi))]
    {:x (+ (:x center) x)
     :y (+ (:y center) y)
     :z (+ (:z center) z)}))

(defn random-sphere-velocity [speed]
  "Generate random velocity vector"
  (let [point (random-sphere-point {:x 0 :y 0 :z 0} speed)]
    [(:x point) (:y point) (:z point)]))

(defn offset-random [pos range]
  "Offset position by random amount within range"
  {:x (+ (:x pos) (- (rand (* 2 range)) range))
   :y (+ (:y pos) (- (rand (* 2 range)) range))
   :z (+ (:z pos) (- (rand (* 2 range)) range))})

(defn relative-to-absolute [base-pos [rx ry rz]]
  "Convert relative coordinates to absolute position"
  {:x (+ (:x base-pos) rx)
   :y (+ (:y base-pos) ry)
   :z (+ (:z base-pos) rz)})

;;; State Management Helpers

(defn process-form-state-change
  "Process a change in formation state."
  [state was-formed is-formed]
  (if is-formed
    (do
      (log/info "Matrix formed")
      (set-active state true))
    (do
      (log/info "Matrix unformed")
      (set-active state false))))

;;; Item Validation

(defn item-matches?
  "Check if an item matches expected item type."
  [item item-type]
  (= (:type item) item-type))

(defn is-core-item?
  "Check if an item is a matrix core."
  [item]
  (item-matches? item :matrix-core))

(defn is-plate-item?
  "Check if an item is a matrix plate."
  [item]
  (item-matches? item :matrix-plate))

(defn get-item-level
  "Get level of an item (core or plate)."
  [item]
  (get item :level 0))

;;; Serialization Helpers

(defn serialize-item
  "Serialize an item to a map."
  [item]
  (when item
    {:type (:type item)
     :level (:level item)
     :meta (:meta item)}))

(defn deserialize-item
  "Deserialize an item from a map."
  [item-data]
  (when item-data
    (let [{:keys [type level meta]} item-data]
      {:type type
       :level (or level 1)
       :meta (or meta {})})))

;;; Asset and Resource Helpers

(defn get-texture-path
  "Get texture path for resource."
  [mod-id resource-name]
  (str mod-id ":textures/blocks/matrix/" resource-name ".png"))

(defn get-model-path
  "Get model path for resource."
  [mod-id resource-name]
  (str mod-id ":models/block/matrix/" resource-name ".json"))

;;; Config Functions

(defn deep-merge
  "Recursively merge maps."
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))