(ns mcmod.lifecycle
  (:require [mcmod.logging :as log]
            [mcmod.concurrent :as concurrent]
            [mcmod.cache :as cache])
  (:import [net.minecraftforge.fml.common Mod$EventBusSubscriber]
           [net.minecraftforge.fml.event.server ServerStoppingEvent]))

(defprotocol ILifecycle
  (start [this] "Called when the component starts")
  (stop [this] "Called when the component is stopping"))

(def lifecycle-components (atom []))

(defn register-lifecycle [component]
  (swap! lifecycle-components conj component))

(defn start-all []
  (doseq [component @lifecycle-components]
    (try
      (start component)
      (catch Exception e
        (log/error "Error starting component: %s" (.getMessage e))))))

(defn stop-all []
  (doseq [component @lifecycle-components]
    (try
      (stop component)
      (catch Exception e
        (log/error "Error stopping component: %s" (.getMessage e)))))
  (concurrent/shutdown))