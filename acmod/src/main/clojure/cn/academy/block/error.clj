(ns cn.academy.block.error
  (:require [mcmod.protocols :refer [IErrorHandler]]
            [clojure.tools.logging :as log]))

;; Error state tracking
(def error-state
  (atom {:active-errors {}
         :error-history []}))

;; Error handler implementation
(defrecord BlockErrorHandler [block]
  IErrorHandler
  (handle-error! [_ error]
    (let [error-id (str (random-uuid))
          error-data {:id error-id
                     :block-id (:id block)
                     :type (:type error)
                     :message (:message error)
                     :timestamp (System/currentTimeMillis)}]
      (swap! error-state assoc-in [:active-errors error-id] error-data)
      (swap! error-state update :error-history conj error-data)
      (log/error "Block error:" (:message error))
      error-id))
  
  (clear-error! [_ error-id]
    (swap! error-state update :active-errors dissoc error-id))
  
  (get-active-errors [_]
    (filter #(= (:block-id %) (:id block))
           (vals (:active-errors @error-state))))
  
  (has-errors? [this]
    (not (empty? (.get-active-errors this)))))

;; Error construction
(defn create-error
  ([type message]
   {:type type
    :message message
    :timestamp (System/currentTimeMillis)})
  ([type message cause]
   (assoc (create-error type message)
          :cause cause)))

;; Error categories
(def error-types
  {:validation "Validation Error"
   :resource "Resource Error" 
   :network "Network Error"
   :state "State Error"
   :operation "Operation Error"})

;; Error handling macro
(defmacro with-error-handling [error-type & body]
  `(try
     ~@body
     (catch Exception e#
       (log/error "Error during" ~error-type ":" (.getMessage e#))
       (create-error ~error-type (.getMessage e#) e#))))

;; Safe execution wrapper
(defmacro with-safe-execution [block-id category & body]
  `(try
     ~@body
     (catch Exception e#
       (let [error# (create-error ~category (.getMessage e#) e#)]
         (log/error "Error in block" ~block-id ":" (.getMessage e#))
         (when-let [handler# (mcmod.block/get-error-handler ~block-id)]
           (.handle-error! handler# error#))))))

;; Error monitoring
(defn get-error-count [block-id]
  (count (filter #(= (:block-id %) block-id)
                (vals (:active-errors @error-state)))))

(defn get-error-history [block-id]
  (filter #(= (:block-id %) block-id)
          (:error-history @error-state)))

;; Error cleanup
(defn clear-old-errors! []
  (let [current-time (System/currentTimeMillis)
        timeout (* 5 60 1000)] ; 5 minutes
    (swap! error-state update :active-errors
           #(into {} (filter (fn [[_ error]]
                              (< (- current-time (:timestamp error))
                                 timeout))
                            %)))))

;; Common error messages
(def error-messages
  {:insufficient-energy "Insufficient energy"
   :invalid-upgrade "Invalid upgrade type"
   :missing-resources "Missing required resources"
   :invalid-state "Invalid machine state"
   :network-disconnected "Network connection lost"})

;; Initialize error system
(defn init-errors! []
  (reset! error-state {:active-errors {}
                       :error-history []}))