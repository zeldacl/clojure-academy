(ns cn.li.forge-api.blocks
  (:use [cn.li.forge-api.core :only [defclass gen-setter]])
  (:import (net.minecraft.block.material Material)
           (net.minecraft.block BlockContainer)
           (net.minecraft.world World)))

(defmacro defBlockcontainer [block-name & options]
  (let [prefix (str block-name "-")
        block-data (apply hash-map options)
        material (or (:material block-data) `Material/rock)
        block-data (assoc block-data :material material)
        block-data (assoc block-data :extends BlockContainer)
        tileentity (:tile-entity block-data)
        setters (map #(if (vector? %2)
                       (apply list %1 %2)
                       (list %1 %2))
                     (map gen-setter (keys (:attrs block-data)))
                     (vals (:attrs block-data)))
        block-data (assoc block-data :post-init-fn (fn [this & options]
                                                  ;`(doto ~this ~@setters)
                                                  ;(print setters)
                                                  (print material setters)
                                                  ))
        ]
    `(do
       (defclass ~block-name ~block-data)
       (defn ~(symbol (str prefix "createNewTileEntity")) [~'this ^World ~'world ^int ~'var2]
         (construct-proxy ~tileentity)))))
