(ns cn.li.bridge.matrix.init
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-state :as state]
            [cn.li.bridge.matrix.bridge :as bridge]
            [cn.li.bridge.matrix.capability :as capability]
            [cn.li.bridge.matrix.block-component :as block]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]))

(def ^:const MATRIX_ID "wireless_matrix")

(defn register-matrix [registry-handler]
  (log/info "Registering Matrix bridge components...")
  
  ;; Create matrix instance using acmod implementation
  (let [matrix-instance (matrix/create-matrix)
        block-component (block/create-block-component matrix-instance)
        matrix-bridge (bridge/create-bridge)
        cap-provider (capability/create-capability-provider matrix-instance)]
    
    ;; Register with Forge
    (bridge/register-matrix matrix-bridge registry-handler MATRIX_ID matrix-instance)
    
    ;; Register event handlers
    (.register MinecraftForge/EVENT_BUS block-component)
    
    matrix-instance))

(defn initialize []
  (log/info "Initializing Matrix system..."))