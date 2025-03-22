(ns cn.academy.blocks.block-node.init
  (:require [cn.academy.blocks.block-node.registry :as registry]
            [clojure.tools.logging :as log]))

(defn init! []
  ;; Registration functionality moved to registry.clj
  (registry/register-node-types!)
  (log/info "Block node system initialized"))