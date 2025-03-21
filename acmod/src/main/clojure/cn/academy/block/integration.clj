(ns cn.academy.block.integration
  (:require [cn.academy.block.init :as init]
            [cn.academy.block.registry.init :as registry-init]
            [cn.academy.block.capability :as capability]
            [clojure.tools.logging :as log]))

;; Integration with mcmod system
(defn prepare-block-for-registration
  "Prepare a block instance for registration by adding capabilities"
  [block]
  (let [cap-provider (capability/create-capability-provider block)]
    (mcmod.capability/add-capability-provider! block cap-provider)
    block))

(defn register-block!
  "Register a block with all required capabilities and handlers"
  [id block]
  (let [prepared-block (prepare-block-for-registration block)]
    (mcmod.registry/register-block! id prepared-block)
    (when (get-in block [:properties :has-tile-entity])
      (mcmod.registry/register-tile-entity! id prepared-block))))

;; System initialization
(defn init!
  "Initialize the entire block system"
  []
  (try
    ;; Initialize core systems
    (registry-init/init!)
    (init/init-blocks!)
    
    ;; Add capability support
    (mcmod.registry/set-block-processor! prepare-block-for-registration)
    
    ;; Register block types
    (doseq [[id block-def] @cn.academy.block.blocks/block-registry]
      (register-block! id ((:constructor block-def) (:properties block-def))))
    
    (log/info "Academy block integration complete")
    true
    (catch Exception e
      (log/error "Failed to initialize block integration:" (.getMessage e))
      false)))