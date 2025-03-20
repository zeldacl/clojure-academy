(ns cljacademy.registry.blocks
  (:require [cn.academy.block.matrix.registry :as matrix]))

(def blocks
  {:wireless-matrix (matrix/register-matrix-block)})

(defn get-block-info [block-id]
  (get blocks block-id))