(ns cn.li.bridge.event.api
  (:require [clojure.tools.logging :as log]))

(defprotocol IEventHandler
  "Event handler functionality"
  (handle-event [this event data] "Handle an event"))

(defprotocol IEventBus
  "Event bus functionality"
  (register-handler [this event-type handler] "Register event handler")
  (unregister-handler [this handler] "Unregister event handler")
  (post-event [this event data] "Post an event"))

(defn create-forge-bridge []
  nil) ;; Implement in bridge

(defn register-core-handlers [bridge]
  (log/info "Registering core event handlers"))