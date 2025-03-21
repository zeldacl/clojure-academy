(ns cn.academy.tech-system.energy-system.error-handling
  (:require [clojure.tools.logging :as log]
            [mcmod.protocols :as protocols]))

(def ^:private error-state
  (atom {:errors {}
         :warnings {}
         :last-error nil}))

(defprotocol IErrorHandler
  (handle-error! [this error context])
  (handle-warning! [this warning context])
  (get-last-error [this])
  (clear-errors! [this]))

(defrecord NetworkErrorHandler [state-atom]
  IErrorHandler
  (handle-error! [_ error context]
    (let [error-id (str (random-uuid))
          timestamp (System/currentTimeMillis)
          error-data {:id error-id
                     :timestamp timestamp
                     :error error
                     :context context}]
      (swap! state-atom update :errors assoc error-id error-data)
      (swap! state-atom assoc :last-error error-data)
      (log/error error "Network error occurred:" context)
      error-id))
  
  (handle-warning! [_ warning context]
    (let [warning-id (str (random-uuid))
          timestamp (System/currentTimeMillis)
          warning-data {:id warning-id
                       :timestamp timestamp
                       :warning warning
                       :context context}]
      (swap! state-atom update :warnings assoc warning-id warning-data)
      (log/warn warning "Network warning:" context)
      warning-id))
  
  (get-last-error [_]
    (:last-error @state-atom))
  
  (clear-errors! [_]
    (reset! state-atom {:errors {}
                       :warnings {}
                       :last-error nil})))

(defmacro with-error-handling
  "Execute body with error handling, capturing any exceptions"
  [handler context & body]
  `(try
     ~@body
     (catch Exception e#
       (handle-error! ~handler e# ~context)
       nil)))

(defmacro with-network-safety
  "Execute network operations with safety checks and error handling"
  [handler network & body]
  `(try
     (if (protocols/valid? ~network)
       (do ~@body)
       (handle-warning! ~handler "Invalid network state" {:network-id (:id ~network)}))
     (catch Exception e#
       (handle-error! ~handler e# {:network-id (:id ~network)})
       nil)))

(def error-handler (->NetworkErrorHandler error-state))

;; Error reporting functions
(defn get-error-report []
  (let [state @error-state
        error-count (count (:errors state))
        warning-count (count (:warnings state))]
    {:total-errors error-count
     :total-warnings warning-count
     :last-error (:last-error state)
     :errors (vals (:errors state))
     :warnings (vals (:warnings state))}))

(defn get-errors-since [timestamp]
  (->> (:errors @error-state)
       vals
       (filter #(> (:timestamp %) timestamp))))

(defn get-warnings-since [timestamp]
  (->> (:warnings @error-state)
       vals
       (filter #(> (:timestamp %) timestamp)))))