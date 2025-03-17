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
  (is-formed? [this])
  (form! [this])
  (break! [this])
  (get-energy-handler [this])
  (save-to-nbt [this])
  (load-from-nbt! [this nbt]))

(defrecord Matrix [energy-handler formed?]
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
     "energy" (energy/save-to-nbt energy-handler)})
  
  (load-from-nbt! [this nbt]
    (reset! formed? (get nbt "formed"))
    (energy/load-from-nbt! energy-handler (get nbt "energy"))))

(defn create-matrix []
  (->Matrix (energy/create-energy-handler) (atom false)))

(s/def ::core-level (s/and number? #(>= % 0)))
(s/def ::plate-count (s/and number? #(<= % 3)))
(s/def ::position (s/keys :req [:x :y :z]))
(s/def ::matrix-state (s/keys :req-un [::core-level ::plate-count ::position]))