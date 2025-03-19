(ns forge-impl.gui-registry
  (:require [forge-impl.gui-wrapper :as wrapper]
            [forge-impl.gui.wireless-matrix-screen :as matrix-screen])
  (:import [net.minecraft.client.gui ScreenManager]
           [net.minecraftforge.fml.network NetworkHooks]))

(def container-types (atom {}))

(defn register-container-type [gui-id container-supplier screen-factory]
  (let [type (ContainerType. container-supplier)]
    (.register ForgeRegistries/CONTAINERS 
              (ResourceLocation. "cljacademy" gui-id) 
              type)
    (swap! container-types assoc gui-id 
           {:type type
            :screen-factory screen-factory})))

(defn register-client-gui [gui-id]
  (when-let [{:keys [type screen-factory]} (get @container-types gui-id)]
    (ScreenManager/registerFactory type screen-factory)))

(defn open-gui [player world pos gui-id]
  (when-let [{:keys [type]} (get @container-types gui-id)]
    (NetworkHooks/openGui player 
                         (reify INamedContainerProvider
                           (createMenu [this player inv]
                             (let [te (.getTileEntity world pos)]
                               (wrapper/create-container-wrapper te player inv)))
                           (getDisplayName [this]
                             (StringTextComponent. "Wireless Matrix")))
                         pos)))