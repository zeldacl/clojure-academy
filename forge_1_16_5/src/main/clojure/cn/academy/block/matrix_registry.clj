(ns cn.academy.block.matrix-registry
  (:require [cn.academy.block.block.block-matrix :as matrix]
            [cn.academy.block.registration-impl :as reg-impl]))

(defn register-matrix [mod-id]
  (let [registration (reg-impl/create-registration)]
    (matrix/register! registration mod-id)))