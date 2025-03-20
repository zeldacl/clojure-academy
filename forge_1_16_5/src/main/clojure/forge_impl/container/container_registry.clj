(ns forge-impl.container.container-registry
  (:require [mcmod.protocols :refer :all]
            [forge-impl.container.container-adapter :as container-adapter]
            [forge-impl.gui.gui-adapter :as gui-adapter])
  (:import [net.minecraft.inventory.container ContainerType]
           [net.minecraftforge.fml.network NetworkHooks]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.util ResourceLocation]
           [net.minecraft.util.math BlockPos]))

;; Track registered container types
(def ^:private container-types (atom {}))

;; Create container type for mcmod container
(defn create-container-type [container-id container]
  (ContainerType/create
    (fn [window-id inventory pos]
      (container-adapter/create-forge-container container window-id inventory pos))))

;; Register container type
(defn register-container-type! [mod-id container-id container]
  (let [type (create-container-type container-id container)
        location (ResourceLocation. mod-id container-id)]
    (.setRegistryName type location)
    (swap! container-types assoc container-id type)
    type))

;; Open container for player
(defn open-container! [player world pos container-id]
  (NetworkHooks/openGui player
                       (reify INamedContainerProvider
                         (createMenu [_ window-id inventory player]
                           (when-let [type (get @container-types container-id)]
                             (.create type window-id inventory (BlockPos. pos))))
                         (getDisplayName [_]
                           (TranslationTextComponent. (str "container." container-id))))
                       pos))