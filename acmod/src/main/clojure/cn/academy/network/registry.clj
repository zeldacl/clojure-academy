(ns cn.academy.network.registry
  (:require [mcmod.protocols :refer [INetworkRegistry]]))

(def ^:private message-handlers (atom {}))

(defrecord NetworkRegistry []
  INetworkRegistry
  (register-message! [_ id message-type]
    (swap! message-handlers assoc id message-type)))

(defn create-registry []
  (->NetworkRegistry))

(defn get-message-handler [id]
  (get @message-handlers id))

(defn dispatch-message [id world player data]
  (when-let [handler (get-message-handler id)]
    (handler world player data)))