(ns cn.mcmod.util.diagnostics
  (:require [cn.mcmod.logging :as log]
            [cn.mcmod.registry :as registry])
  (:import [java.io File PrintWriter]
           [java.util Date]))

(defn collect-runtime-info []
  {:java-version (System/getProperty "java.version")
   :os-name (System/getProperty "os.name")
   :os-version (System/getProperty "os.version")
   :available-processors (.. Runtime getRuntime availableProcessors)
   :max-memory (.. Runtime getRuntime maxMemory)
   :free-memory (.. Runtime getRuntime freeMemory)
   :total-memory (.. Runtime getRuntime totalMemory)})

(defn collect-registry-stats [registry]
  {:block-count (count @(:blocks registry))
   :item-count (count @(:items registry))
   :tile-entity-count (count @(:tile-entities registry))
   :entity-count (count @(:entities registry))
   :mod-block-counts (into {} (map (fn [[mod-id blocks]] 
                                   [mod-id (count blocks)])
                                @(:mod-blocks registry)))
   :mod-item-counts (into {} (map (fn [[mod-id items]] 
                                  [mod-id (count items)])
                               @(:mod-items registry)))})

(defn collect-loaded-namespaces []
  (sort (map str (all-ns))))

(defn write-section [writer title content]
  (.println writer (str "=== " title " ==="))
  (.println writer "")
  (cond
    (string? content) (.println writer content)
    (map? content) (doseq [[k v] content]
                     (.println writer (format "%s: %s" k v)))
    (coll? content) (doseq [item content]
                      (.println writer (str item)))
    :else (.println writer (str content)))
  (.println writer ""))

(defn generate-diagnostics-report [file]
  (try
    (let [registry (registry/get-global-registry)
          runtime-info (collect-runtime-info)
          registry-stats (collect-registry-stats registry)
          loaded-ns (collect-loaded-namespaces)]
      
      (with-open [writer (PrintWriter. file)]
        (.println writer "=== Academy Mod Diagnostic Report ===")
        (.println writer (str "Generated: " (Date.)))
        (.println writer "")
        
        (write-section writer "Runtime Information" runtime-info)
        (write-section writer "Registry Statistics" registry-stats)
        (write-section writer "Loaded Namespaces" loaded-ns)
        
        (log/info "Diagnostic report written to %s" (.getPath file))))
    (catch Exception e
      (log/error e "Failed to generate diagnostic report"))))