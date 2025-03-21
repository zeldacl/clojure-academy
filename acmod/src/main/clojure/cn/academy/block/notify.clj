(ns cn.academy.block.notify
  (:require [mcmod.protocols :refer [INotifier]]
            [cn.academy.block.error :as error]
            [cn.academy.block.config :as config]
            [cn.academy.block.monitor :as monitor]
            [clojure.tools.logging :as log]))

;; Notification state tracking
(def notify-state
  (atom {:handlers {}
         :history []
         :muted-categories #{}}))

;; Notification types & priorities
(def notification-types
  {:error 0
   :warning 1 
   :info 2
   :debug 3})

;; Notification implementation
(defrecord BlockNotification [type category message timestamp data])

;; Notifier implementation 
(defrecord BlockNotifier [state-atom]
  INotifier
  (notify! [_ type category message & [data]]
    (when-not ((:muted-categories @state-atom) category)
      (let [notification (->BlockNotification type category message 
                                            (System/currentTimeMillis) data)]
        ;; Add to history
        (swap! state-atom update :history conj notification)
        
        ;; Send to registered handlers
        (doseq [[_ handler] (get-in @state-atom [:handlers category])]
          (try
            (handler notification)
            (catch Exception e
              (log/error "Handler failed:" (.getMessage e)))))
        
        ;; Log based on type
        (case type
          :error (log/error message)
          :warning (log/warn message)
          :info (log/info message)
          :debug (log/debug message))
        
        notification)))
  
  (register-handler! [_ category id handler]
    (swap! state-atom assoc-in [:handlers category id] handler))
  
  (unregister-handler! [_ category id]
    (swap! state-atom update-in [:handlers category] dissoc id))
  
  (get-history [_]
    (:history @state-atom))
  
  (clear-history! [_]
    (swap! state-atom assoc :history []))
  
  (mute-category! [_ category]
    (swap! state-atom update :muted-categories conj category))
  
  (unmute-category! [_ category]
    (swap! state-atom update :muted-categories disj category)))

;; Factory functions
(defn create-notifier []
  (->BlockNotifier notify-state))

;; Default handlers
(def default-handlers
  {:system {"console" (fn [notification]
                       (println (format "[%s] %s: %s"
                                      (name (:type notification))
                                      (:category notification)
                                      (:message notification))))
           
           "metric-alert" (fn [notification]
                          (when (= (:category notification) :metric)
                            (monitor/record-metric! "notification-count" 
                                                  :system 1)))}
   
   :player {"chat" (fn [notification]
                    (when (#{:error :warning :info} (:type notification))
                      (mcmod.chat/send-message (:message notification))))}
   
   :machine {"log" (fn [notification]
                    (spit "logs/machine.log"
                          (str (pr-str notification) "\n")
                          :append true))}})

;; History pruning
(defn prune-history! []
  (let [max-size (config/get-config [:system :notification-history-size] 1000)
        current-size (count (:history @notify-state))]
    (when (> current-size max-size)
      (let [to-remove (- current-size max-size)]
        (swap! notify-state update :history
               #(vec (drop to-remove %)))))))

;; Public API functions
(defn notify!
  ([message] (notify! :info :system message))
  ([type message] (notify! type :system message))
  ([type category message] 
   (.notify! (create-notifier) type category message)))

(defn error! [message & [data]]
  (.notify! (create-notifier) :error :system message data))

(defn warn! [message & [data]]
  (.notify! (create-notifier) :warning :system message data))

(defn info! [message & [data]]
  (.notify! (create-notifier) :info :system message data))

(defn debug! [message & [data]]
  (.notify! (create-notifier) :debug :system message data))

(defn get-notifications [& [category]]
  (let [history (:history @notify-state)]
    (if category
      (filter #(= (:category %) category) history)
      history)))

;; Initialize notification system
(defn init-notify! []
  (reset! notify-state {:handlers default-handlers
                       :history []
                       :muted-categories #{}})
  
  ;; Register cleanup task
  (future
    (while true
      (Thread/sleep (* 1000 60))  ; Run every minute
      (try
        (prune-history!)
        (catch Exception e
          (log/error "Failed to prune notification history:"
                    (.getMessage e)))))))