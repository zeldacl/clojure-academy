(ns cn.academy.block.init
  (:require [cn.academy.block.impl.energy :as energy]
            [cn.academy.block.impl.processor :as processor]
            [cn.academy.block.impl.multiblock :as multiblock]
            [cn.academy.block.blocks :as blocks]
            [clojure.tools.logging :as log]))

(defn init-blocks!
  "Initialize all block systems"
  []
  (try
    ;; Load block implementations
    (require 'cn.academy.block.impl.energy)
    (require 'cn.academy.block.impl.processor)
    (require 'cn.academy.block.impl.multiblock)
    
    ;; Initialize block registries
    (blocks/init-registry!)
    
    ;; Register blocks with Minecraft
    (doseq [[id block-def] @blocks/block-registry]
      (mcmod.registry/register-block! id ((:constructor block-def) (:properties block-def))))
    
    (log/info "Academy block systems initialized")
    true
    (catch Exception e
      (log/error "Failed to initialize block systems:" (.getMessage e))
      false)))