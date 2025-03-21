(ns cn.academy.forge_1_15_2.blocks.matrix-block
  (:require [cn.academy.blocks.block_matrix.core :as matrix-core]
            [cn.academy.blocks.block_matrix.registry :as registry]
            [cn.academy.blocks.block_matrix.events :as events]
            [cn.academy.tech-system.energy-system.network.distribution :as energy-network]
            [clojure.tools.logging :as log]))

;; Forge implementation for the Wireless Matrix block
(defn register-matrix-block! [registry]
  (let [matrix-def (registry/get-matrix-definition)]
    (.registerBlock registry 
                    (:id matrix-def)
                    (fn []
                      (matrix-core/create-wireless-matrix)))
    
    (.registerTileEntity registry
                         (str (:id matrix-def) "_tile")
                         (fn [pos]
                           {:matrix (matrix-core/create-wireless-matrix)
                            :state (matrix-core/create-matrix-state)
                            :events (matrix-core/create-event-dispatcher)}))
    
    (log/info "Registered Wireless Matrix block for Forge 1.15.2")))

;; Forge adapter for the matrix energy network
(defn connect-to-energy-network! [matrix-tile]
  (let [matrix (:matrix matrix-tile)
        pos (get-position matrix)
        storage (get-energy-storage matrix)
        node (energy-network/create-node! pos storage)]
    
    ;; Store node in the matrix
    (swap! (:state matrix) assoc :energy-node node)
    
    ;; Return the node for reference
    node))