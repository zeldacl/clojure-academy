(ns cn.academy.block.block-registry
  (:require [cn.academy.block.registration-impl :as reg-impl]
            [cn.academy.block.registry-def :as registry]))

(defn register-blocks! [mod-id]
  (let [registration (reg-impl/create-registration)]
    (registry/register-all! registration mod-id)))