(ns cn.academy.blocks.block_matrix.core
  (:require [cn.academy.blocks.block_matrix.registry :as registry]
            [cn.academy.blocks.block_matrix.config :as config]
            [cn.academy.blocks.block_matrix.state :as state]
            [cn.academy.blocks.block_matrix.wireless-matrix :as wireless-matrix]
            [cn.academy.blocks.block_matrix.events :as events]
            [cn.academy.blocks.block_matrix.energy :as energy]
            [cn.academy.tech-system.energy-system.api :as energy-api]
            [clojure.tools.logging :as log]))

;; Matrix initialization
(defn init! []
  (let [matrix-config (config/create-config)
        matrix-registry (registry/create-registry "academy" matrix-config)]
    
    ;; Register the block with Minecraft
    (registry/register-all matrix-registry)
    
    ;; Register with energy system
    (energy-api/register-energy-node-type! 
      :matrix 
      {:max-energy (-> matrix-config :energy :base-capacity)
       :bandwidth (-> matrix-config :network :base-bandwidth)
       :range (-> matrix-config :network :base-range)})
    
    (log/info "Wireless Matrix Block initialized")))

;; Module exports
(def create-wireless-matrix wireless-matrix/create-wireless-matrix)
(def create-matrix-state state/create-matrix-state)
(def create-matrix-energy energy/create-energy)
(def create-event-dispatcher events/create-event-dispatcher)