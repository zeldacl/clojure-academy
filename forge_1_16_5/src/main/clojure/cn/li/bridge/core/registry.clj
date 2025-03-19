(ns cn.li.bridge.core.registry
  (:require [clojure.tools.logging :as log])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]))

(defprotocol IRegistry
  "Core registry functionality"
  (register-block [this id block] "Register a block")
  (register-item [this id item] "Register an item")
  (register-tile-entity [this id type] "Register a tile entity type")
  (register-container [this id type] "Register a container type"))

(defprotocol IRegistryManager
  "Registry management functionality"
  (get-blocks-registry [this] "Get blocks registry")
  (get-items-registry [this] "Get items registry")
  (get-tile-entities-registry [this] "Get tile entities registry")
  (get-containers-registry [this] "Get containers registry"))

(defn initialize-registry []
  (log/info "Initializing registry bridge"))