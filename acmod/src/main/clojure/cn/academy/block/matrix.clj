(ns cn.academy.block.matrix
  (:require [clojure.spec.alpha :as s]
            [cn.academy.block.matrix-energy :as energy]
            [cn.academy.block.matrix-structure :as structure]))

(def matrix-structure
  [[0 0 0] [1 0 0]
   [0 1 0] [1 1 0]
   [0 0 1] [1 0 1]
   [0 1 1] [1 1 1]])

(defprotocol IMatrix
  "Core matrix functionality"
  (is-formed? [this])
  (form! [this])
  (break! [this])
  (get-energy-handler [this])
  (save-to-nbt [this])
  (load-from-nbt! [this nbt]))

(defprotocol IMatrixBlock
  "Matrix block behavior"
  (on-activated [this pos state player]
    "Handle block activation")
  (on-placed [this pos state placer]
    "Handle block placement")
  (get-properties [this]
    "Get block properties")
  (create-tile-entity [this pos]
    "Create a new tile entity"))

(defprotocol IMatrixTileEntity
  "Matrix tile entity behavior"
  (tick [this]
    "Update tile entity state")
  (get-matrix [this]
    "Get associated matrix")
  (get-capability [this capability side]
    "Get capability interface")
  (invalidate [this]
    "Handle tile entity invalidation"))

(defrecord Matrix [energy-handler formed? state]
  IMatrix
  (is-formed? [_] @formed?)
  
  (form! [_] 
    (reset! formed? true))
  
  (break! [_]
    (reset! formed? false))
  
  (get-energy-handler [_]
    energy-handler)
  
  (save-to-nbt [_]
    {"formed" @formed?
     "energy" (energy/save-to-nbt energy-handler)
     "state" @state})
  
  (load-from-nbt! [this nbt]
    (reset! formed? (get nbt "formed"))
    (reset! state (get nbt "state"))
    (energy/load-from-nbt! energy-handler (get nbt "energy")))

  IMatrixBlock
  (on-activated [_ pos state player]
    (when @formed?
      {:success true
       :should-consume true}))
  
  (on-placed [this pos state placer]
    (swap! state assoc :placer-name (str placer))
    {:success true})
  
  (get-properties [_]
    {:material :rock
     :hardness 3.0
     :resistance 3.0
     :light-level 1
     :has-tile-entity true})
  
  (create-tile-entity [this pos]
    {:matrix this
     :position pos
     :capabilities #{:energy :inventory}}))

(defrecord MatrixTileEntity [matrix pos capabilities]
  IMatrixTileEntity
  (tick [_]
    (when (is-formed? matrix)
      (energy/update! (get-energy-handler matrix))))
  
  (get-matrix [_] matrix)
  
  (get-capability [_ capability _]
    (when (contains? capabilities capability)
      (case capability
        :energy (get-energy-handler matrix)
        :inventory nil)))
  
  (invalidate [_]
    (break! matrix)))

(s/def ::core-level (s/and number? #(>= % 0)))
(s/def ::plate-count (s/and number? #(<= % 3)))
(s/def ::position (s/keys :req [:x :y :z]))
(s/def ::matrix-state (s/keys :req-un [::core-level ::plate-count ::position]))

(defn create-matrix []
  "Create a new matrix instance"
  (->Matrix (energy/create-energy-handler) 
           (atom false)
           (atom {:core-level 0
                 :plate-count 0
                 :position nil
                 :placer-name nil})))

(defn create-matrix-tile-entity [matrix pos]
  "Create a new matrix tile entity"
  (->MatrixTileEntity matrix pos #{:energy :inventory}))