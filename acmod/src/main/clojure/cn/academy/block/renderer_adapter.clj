(ns cn.academy.block.renderer-adapter)

(defprotocol IRendererAdapter
  "Protocol for platform-specific rendering adaptation"
  (create-tile-renderer [this handler]
    "Create a platform-specific tile entity renderer using the core handler")
  (begin-render [this matrix-state]
    "Begin a render operation with given matrix state")
  (end-render [this]
    "End a render operation")
  (apply-transform [this transform-data]
    "Apply transformation data to current render state")
  (bind-texture [this texture-info]
    "Bind a texture for rendering")
  (render-model-part [this model part-name transform effects]
    "Render a specific part of a model with given transform and effects"))

(defprotocol IModelAdapter
  "Protocol for platform-specific model loading"
  (load-model [this model-path]
    "Load a model from the given path")
  (get-model-part [this model part-name]
    "Get a specific part from a loaded model"))

(defprotocol ITextureAdapter 
  "Protocol for platform-specific texture handling"
  (load-texture [this texture-path]
    "Load a texture from the given path")
  (set-texture-properties [this texture properties]
    "Set properties for a texture (filtering, wrapping, etc)"))