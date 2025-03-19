(ns cn.li.bridge.matrix.state-manager
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.network-events :refer [->MatrixFormationEvent]]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]))

(defprotocol IStateManager
  "Protocol for managing Matrix state"
  (initialize [this] "Initialize matrix state")
  (validate-state [this] "Validate current state")
  (update-state [this] "Update matrix state")
  (handle-state-change [this old-state new-state] "Handle state transitions"))

(defrecord MatrixStateManager [matrix]
  IStateManager
  (initialize [_]
    (when (matrix/can-initialize? matrix)
      (matrix/initialize matrix)
      (log/debug "Matrix" matrix "initialized")))
  
  (validate-state [_]
    (let [valid? (matrix/validate-structure matrix)]
      (when-not valid?
        (matrix/invalidate matrix))
      valid?))
  
  (update-state [this]
    (let [old-state (matrix/get-state matrix)
          new-state (if (validate-state this)
                     (if (matrix/is-formed? matrix)
                       :formed
                       :unformed)
                     :invalid)]
      (when (not= old-state new-state)
        (handle-state-change this old-state new-state))))
  
  (handle-state-change [_ old-state new-state]
    (log/debug "Matrix" matrix "state changed from" old-state "to" new-state)
    (case new-state
      :formed (let [event (->MatrixFormationEvent matrix)]
                (when-not (.isCanceled (.post MinecraftForge/EVENT_BUS event))
                  (matrix/form matrix)))
      :unformed (matrix/unform matrix)
      :invalid (matrix/invalidate matrix))))

(defn create-state-manager [matrix]
  (->MatrixStateManager matrix))

(defn register-state-events! [manager event-bus]
  (.register event-bus manager))