(ns cn.academy.block.common.property
  (:require [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-property-manager [property-name]
  (let [prop-atom (atom nil)]
    {:property-atom prop-atom
     :get-prop #(when @prop-atom (block-api/get-property % @prop-atom))
     :set-prop #(when @prop-atom (block-api/with-property %1 @prop-atom %2))
     :init! #(reset! prop-atom (block-api/create-boolean-property % property-name))
     :add-to-container! (fn [container]
                         (when @prop-atom
                           (try
                             (.addProperty container @prop-atom)
                             (catch Exception e
                               (log/error (str "Failed to add property " property-name ": ") (.getMessage e))))))}))

(defn create-facing-property-manager []
  (let [prop-atom (atom nil)]
    {:property-atom prop-atom
     :get-facing #(when @prop-atom (block-api/get-property % @prop-atom))
     :set-facing #(when @prop-atom (block-api/with-property %1 @prop-atom %2))
     :init! #(reset! prop-atom (block-api/create-integer-property % "facing" 0 5))
     :add-to-container! (fn [container]
                         (when @prop-atom
                           (try
                             (.addProperty container @prop-atom)
                             (catch Exception e
                               (log/error "Failed to add facing property: " (.getMessage e)))))}))