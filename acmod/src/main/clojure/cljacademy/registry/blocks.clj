(ns cljacademy.registry.blocks
  (:require [cljacademy.blocks.matrix :as matrix]))

(def blocks
  {:wireless-matrix {:create-fn matrix/create-matrix-block
                    :tile-entity? true
                    :gui? true}})

(defn get-block-info [block-id]
  (get blocks block-id))