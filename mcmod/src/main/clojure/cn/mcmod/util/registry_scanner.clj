(ns cn.mcmod.util.registry-scanner
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.protocols :refer :all]))

(defn scan-namespace-for-blocks
  "Scan namespace for block definitions"
  [ns]
  (filter #(satisfies? IBlock (var-get %))
          (vals (ns-publics ns))))

(defn scan-namespace-for-items
  "Scan namespace for item definitions"
  [ns]
  (filter #(satisfies? IItem (var-get %))
          (vals (ns-publics ns))))

(defn scan-namespace-for-tile-entities
  "Scan namespace for tile entity definitions"
  [ns]
  (filter #(satisfies? IBlockEntity (var-get %))
          (vals (ns-publics ns))))

(defn scan-mod-namespaces
  "Scan all namespaces under a mod's base namespace"
  [base-ns]
  (let [base-pattern (str "^" base-ns "\\.")
        ns-list (filter #(re-matches (re-pattern base-pattern) (str %))
                       (all-ns))]
    (doseq [ns ns-list]
      (log/debug (str "Scanning namespace: " ns))
      {:blocks (scan-namespace-for-blocks ns)
       :items (scan-namespace-for-items ns)
       :tile-entities (scan-namespace-for-tile-entities ns)})))

(defn register-scanned-content!
  "Register all scanned content with a registry"
  [registry content]
  (doseq [[name block] (:blocks content)]
    (register-block! registry (str name) @block))
  
  (doseq [[name item] (:items content)]
    (register-item! registry (str name) @item))
  
  (doseq [[name te] (:tile-entities content)]
    (register-tile-entity! registry (str name) @te)))

(defn scan-and-register-mod!
  "Scan and register all content from a mod's namespaces"
  [registry mod-ns]
  (log/info (str "Scanning mod namespace: " mod-ns))
  (let [content (scan-mod-namespaces mod-ns)]
    (register-scanned-content! registry content)))