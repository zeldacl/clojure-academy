(ns cn.academy.block.matrix.state)

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
      (swap! state-atom update :last-update + delta-time)))
  
  (is-formed? [_]
    (:formed? @state-atom))
  
  (try-form! [this]
    (when (and (pos? (get-core-level this))
               (>= (get-plate-count this) 3))
      (swap! state-atom assoc :formed? true)
      true))
  
  (break! [_]
    (swap! state-atom assoc :formed? false))

  (get-core-level [_]
    (get-in @state-atom [:core :level] 0))
  
  (get-plate-count [_]
    (count (get-in @state-atom [:plates] [])))
  
  (get-energy-stored [_]
    (get-in @state-atom [:energy :stored] 0))
  
  (get-energy-capacity [_]
    (get-in @state-atom [:energy :capacity] 0))
  
  (get-network-id [_]
    (get-in @state-atom [:network :id]))
  
  (handle-sync! [_ data]
    (swap! state-atom merge (select-keys data [:formed? :core :plates :energy :network]))))

(defn create-matrix-state [matrix config]
  (->MatrixState matrix 
                 (:inventory matrix)
                 (:network matrix)
                 (:energy matrix)
                 (atom {})))