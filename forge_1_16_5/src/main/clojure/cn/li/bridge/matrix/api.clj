(ns cn.li.bridge.matrix.api
  (:require [cn.li.bridge.matrix.registry :as registry]
            [cn.li.bridge.matrix.energy-events :as energy]
            [cn.li.bridge.matrix.network-events :as network]
            [cn.li.bridge.matrix.state-manager :as state]
            [cn.li.bridge.matrix.energy-transfer :as transfer]
            [cn.li.bridge.matrix.event-coordinator :as coordinator]
            [clojure.tools.logging :as log]))

(defprotocol IMatrix
  "Core Matrix functionality"
  (get-id [this] "Get unique matrix ID")
  (get-pos [this] "Get matrix position")
  (get-state [this] "Get current state")
  (get-energy-stored [this] "Get stored energy")
  (get-energy-capacity [this] "Get energy capacity")
  (can-receive? [this] "Check if can receive energy")
  (can-extract? [this] "Check if can extract energy") 
  (receive-energy [this amount simulate] "Receive energy")
  (extract-energy [this amount simulate] "Extract energy")
  (can-form? [this] "Check if can form")
  (is-formed? [this] "Check if formed")
  (validate-structure [this] "Validate structure")
  (get-sync-data [this] "Get sync data")
  (handle-sync [this data] "Handle sync data"))

(defn create-matrix
  "Create a new Matrix instance"
  [id pos]
  (let [event-bus (MinecraftForge/EVENT_BUS)
        coordinator (coordinator/create-coordinator event-bus)
        matrix (reify IMatrix
                (get-id [_] id)
                (get-pos [_] pos)
                (get-state [_] @state)
                (get-energy-stored [_] @energy)
                (get-energy-capacity [_] capacity)
                (can-receive? [_] (and (is-formed? matrix) (< @energy capacity)))
                (can-extract? [_] (and (is-formed? matrix) (pos? @energy)))
                (receive-energy [_ amount simulate]
                  (when (can-receive? matrix)
                    (let [space (- capacity @energy)
                          accepted (min amount space)]
                      (when-not simulate
                        (swap! energy + accepted)
                        (coordinator/coordinate-energy-update coordinator matrix))
                      accepted)))
                (extract-energy [_ amount simulate]
                  (when (can-extract? matrix)
                    (let [available @energy
                          extracted (min amount available)]
                      (when-not simulate
                        (swap! energy - extracted)
                        (coordinator/coordinate-energy-update coordinator matrix))
                      extracted)))
                (can-form? [_] 
                  (validate-structure matrix))
                (is-formed? [_] 
                  (= @state :formed))
                (validate-structure [_]
                  (structure/validate-matrix-structure pos))
                (get-sync-data [_]
                  {:energy @energy
                   :state @state})
                (handle-sync [_ data]
                  (reset! energy (:energy data))
                  (reset! state (:state data))))
        state (atom :unformed)
        energy (atom 0)
        capacity 100000]
    (coordinator/register-coordinator! coordinator)
    matrix))

(defn register-matrix!
  "Register a Matrix with the system"
  [matrix]
  (registry/register-matrix! registry/matrix-registry matrix))

(defn unregister-matrix!
  "Unregister a Matrix from the system"
  [matrix]
  (registry/unregister-matrix! registry/matrix-registry matrix))

(defn get-matrix
  "Get a Matrix by ID"
  [id]
  (registry/get-matrix registry/matrix-registry id))

(defn get-all-matrices
  "Get all registered Matrices"
  []
  (registry/get-all-matrices registry/matrix-registry))

(defn transfer-energy!
  "Transfer energy between matrices"
  [source target amount]
  (transfer/transfer-energy! source target amount))

(defn distribute-energy!
  "Distribute energy from source to multiple targets"
  [source targets amount]
  (transfer/distribute-energy! source targets amount))