(ns cn.academy.block.error
  (:require [mcmod.protocols :refer [IErrorHandler]]
            [cn.academy.block.event :as event]
            [clojure.tools.logging :as log]))

;; Error tracking state
(def error-state
  (atom {:active {}
         :history []}))

;; Error handler implementation
(defrecord ErrorTracker [id]
  IErrorHandler
  (add-error! [_ error]
    (swap! error-state update-in [:active id]
           (fnil conj #{}) error)
    (swap! error-state update :history conj 
           {:id id
            :error error
            :timestamp (System/currentTimeMillis)}))
  
  (clear-errors! [_]
    (swap! error-state update :active dissoc id))
  
  (get-active-errors [_]
    (get-in @error-state [:active id] #{}))
  
  (has-errors? [this]
    (not (empty? (get-active-errors this)))))

;; Error management functions
(defn get-error-tracker
  "Get or create error tracker for ID"
  [id]
  (->ErrorTracker id))

(defn set-error!
  "Set error for component"
  [id error]
  (let [tracker (get-error-tracker id)]
    (.add-error! tracker error)
    (event/post-machine-event! :machine/error
                              (mcmod.protocols/get-machine id)
                              {:error error})))

(defn clear-errors!
  "Clear errors for component"
  [id]
  (.clear-errors! (get-error-tracker id)))

(defn get-active-errors
  "Get active errors for component"
  [id]
  (.get-active-errors (get-error-tracker id)))

;; Error execution wrapper
(defmacro with-error-handling
  "Execute body with error handling for component"
  [id error-type & body]
  `(try
     ~@body
     (catch Exception e#
       (let [error# {:type ~error-type
                     :message (.getMessage e#)}]
         (set-error! ~id error#)
         (log/error "Error in" ~error-type ":" (.getMessage e#))
         nil))))

;; Error reporting
(defn get-error-history
  "Get error history for time period"
  [start-time end-time]
  (->> (:history @error-state)
       (filter #(and (>= (:timestamp %) start-time)
                    (<= (:timestamp %) end-time)))
       (sort-by :timestamp)))

;; Initialize error system
(defn init-error-system! []
  (reset! error-state {:active {}
                       :history []}))