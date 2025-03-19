(ns cn.li.bridge.matrix.registry
  (:require [cn.li.bridge.matrix.energy-events :as energy-events]
            [cn.li.bridge.matrix.network-events :as network-events]
            [cn.li.bridge.matrix.network-adapter :as network-adapter]
            [cn.li.bridge.matrix.state-manager :as state-manager]
            [cn.li.bridge.matrix.energy-transfer :as energy-transfer]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]))

(defprotocol IMatrixRegistry
  "Protocol for managing Matrix components"
  (register-matrix! [this matrix] "Register a new matrix")
  (unregister-matrix! [this matrix] "Unregister a matrix")
  (get-matrix [this id] "Get matrix by ID")
  (get-all-matrices [] "Get all registered matrices"))

(defrecord MatrixRegistry [event-bus network-handler]
  IMatrixRegistry
  (register-matrix! [_ matrix]
    (let [energy-handler (energy-events/create-energy-handler matrix)
          network-handler (network-events/create-network-handler matrix)
          state-manager (state-manager/create-state-manager matrix)
          network-adapter (network-adapter/create-network-adapter matrix network-handler)]
      
      ; Register event handlers
      (energy-events/register-energy-events! energy-handler event-bus)
      (network-events/register-network-events! network-handler event-bus)
      (state-manager/register-state-events! state-manager event-bus)
      
      ; Initialize matrix
      (state-manager/initialize state-manager)
      
      (log/info "Registered matrix:" matrix)))
  
  (unregister-matrix! [_ matrix]
    (.unregister event-bus matrix)
    (log/info "Unregistered matrix:" matrix))
  
  (get-matrix [_ id]
    (let [matrices @matrix-registry]
      (get matrices id)))
  
  (get-all-matrices [_]
    (vals @matrix-registry)))

(def ^:private matrix-registry (atom {}))

(defn create-registry [event-bus network-handler]
  (->MatrixRegistry event-bus network-handler))

(defn register-matrix! [registry matrix]
  (swap! matrix-registry assoc (matrix/get-id matrix) matrix)
  (.register-matrix! registry matrix))

(defn unregister-matrix! [registry matrix]
  (swap! matrix-registry dissoc (matrix/get-id matrix))
  (.unregister-matrix! registry matrix))