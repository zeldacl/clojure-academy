(ns cn.mcmod.lifecycle
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.monitoring :as monitoring]))

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
        (log/error-ex e "Error starting component")))))

(defn stop-all []
  (doseq [component @lifecycle-components]
    (try
      (stop component)
      (catch Exception e
        (log/error-ex e "Error stopping component")))))

(defn unregister-lifecycle [component]
  (swap! lifecycle-components 
         (fn [components]
           (vec (remove #(identical? % component) components)))))

(defmacro with-lifecycle [component & body]
  `(try
     (register-lifecycle ~component)
     (start ~component)
     ~@body
     (finally
       (stop ~component)
       (unregister-lifecycle ~component))))

(defn register-shutdown-hook []
  (.addShutdownHook (Runtime/getRuntime)
                   (Thread. ^Runnable stop-all)))
