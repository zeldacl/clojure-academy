(ns cn.academy.block.matrix-init
  (:require [cn.academy.block.matrix-registry :as registry]
            [cn.academy.block.matrix-config :as config]
            [cn.academy.block.matrix-resources :as resources]))

(defprotocol IMatrixInitializer
  (initialize! [this])
  (load-resources! [this])
  (create-registry! [this]))

(defrecord MatrixInitializer [mod-id]
  IMatrixInitializer
  (initialize! [this]
    (doto this
      load-resources!
      create-registry!))
  
  (load-resources! [_]
    (let [resource-manager (resources/create-resource-manager mod-id)]
      (resources/load-model resource-manager)
      (resources/load-texture resource-manager)
      resource-manager))
  
  (create-registry! [_]
    (let [config (config/create-matrix-config)
          resource-manager (load-resources! this)]
      (registry/create-registry config resource-manager))))

(defn create-initializer [mod-id]
  (->MatrixInitializer mod-id))