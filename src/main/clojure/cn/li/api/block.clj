(ns cn.li.api.block
  (:use [clojure.string :only [lower-case]])
  (:import (net.minecraft.block BlockContainer Block)
           (cpw.mods.fml.common.registry GameRegistry)))

(defmacro create-block [])

(defn register-block [& blocks]
  (doseq [^Block block blocks]
    (GameRegistry/registerBlock block (.getUnlocalizedName block))))


(defmacro defblockContainer [name args {:keys
                                        [material
                                         mod-id
                                         block-name
                                         creative-tab
                                         texture-name
                                         hardness
                                         light-level]
                                        :as inits} bindings & body]
  `(defn ~name ~args
     (let ~bindings
       (let [block# (proxy [BlockContainer] [(:material ~inits)]
               ~@body)]
         (when ~block-name
           (.setBlockName block# (str ~block-name)))
         (when ~creative-tab
           (.setCreativeTab block# ~creative-tab))
         (when ~texture-name
           (.setBlockTextureName block# (lower-case (str ~texture-name))))
         (when ~hardness
           (.setHardness block# (float ~hardness)))
         (when ~light-level
           (.setLightLevel block# (float ~light-level)))
         block#))))