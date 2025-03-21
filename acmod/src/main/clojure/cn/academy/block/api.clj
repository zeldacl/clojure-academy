(ns cn.academy.block.api
  (:require [clojure.tools.logging :as log]))

;; Block creation helpers
(defn create-block-state [id properties]
  {:id id
   :properties properties})

;; Re-export commonly used functions from mcmod
(def set-block-property! mcmod.block/set-block-property!)
(def get-block-property mcmod.block/get-block-property)
(def create-block! mcmod.block/create-block!)
(def register-block! mcmod.block/register-block!)
(def get-block mcmod.block/get-block)

;; Block state management helpers
(defn update-block-state! [world pos state-fn]
  (when-let [block (get-block world pos)]
    (let [current-state (get-block-property block)
          new-state (state-fn current-state)]
      (set-block-property! block new-state))))

(defn with-block-state [block property value]
  (assoc-in block [:properties property] value))