(ns cn.academy.block.matrix-state
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-inventory :as inv]
            [cn.academy.block.matrix-network :as network]
            [cn.academy.block.matrix-energy :as energy]))

(defprotocol IMatrixState
  (initialize! [this])
  (update! [this delta-time])
  (is-formed? [this])
  (try-form! [this])
  (break! [this])
  (get-core-level [this])
  (get-plate-count [this])
  (get-energy-stored [this])
  (get-energy-capacity [this])
  (get-network-id [this])
  (handle-sync! [this data]))

(defrecord MatrixState [matrix inventory network energy state-atom]
  IMatrixState
  (initialize! [_]
    (reset! state-atom {:formed? false
                       :last-update 0}))

  (update! [_ delta-time]
    (when (is-formed? _)
      (swap! state-atom update :last-update + delta-time)
      (energy/update-energy! energy)))

  (is-formed? [_]
    (:formed? @state-atom))

  (try-form! [this]
    (when (and (pos? (get-core-level this))
               (>= (get-plate-count this) 3))
      (swap! state-atom assoc :formed? true)
      true))

  (break! [_]
    (swap! state-atom assoc :formed? false)
    (network/leave-network network))

  (get-core-level [_]
    (let [core-item (inv/get-core-item inventory)]
      (if (.isEmpty core-item)
        0
        (.getCount core-item))))

  (get-plate-count [_]
    (inv/get-plate-count inventory))

  (get-energy-stored [_]
    (energy/get-energy-stored energy))

  (get-energy-capacity [_]
    (energy/get-energy-capacity energy))

  (get-network-id [_]
    (network/get-network-id network))

  (handle-sync! [_ data]
    (swap! state-atom merge (select-keys data [:formed?]))))

(defn create-matrix-state [matrix inventory network energy]
  (->MatrixState matrix inventory network energy (atom {:formed? false
                                                      :last-update 0})))