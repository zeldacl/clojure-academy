(ns cn.academy.mod
  (:require [cn.academy.block.registry-def :as registry]))

(def MOD-ID "academy")

(defprotocol IModSetup
  "Protocol for mod initialization across different Forge versions"
  (setup-common! [this] "Common setup for both client and server")
  (setup-client! [this] "Client-only setup")
  (register-all! [this] "Register all content with Forge"))

(defprotocol IRegistrationHandler
  "Protocol for handling block/item registration"
  (create-registration [this] "Create registration implementation for this Forge version")
  (register-deferred! [this mod-bus] "Register deferred registries with mod event bus"))

(defn init-mod! [registration-handler]
  "Initialize the mod with the given registration handler"
  (let [registration (create-registration registration-handler)]
    (registry/register-all! registration MOD-ID)))