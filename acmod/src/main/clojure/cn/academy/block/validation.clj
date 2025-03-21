(ns cn.academy.block.validation
  (:require [clojure.spec.alpha :as s]
            [clojure.tools.logging :as log]))

;; Block state specs
(s/def ::energy (s/and number? #(>= % 0)))
(s/def ::progress (s/and number? #(>= % 0) #(<= % 1)))
(s/def ::active boolean?)
(s/def ::efficiency (s/and number? #(>= % 0) #(<= % 1)))

;; Machine state validation
(s/def ::machine-state
  (s/keys :req-un [::energy ::active]
          :opt-un [::progress ::efficiency]))

;; Block configuration specs
(s/def ::energy-capacity (s/and number? pos?))
(s/def ::max-output (s/and number? #(>= % 0)))
(s/def ::base-efficiency (s/and number? #(> % 0) #(<= % 1)))
(s/def ::work-speed (s/and number? pos?))
(s/def ::inventory-size (s/and integer? pos?))

(s/def ::machine-config
  (s/keys :req-un [::energy-capacity]
          :opt-un [::max-output ::base-efficiency ::work-speed ::inventory-size]))

;; Validation functions
(defn validate-state! [state spec]
  (try
    (if (s/valid? spec @state)
      true
      (do (log/warn "Invalid state:" (s/explain-str spec @state))
          false))
    (catch Exception e
      (log/error "State validation error:" (.getMessage e))
      false)))

(defn validate-config! [config spec]
  (try
    (if (s/valid? spec config)
      true
      (do (log/warn "Invalid config:" (s/explain-str spec config))
          false))
    (catch Exception e
      (log/error "Config validation error:" (.getMessage e))
      false)))

;; Block validation helpers
(defn validate-machine! [block]
  (and (validate-state! (:state block) ::machine-state)
       (validate-config! (:config block) ::machine-config)))

;; State update validation
(defn validate-update! [state key value spec]
  (try
    (if (s/valid? spec value)
      true
      (do (log/warn "Invalid update:" key (s/explain-str spec value))
          false))
    (catch Exception e
      (log/error "Update validation error:" (.getMessage e))
      false)))

;; Update validators map
(def update-validators
  {:energy ::energy
   :progress ::progress
   :active ::active
   :efficiency ::efficiency})

(defn safe-update! [state key value]
  (if-let [spec (get update-validators key)]
    (when (validate-update! state key value spec)
      (swap! state assoc key value)
      true)
    (do (swap! state assoc key value)
        true)))