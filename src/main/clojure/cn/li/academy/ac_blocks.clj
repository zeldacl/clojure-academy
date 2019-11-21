(ns cn.li.academy.ac-blocks
  (:require [cn.li.mcmod.blocks :refer [instance-block]]
    ;[cn.li.academy.energy.blocks.node :refer [block-node]]
            [cn.li.mcmod.registry :refer [registry-block]]
            [cn.li.mcmod.ui :refer [defcontainertype]]
            [clojure.tools.logging :as log])
  (:import (net.minecraft.block Block)))


;(def block-node-instance (instance-block block-node))
;;(.setRegistryName block-node-instance "basenode")
;;(log/info "12EEEEEEEEEEEEEEEEEEEE " (nil? block-node-instance)  "TTTTTTTTTTTTTT" (cn.li.academy.energy.blocks.node.BlockNode.))
;(registry-block block-node-instance true)
;
;(defcontainertype block-node-container-type block-node-instance (.getRegistryName ^Block block-node-instance))

