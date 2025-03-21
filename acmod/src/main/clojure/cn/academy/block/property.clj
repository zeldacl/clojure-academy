(ns cn.academy.block.property
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-property!
  "Creates and returns a new block property with error handling"
  [block-properties property-type property-name & args]
  (let [creator-fn (case property-type
                    :boolean block-api/create-boolean-property
                    :int block-api/create-integer-property)
        property (atom nil)]
    (try
      (reset! property (apply creator-fn block-properties property-name args))
      (catch Exception e
        (log/error (str "Failed to create " property-type " property " property-name ": ") (.getMessage e))))
    property))

(defn add-property!
  "Safely adds a property to a block container"
  [container property]
  (when property
    (try
      (.addProperty container @property)
      (catch Exception e
        (log/error "Failed to add property to block: " (.getMessage e))))))

(defn get-property-value
  "Gets the value of a property from block state"
  [state property]
  (when property
    (block-api/get-property state @property)))

(defn set-property-value
  "Sets the value of a property on block state"
  [state property value]
  (when property
    (block-api/with-property state @property value))))