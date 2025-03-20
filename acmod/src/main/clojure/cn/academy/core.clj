(ns cn.academy.core
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cn.academy.registry :as registry]
            [mcmod.protocols :refer :all]
            [mcmod.registry :as mcr]
            [cn.academy.registration :as reg]))

(def MOD-ID "acmod")
(def version "1.0.0")
(def debug true)

(def ^:dynamic *registry-provider* nil)
(def ^:dynamic *network-bridge* nil)
(def ^:dynamic *capability-bridge* nil)

;; Core initialization
(defn init! [registry-provider network-bridge capability-bridge]
  (binding [*registry-provider* registry-provider
            *network-bridge* network-bridge
            *capability-bridge* capability-bridge]
    
    (log/info "Initializing AcademyCraft core module")
    
    ;; Initialize registries
    (let [registry (create-registry registry-provider MOD-ID)]
      (reg/init-registration! registry))
    
    ;; Setup networking
    (when network-bridge
      (let [channel (create-channel network-bridge MOD-ID)]
        ;; Register network messages here
        ))
        
    ;; Register capabilities
    (when capability-bridge
      ;; Register mod capabilities here
      )))

;; Common setup (called on both client and server)
(defn setup-common! []
  (log/info "Setting up common components"))

;; Client-only setup 
(defn setup-client! []
  (log/info "Setting up client components"))

;; Server-only setup
(defn setup-server! []
  (log/info "Setting up server components"))

;; Export Java-accessible initialization method
(gen-class
  :name cn.academy.Core
  :methods [^:static [init [] void]]
  :prefix "core-")

(defn core-init []
  (init! *registry-provider* *network-bridge* *capability-bridge*))