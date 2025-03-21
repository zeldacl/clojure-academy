(ns cn.academy.block.registry.init
  (:require [cn.academy.block.blocks :as blocks]
            [cn.academy.block.properties :as props]
            [clojure.tools.logging :as log]))

;; Initialize block registry with common functions
(defn init-registry-functions! []
  (mcmod.registry/register-provider! 
    :block-provider 
    {:create-block blocks/create-block!
     :get-block blocks/get-block
     :register-block! blocks/register-block-type!})
  
  (mcmod.registry/register-provider!
    :property-provider
    {:get-properties props/get-default-properties
     :merge-properties props/merge-properties}))

;; Initialize tile entity providers
(defn init-tile-entities! []
  (mcmod.registry/register-provider!
    :tile-provider
    {:create-tile-entity (fn [id world meta]
                          (when-let [block-def (blocks/get-block-type id)]
                            ((:constructor block-def) (:properties block-def))))
     :load-tile-entity (fn [id nbt]
                        (when-let [block-def (blocks/get-block-type id)]
                          ((:load-nbt block-def) nbt)))}))

(defn init!
  "Initialize all block registry systems"
  []
  (try
    (init-registry-functions!)
    (init-tile-entities!)
    (log/info "Block registry systems initialized")
    true
    (catch Exception e
      (log/error "Failed to initialize block registry:" (.getMessage e))
      false)))