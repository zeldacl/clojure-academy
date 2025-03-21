(ns cn.academy.tech-system.energy-system.network.handler
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [mcmod.block :as block]
            [clojure.tools.logging :as log]))

(def ^:private handler-state
  (atom {:handlers {}
         :pending {}
         :channel nil}))

;; Message handling
(defmulti handle-message
  (fn [msg-type data world] msg-type))

(defmethod handle-message :node-destroyed
  [_ node world]
  (when-let [network (wireless/get-node-network node)]
    (wireless/remove-node! network node)))

(defmethod handle-message :update-energy
  [_ node amount _]
  (when (wireless/is-wireless-node? node)
    (wireless/set-energy! node amount)
    (optimization/optimize-update! node :energy amount)))

(defmethod handle-message :network-formed
  [_ matrix nodes _]
  (doseq [node nodes]
    (when (wireless/is-wireless-node? node)
      (wireless/connect! node matrix))))

(defmethod handle-message :network-broken
  [_ node _]
  (when (wireless/is-wireless-node? node)
    (wireless/disconnect! node)))

;; Public API
(defn send-message! [msg-type & args]
  (when-let [handler (get-in @handler-state [:handlers msg-type])]
    (try
      (apply handler args)
      (catch Exception e
        (log/error "Error sending message:" msg-type (.getMessage e))))))

(defn register-handler! [msg-type handler]
  (swap! handler-state assoc-in [:handlers msg-type] handler))

(defn set-network-channel! [channel]
  (reset! (:channel handler-state) channel))

;; Initialize network handlers
(defn init! []
  (doseq [[msg-type handler] {:node-destroyed handle-message
                             :update-energy handle-message
                             :network-formed handle-message
                             :network-broken handle-message}]
    (register-handler! msg-type handler))
  (log/info "Network handlers initialized"))