(ns cn.academy.block.matrix
  "Core matrix block implementation"
  (:require [cn.academy.block.matrix.state :as state]
            [cn.academy.block.matrix.inventory :as inv]
            [cn.academy.block.matrix.energy :as energy]
            [cn.academy.block.matrix.network :as network]))

(defprotocol IMatrix
  "Core matrix block protocol"
  (get-position [this])
  (get-placer-name [this])
  (set-placer! [this name])
  (is-formed? [this])
  (is-valid? [this])
  (get-core-level [this])
  (get-plate-count [this])
  (get-energy-stored [this])
  (get-energy-capacity [this])
  (get-range [this])
  (get-bandwidth [this])
  (working? [this]))

(defrecord Matrix [id position core plates state network storage]
  IMatrix
  (get-position [_] position)
  (get-placer-name [_] (:placer @state))
  (set-placer! [_ name] (swap! state assoc :placer name))
  (is-formed? [_] (state/is-formed? @state))
  (is-valid? [_] (and (pos? (get-core-level _))
                      (>= (get-plate-count _) 3)))
  (get-core-level [_] (:level @core))
  (get-plate-count [_] (count @plates))
  (get-energy-stored [_] (:stored @storage))
  (get-energy-capacity [_] (:capacity @storage))
  (get-range [_] (:range @state))
  (get-bandwidth [_] (:bandwidth @network))
  (working? [this] (and (is-formed? this)
                        (pos? (get-energy-stored this)))))

(defn create-matrix 
  "Create new matrix instance"
  [& {:keys [id position] 
      :or {id (str (random-uuid))
           position {:x 0 :y 0 :z 0}}}]
  (->Matrix id
           position
           (atom {:level 0})
           (atom [])
           (atom {:placer nil
                :formed? false
                :range 16.0})
           (atom {:network-id nil
                :bandwidth 1000})
           (atom {:stored 0
                :capacity 100000})))