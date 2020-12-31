(ns cn.li.academy.ac-registry
  (:require                                                 ;[cn.li.mcmod.blocks :refer [node-struct]]
    [cn.li.academy.energy.blocks.node :refer [node-struct]]
    [cn.li.mcmod.global :refer [*mod-id*]]
            [cn.li.mcmod.registry :refer [init-deferred-register registry-block-struct]]
    ;[cn.li.mcmod.ui :refer [defcontainertype]]
            [clojure.tools.logging :as log])
  (:import (net.minecraft.block Block)))


(defn init-registry []
  (init-deferred-register *mod-id*)
  (registry-block-struct node-struct))

(init-registry)

;(def block-node-instance (instance-block block-node))
;;(.setRegistryName block-node-instance "basenode")
;;(log/info "12EEEEEEEEEEEEEEEEEEEE " (nil? block-node-instance)  "TTTTTTTTTTTTTT" (cn.li.academy.energy.blocks.node.BlockNode.))
;(registry-block block-node-instance true)
;
;(defcontainertype block-node-container-type block-node-instance (.getRegistryName ^Block block-node-instance))

