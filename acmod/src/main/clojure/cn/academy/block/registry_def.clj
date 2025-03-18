(ns cn.academy.block.registry-def
  (:require [cn.academy.block.block.block-matrix :as matrix]
            [cn.academy.block.block.block-cat-engine :as cat-engine]
            [cn.academy.block.block.block-node :as node]))

;; Registry entries that will be used by all forge versions
(def registry-entries
  {:blocks
   {"matrix" {:def matrix/block-matrix-def
              :register matrix/register!}
    "cat_engine" {:def cat-engine/block-def
                  :register cat-engine/register!}
    "node_basic" {:def (node/create-node-def :basic)
                  :register node/register!}
    "node_standard" {:def (node/create-node-def :standard)
                    :register node/register!}
    "node_advanced" {:def (node/create-node-def :advanced)
                    :register node/register!}}})

(defn register-all! [registration mod-id]
  "Register all blocks with the given registration implementation"
  (doseq [[block-id entry] (get registry-entries :blocks)]
    ((:register entry) registration mod-id block-id (:def entry))))