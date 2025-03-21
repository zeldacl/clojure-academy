(ns cn.academy.block.cache
  (:require [cn.academy.block.env :as env]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Cache configuration
(def cache-config
  {:max-size 10000
   :ttl 60000  ; 60 seconds
   :cleanup-interval 300})  ; 5 minutes

;; Cache state
(def cache-state
  (atom {:entries {}
         :access-times {}
         :size 0}))

;; Cache entry validation
(defn valid-entry? [entry]
  (let [now (System/currentTimeMillis)]
    (< (- now (:timestamp entry)) (:ttl cache-config))))

;; Cache management
(defn cache-get [block-id key]
  (when-let [entry (get-in @cache-state [:entries block-id key])]
    (when (valid-entry? entry)
      (swap! cache-state assoc-in [:access-times block-id key] 
             (System/currentTimeMillis))
      (:value entry))))

(defn cache-put! [block-id key value]
  (let [now (System/currentTimeMillis)]
    (swap! cache-state
           (fn [state]
             (-> state
                 (assoc-in [:entries block-id key]
                          {:value value
                           :timestamp now})
                 (assoc-in [:access-times block-id key] now)
                 (update :size inc))))))

;; Cache invalidation
(defn invalidate-cache! [block-id]
  (let [entries (get-in @cache-state [:entries block-id])]
    (swap! cache-state
           (fn [state]
             (-> state
                 (update :entries dissoc block-id)
                 (update :access-times dissoc block-id)
                 (update :size - (count entries)))))))

;; Cache cleanup
(defn cleanup-cache! []
  (let [now (System/currentTimeMillis)
        ttl (:ttl cache-config)]
    (swap! cache-state
           (fn [state]
             (->> state
                  :entries
                  (remove (fn [[_ entries]]
                           (every? #(valid-entry? (val %)) entries)))
                  (reduce (fn [state [block-id _]]
                           (-> state
                               (update :entries dissoc block-id)
                               (update :access-times dissoc block-id)))
                         state))))))

;; Cache decorators
(defmacro with-cache [block-id key & body]
  `(if-let [cached# (cache-get ~block-id ~key)]
     cached#
     (let [result# (do ~@body)]
       (cache-put! ~block-id ~key result#)
       result#)))

;; Initialize cache system
(defn init-cache! []
  (let [cleanup-interval (:cleanup-interval cache-config)]
    (mcmod.scheduler/schedule-periodic
      cleanup-interval
      cleanup-cache!)))