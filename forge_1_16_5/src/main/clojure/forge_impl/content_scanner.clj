(ns forge-impl.content-scanner
  (:require [mcmod.protocols :refer :all]
            [forge-impl.registry-scanner :as registry])
  (:import [net.minecraftforge.fml ModList]))

(defn scan-mcmod-blocks []
  (try 
    (require 'cn.academy.block.registry)
    (let [reg-ns (find-ns 'cn.academy.block.registry)]
      (when-let [blocks (some-> reg-ns (ns-resolve 'registered-blocks) deref)]
        blocks))
    (catch Exception e
      (println "Error scanning mcmod blocks:" (.getMessage e))
      {})))

(defn scan-mcmod-tile-entities []
  (try
    (require 'cn.academy.block.registry)
    (let [reg-ns (find-ns 'cn.academy.block.registry)]
      (when-let [tes (some-> reg-ns (ns-resolve 'registered-tile-entities) deref)]
        tes))
    (catch Exception e
      (println "Error scanning mcmod tile entities:" (.getMessage e))
      {})))

(defn register-scanned-content! []
  (let [blocks (scan-mcmod-blocks)
        tile-entities (scan-mcmod-tile-entities)]
    
    ;; Register blocks
    (doseq [[block-id block] blocks]
      (registry/register-block! "mcmod" block-id block))
    
    ;; Register tile entities for blocks that have them
    (doseq [[block-id [te-type te]] tile-entities
            :let [block (get blocks block-id)]]
      (when block
        (registry/register-tile-entity! "mcmod" te-type te block)))))