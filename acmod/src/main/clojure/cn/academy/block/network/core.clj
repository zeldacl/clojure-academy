(ns cn.academy.block.network.core
  (:require [clojure.tools.logging :as log]))

;; Message handlers
(def message-handlers (atom {}))

(defn register-handler! [msg-type handler-fn]
  (swap! message-handlers assoc msg-type handler-fn))

;; Message definitions
(def block-messages
  {:update-energy {:encode (fn [block amount]
                           (mcmod.network/encode-message
                             {:type :update-energy
                              :pos (mcmod.block/get-pos block)
                              :amount amount}))
                   :handle (fn [msg world]
                           (when-let [block (mcmod.block/get-block-at world (:pos msg))]
                             (swap! (:state block) update :energy + (:amount msg))))}
   
   :update-progress {:encode (fn [block progress]
                            (mcmod.network/encode-message
                              {:type :update-progress
                               :pos (mcmod.block/get-pos block)
                               :progress progress}))
                    :handle (fn [msg world]
                            (when-let [block (mcmod.block/get-block-at world (:pos msg))]
                              (swap! (:state block) assoc :progress (:progress msg))))}
   
   :multiblock-formed {:encode (fn [controller members]
                              (mcmod.network/encode-message
                                {:type :multiblock-formed
                                 :controller-pos (mcmod.block/get-pos controller)
                                 :member-positions (map mcmod.block/get-pos members)}))
                      :handle (fn [msg world]
                              (let [controller (mcmod.block/get-block-at world (:controller-pos msg))
                                    members (map #(mcmod.block/get-block-at world %) (:member-positions msg))]
                                (swap! (:state controller) assoc 
                                  :formed true
                                  :members (set members))))}})

;; Initialize networking
(defn init-networking! []
  (doseq [[msg-type {:keys [handle]}] block-messages]
    (register-handler! msg-type handle))
  (mcmod.network/register-handlers! @message-handlers)
  true)

;; Network message dispatch
(defn send-message! [msg-type & args]
  (when-let [{:keys [encode]} (get block-messages msg-type)]
    (try
      (let [msg (apply encode args)]
        (mcmod.network/send-message! msg))
      (catch Exception e
        (log/error "Error sending message:" msg-type (.getMessage e))))))