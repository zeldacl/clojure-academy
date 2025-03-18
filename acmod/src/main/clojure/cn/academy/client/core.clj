(ns cn.academy.client.core)

(defprotocol IClientRegistration
  "Core client registration functionality"
  (register-screens [this] "Register GUI screens")
  (register-renderers [this] "Register block/item renderers")
  (register-models [this] "Register custom models"))

(defprotocol IRenderInfo
  "Block/item render information"
  (get-render-type [this] "Get render type (solid, translucent etc)")
  (get-model-loader [this] "Get custom model loader if any"))

(defprotocol IModelLoader
  "Custom model loader"
  (get-model-id [this] "Get model identifier")
  (load-model [this resource-manager] "Load model with resources")
  (reload-resources [this resource-manager] "Handle resource reload"))

(defprotocol IRenderer
  "Custom renderer"
  (render [this state data] "Render with current state and data")
  (should-render-offscreen? [this] "Check if should render when off screen"))