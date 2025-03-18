(ns cn.academy.block.model-adapter)

(defprotocol IModelPlatformAdapter
  "Protocol for platform-specific model loading and management"
  (create-model-loader [this]
    "Create a platform-specific model loader")
  (load-model-geometry [this model-path]
    "Load raw geometry data from a model file")
  (register-textures [this texture-paths]
    "Register textures with the platform's texture system")
  (create-geometry-buffer [this vertices faces]
    "Create platform-specific geometry buffers")
  (bake-model [this model baking-context]
    "Bake/compile model for rendering"))

(defprotocol IModelBakingContext
  "Protocol for model baking context"
  (get-texture-resolver [this]
    "Get platform's texture resolution system")
  (get-format-resolver [this]
    "Get platform's vertex format resolver")
  (get-transform-resolver [this]
    "Get platform's transformation resolver"))

(defprotocol IGeometryBuilder
  "Protocol for building model geometry"
  (add-vertex [this x y z u v normal-x normal-y normal-z]
    "Add a vertex to the geometry")
  (add-face [this vertices texture-name]
    "Add a face made up of vertices with given texture")
  (build [this]
    "Build and return the final geometry"))