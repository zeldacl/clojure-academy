(ns cn.li.bridge.matrix.init
  (:require [cn.li.bridge.matrix.core :as matrix]
            [cn.li.bridge.matrix.bridge :as bridge]
            [cn.li.bridge.matrix.network :as network]
            [cn.li.bridge.matrix.container :as container]
            [cn.li.bridge.matrix.structure :as structure]
            [cn.li.bridge.registry.api :as registry]
            [clojure.tools.logging :as log]))

(def ^:const MATRIX_ID "matrix")

(defn register-matrix [registry-handler]
  (log/info "Registering Matrix components...")
  
  ;; Create core matrix instance
  (let [matrix-instance (matrix/create-matrix)
        matrix-structure (structure/create-structure matrix-instance)
        matrix-bridge (bridge/create-bridge)
        network-handler (network/create-network-handler matrix-instance)]
    
    ;; Register blocks and tile entities
    (bridge/register-matrix matrix-bridge 
                           registry-handler 
                           MATRIX_ID 
                           matrix-instance)
    
    ;; Register container type
    (container/register-container-type registry-handler)
    
    ;; Register network handlers
    (network/register-network-handlers registry-handler)
    
    ;; Return registered components
    {:matrix matrix-instance
     :structure matrix-structure
     :network network-handler}))

(defn initialize []
  (log/info "Initializing Matrix system..."))