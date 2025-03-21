(ns cn.academy.block.event
  (:require [mcmod.protocols :refer [IEventBus]]
            [clojure.tools.logging :as log]))

;; Event bus instance
(def event-bus
  (let [handlers (atom {})]
    (reify IEventBus
      (register-handler [_ event-type handler]
        (swap! handlers update event-type 
               (fnil conj #{}) handler))
      
      (post-event [_ event]
        (when-let [handlers (get @handlers (:type event))]
          (doseq [handler handlers]
            (try
              (handler event)
              (catch Exception e
                (log/error "Error in event handler:" (.getMessage e))))))))))

;; Event posting helpers
(defn post-machine-event!
  "Post machine-related event"
  [type machine & [data]]
  (.post-event event-bus
    (merge {:type type
            :machine machine
            :timestamp (System/currentTimeMillis)}
           data)))

(defn post-structure-event!
  "Post multiblock structure event"
  [type controller & [data]]
  (.post-event event-bus
    (merge {:type type
            :controller controller
            :timestamp (System/currentTimeMillis)}
           data)))

;; Standard event handlers
(defn register-standard-handlers! []
  ;; Machine events
  (.register-handler event-bus :machine/created
    (fn [{:keys [machine]}]
      (log/debug "Machine created:" (mcmod.protocols/get-id machine))))
  
  (.register-handler event-bus :machine/destroyed
    (fn [{:keys [machine]}]
      (log/debug "Machine destroyed:" (mcmod.protocols/get-id machine))))
  
  (.register-handler event-bus :machine/error
    (fn [{:keys [machine error]}]
      (log/error "Machine error:" (mcmod.protocols/get-id machine) "-" error)))
  
  ;; Structure events  
  (.register-handler event-bus :structure/formed
    (fn [{:keys [controller members]}]
      (log/debug "Structure formed with" (count members) "members")))
  
  (.register-handler event-bus :structure/broken
    (fn [{:keys [controller]}]
      (log/debug "Structure broken"))))