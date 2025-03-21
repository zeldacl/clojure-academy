(ns cn.academy.tech-system.energy-system.registry
  (:require [cn.academy.blocks.block-node.config :as node-config]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.transfer :as transfer]
            [cn.academy.tech-system.energy-system.network.handler :as handler]
            [mcmod.registry :as registry]
            [clojure.tools.logging :as log]))

(defn init! []
  ;; Initialize network components
  (optimization/init!)
  (transfer/init!)
  (handler/init!)

  ;; Import node types from block system
  (doseq [[node-type config] node-config/node-types]
    (registry/register-node-type! node-type config))

  (log/info "Energy system registry initialized"))