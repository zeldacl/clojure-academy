(ns cn.academy.blocks.block-node.init
  (:require [cn.academy.tech-system.energy-system.api :as energy-api]
            [cn.academy.blocks.block-node.config :as node-config]
            [clojure.tools.logging :as log]))

(defn register-node-types! []
  ;; Register our node types with the energy system
  (let [node-types {:basic {:max-energy 5000
                           :range 8
                           :max-connections 4
                           :bandwidth 500}
                    
                    :standard {:max-energy 20000
                             :range 16
                             :max-connections 8
                             :bandwidth 1000}
                    
                    :advanced {:max-energy 100000
                             :range 32
                             :max-connections 16
                             :bandwidth 2000}}]
    (doseq [[type config] node-types]
      (energy-api/register-energy-node-type! type config))
    (log/info "Registered" (count node-types) "node types with energy system")))

(defn init! []
  (register-node-types!)
  (log/info "Block node system initialized"))