(ns cn.academy.block.matrix
  (:require [cn.academy.block.core :as block]
            [cn.academy.energy.core :as energy]))

(def matrix-properties
  (block/create-properties
    :hardness 3.0
    :resistance 3.0
    :light-level 15
    :material :stone))

(def ^:private matrix-structure
  [[-1 0 -1] [0 0 -1] [1 0 -1]
   [-1 0 0]  [0 0 0]  [1 0 0]
   [-1 0 1]  [0 0 1]  [1 0 1]])

(defrecord MatrixBlock [position id owner storage]
  block/IBlock
  (get-position [_] position)
  
  (get-properties [_] matrix-properties)
  
  (on-placed [this pos placer data]
    (assoc this 
           :position pos
           :owner (:id placer)))
  
  (on-removed [_ pos]
    (when-let [structure (get-structure pos)]
      (break-structure! structure)))
  
  (on-activated [this pos activator data]
    (when-not (:sneaking? activator)
      (when-let [center (get-center-pos pos)]
        {:action :open-gui
         :pos center})))
  
  (can-place? [_ pos]
    (valid-placement? pos))
  
  (can-remove? [_ pos]
    true)

  block/IBlockEntity  
  (load-data [this data]
    (assoc this
           :id (:id data)
           :owner (:owner data)
           :storage (:storage data)))
  
  (save-data [this]
    {:id id
     :owner owner
     :storage storage})
  
  (get-capabilities [_]
    [{:type :energy
      :handler (energy/create-handler storage)}])
  
  (mark-dirty [this]
    this)
  
  (on-load [this]
    (when-let [structure (try-form-structure position)]
      (register-structure! structure)))
  
  (on-unload [_]
    nil))

(defn create-matrix
  "Create a new matrix block instance"
  [& {:keys [position id owner]
      :or {position nil
           id (random-uuid)
           owner nil}}]
  (->MatrixBlock position id owner (atom {:energy 0
                                         :capacity 100000
                                         :max-transfer 1000})))

(defn get-structure
  "Get matrix structure positions relative to center"
  []
  matrix-structure)

(defn- valid-placement?
  "Check if matrix can be placed at position"
  [pos]
  (every? #(can-replace-block? (offset-pos pos %))
          matrix-structure))

(defn- try-form-structure
  "Attempt to form matrix structure at position"
  [pos]
  (when (valid-placement? pos)
    (for [offset matrix-structure]
      (offset-pos pos offset))))

(defn break-structure!
  "Break matrix multiblock structure"
  [structure]
  (doseq [pos structure]
    (remove-block! pos)))

(defn get-energy-level
  "Get current energy level 0-1"
  [matrix]
  (let [storage (:storage matrix)]
    (/ (:energy @storage)
       (:capacity @storage))))

(defn get-energy-stored
  "Get stored energy amount"
  [matrix]
  (get-in @(:storage matrix) [:energy]))

(defn get-energy-capacity
  "Get maximum energy capacity"
  [matrix]
  (get-in @(:storage matrix) [:capacity]))