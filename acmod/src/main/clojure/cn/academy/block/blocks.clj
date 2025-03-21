(ns cn.academy.block.blocks
  (:require [cn.academy.block.properties :as props]
            [cn.academy.block.api :as block-api]
            [clojure.tools.logging :as log]))

(def block-registry (atom {}))

(defn register-block-type! 
  "Register a block type with its base properties and constructor"
  [id {:keys [properties constructor] :as config}]
  (swap! block-registry assoc id config))

;; Block registration definitions
(register-block-type! 
  "cat_engine" 
  {:properties (props/get-default-properties :energy-machine)
   :constructor (fn [props]
                 {:type :cat-engine
                  :properties props})})

(register-block-type!
  "phase_gen"
  {:properties (props/get-default-properties :energy-machine)
   :constructor (fn [props]
                 {:type :phase-gen
                  :properties props})})

(register-block-type!
  "metal_former"
  {:properties (props/get-default-properties :processor)
   :constructor (fn [props]
                 {:type :metal-former
                  :properties props})})

(register-block-type!
  "imag_fusor"
  {:properties (props/get-default-properties :processor)
   :constructor (fn [props]
                 {:type :imag-fusor
                  :properties props})})

(defn create-block!
  "Create a block instance with the specified ID and optional property overrides"
  [id & [property-overrides]]
  (if-let [{:keys [properties constructor]} (get @block-registry id)]
    (let [final-props (props/merge-properties properties (or property-overrides {}))]
      (constructor final-props))
    (log/warn "No block type registered for ID:" id)))