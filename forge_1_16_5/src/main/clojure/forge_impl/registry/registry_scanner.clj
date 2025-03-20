(ns forge-impl.registry.registry-scanner
  (:require [mcmod.protocols :refer :all]
            [mcmod.util.registry-scanner :as mcmod-scanner]
            [forge-impl.registry.content-scanner :as content-scanner]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.registries ForgeRegistries]
           [net.minecraftforge.fml RegistryObject]
           [net.minecraft.util ResourceLocation]))

;; Track scanned content
(def ^:private scanned-content (atom {}))

;; Scan a mod namespace and register its content
(defn scan-mod! [mod-id]
  (log/info (str "Scanning mod: " mod-id))
  (let [content (mcmod-scanner/scan-mod-namespaces mod-id)]
    (swap! scanned-content assoc mod-id content)
    content))

;; Register scanned content with Forge
(defn register-scanned-content! [mod-id content]
  (log/info (str "Registering content for mod: " mod-id))
  
  ;; Register blocks
  (doseq [[name block] (:blocks content)]
    (content-scanner/register-block! mod-id (str name) block))
  
  ;; Register tile entities
  (doseq [[name te] (:tile-entities content)]
    (content-scanner/register-tile-entity! mod-id (str name) te))
  
  ;; Register containers
  (doseq [[name container] (:containers content)]
    (content-scanner/register-container! mod-id (str name) container)))

;; Main scanning function
(defn scan-and-register-mods! []
  (log/info "Starting mod content scanning")
  
  ;; Get all registered mods from mcmod
  (let [mods (mcmod-scanner/get-registered-mods)]
    (doseq [mod-id mods]
      (let [content (scan-mod! mod-id)]
        (register-scanned-content! mod-id content))))
  
  (log/info "Mod content scanning complete"))