(ns cn.academy.block.matrix-nbt
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IMatrixNBT
  (write-to-nbt [this tag])
  (read-from-nbt [this tag])
  (sync-from-nbt [this tag]))

(defrecord MatrixNBTHandler [matrix state network]
  IMatrixNBT
  (write-to-nbt [_ tag]
    (doto tag
      (assoc! "placer" (matrix/get-placer-name matrix))
      (assoc! "plates" (vec (get-in @state [:plates])))
      (assoc! "core" (get-in @state [:core]))
      (assoc! "network_id" (get-in @network [:network-id]))
      (assoc! "total_capacity" (get-in @state [:total-capacity]))
      (assoc! "current_bandwidth" (get-in @state [:current-bandwidth]))
      (assoc! "max_range" (get-in @state [:max-range]))))
  
  (read-from-nbt [_ tag]
    (matrix/set-placer! matrix (get tag "placer"))
    (swap! state merge
           {:plates (get tag "plates")
            :core (get tag "core")
            :total-capacity (get tag "total_capacity")
            :current-bandwidth (get tag "current_bandwidth")
            :max-range (get tag "max_range")})
    (swap! network assoc :network-id (get tag "network_id")))
  
  (sync-from-nbt [this tag]
    (read-from-nbt this tag)))

(defn create-nbt-handler [matrix state network]
  (->MatrixNBTHandler matrix state network))