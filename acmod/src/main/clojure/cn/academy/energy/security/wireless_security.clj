(ns cn.academy.energy.security.wireless-security
  (:require [cn.academy.energy.api.wireless :as wireless]
            [clojure.tools.logging :as log])
  (:import [java.security MessageDigest]
           [java.util Base64]
           [java.time LocalDateTime]))

(defprotocol ISecurityProvider
  (hash-password [this password])
  (verify-password [this password hashed])
  (log-access-attempt [this entity credentials success?])
  (get-access-log [this])
  (check-rate-limit [this entity]))

(defrecord AccessLogEntry [timestamp entity credentials success?])

(defrecord SecurityProvider [salt access-log rate-limits]
  ISecurityProvider
  (hash-password [_ password]
    (let [digest (MessageDigest/getInstance "SHA-256")
          salted (str password salt)
          hashed (.digest digest (.getBytes salted "UTF-8"))]
      (.encodeToString (Base64/getEncoder) hashed)))
  
  (verify-password [this password hashed]
    (= (hash-password this password) hashed))
  
  (log-access-attempt [_ entity credentials success?]
    (let [entry (->AccessLogEntry 
                  (LocalDateTime/now)
                  entity 
                  credentials
                  success?)]
      (swap! access-log conj entry)
      (when-not success?
        (log/warn (str "Failed access attempt from " entity " using credentials " credentials)))))
  
  (get-access-log [_]
    @access-log)
  
  (check-rate-limit [_ entity]
    (let [now (System/currentTimeMillis)
          attempts (get @rate-limits entity [])
          recent-attempts (filter #(> % (- now 300000)) attempts)] ; 5 minute window
      (if (< (count recent-attempts) 5)
        (do
          (swap! rate-limits update entity #(conj (or % []) now))
          true)
        false))))

(defn create-security-provider []
  (->SecurityProvider 
    (str (System/nanoTime)) ; Generate random salt
    (atom []) ; Access log
    (atom {}))) ; Rate limits

(defn secure-network! [network]
  (let [provider (create-security-provider)]
    (wireless/add-security-provider network provider)))