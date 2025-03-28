(ns cn.mcmod.util.registry-scanner
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.protocols :refer :all]
            [cn.mcmod.util :as util]))

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
                       (all-ns))
        result (atom {:blocks {} :items {} :tile-entities {}})]
    (doseq [ns ns-list]
      (log/debug (str "Scanning namespace: " ns))
      (let [blocks (scan-namespace-for-blocks ns)
            items (scan-namespace-for-items ns)
            tile-entities (scan-namespace-for-tile-entities ns)]
        (swap! result update :blocks merge 
               (into {} (map (fn [v] [(-> v meta :name) v]) blocks)))
        (swap! result update :items merge 
               (into {} (map (fn [v] [(-> v meta :name) v]) items)))
        (swap! result update :tile-entities merge 
               (into {} (map (fn [v] [(-> v meta :name) v]) tile-entities)))))
    @result))

(defn register-scanned-content!
  "Register all scanned content with a registry"
  [registry mod-id content]
  (doseq [[name block] (:blocks content)]
    (util/register-mod-block registry mod-id (str name) @block))
  
  (doseq [[name item] (:items content)]
    (util/register-mod-item registry mod-id (str name) @item))
  
  (doseq [[name te] (:tile-entities content)]
    (util/register-mod-tile-entity registry mod-id (str name) @te)))

(defn scan-and-register-mod!
  "Scan and register all content from a mod's namespaces"
  [registry mod-id mod-ns]
  (log/info (str "Scanning mod namespace: " mod-ns))
  (let [content (scan-mod-namespaces mod-ns)]
    (register-scanned-content! registry mod-id content)
    content))