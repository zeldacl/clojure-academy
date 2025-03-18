(ns cn.academy.block.registration-adapter)

(defprotocol IRegistryHandler
  "Protocol for platform-specific block registration"
  (register-block! [this block-data]
    "Register a block with the platform registry system")
  (register-tile-entity! [this tile-data]
    "Register a tile entity type with the platform registry system")
  (register-client-components! [this client-data]
    "Register client-side components (models, renderers) with the platform"))

(defprotocol IClientHandler
  "Protocol for platform-specific client registration"
  (register-model! [this model-data]
    "Register a model with the platform model system")
  (register-renderer! [this renderer-data]
    "Register a renderer with the platform rendering system"))