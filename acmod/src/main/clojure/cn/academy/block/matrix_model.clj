(ns cn.academy.block.matrix-model)

(defprotocol IMatrixModel
  "Core matrix model protocol"
  (get-geometry [this] "Get model geometry data")
  (get-textures [this] "Get model texture mappings")
  (get-animations [this] "Get model animation definitions"))

(defprotocol IMatrixModelLoader
  "Protocol for loading matrix models"
  (load-model [this resource-path] "Load model from the given resource path")
  (reload-resources [this resource-manager] "Handle resource manager reloads"))

(defprotocol IMatrixGeometry 
  "Protocol for matrix model geometry"
  (get-vertices [this] "Get model vertex data")
  (get-faces [this] "Get model face definitions")
  (get-parts [this] "Get named model parts"))

(defrecord MatrixGeometry [vertices faces parts]
  IMatrixGeometry
  (get-vertices [_] vertices)
  (get-faces [_] faces)
  (get-parts [_] parts))

(defrecord MatrixModel [geometry textures animations]
  IMatrixModel
  (get-geometry [_] geometry)
  (get-textures [_] textures)
  (get-animations [_] animations))

(defrecord MatrixModelDescription []
  Object
  (get-model-data [_]
    {:parts ["Base" "Core" "Plate" "Shield"]
     :textures {:main "textures/blocks/wireless_matrix"
                :particle "textures/blocks/wireless_matrix"}
     :animations {:core {:rotation {:axis [0 1 0]
                                  :speed 1.0}}
                 :shield {:translation {:axis [0 1 0]
                                      :amplitude 0.1
                                      :frequency 1.111}}}}))

(defn create-model-loader [resource-manager]
  (reify IMatrixModelLoader
    (load-model [_ path]
      (let [model-data (-> (MatrixModelDescription.)
                          .get-model-data)
            geometry (map->MatrixGeometry 
                     {:vertices [] ; Platform implementations will populate these
                      :faces []   ; with actual geometry data from model files
                      :parts (:parts model-data)})]
        (map->MatrixModel
         {:geometry geometry
          :textures (:textures model-data)
          :animations (:animations model-data)})))
    
    (reload-resources [_ resource-manager]
      ; Platform implementations will handle resource reloading
      )))