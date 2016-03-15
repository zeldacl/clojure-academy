(ns cn.li.academy.energy.blocks
  (:require [cn.li.academy.energy.tileentities :refer [new-node-entity]])
  (:use [cn.li.academy.core.blocks :only [defACblock]]
        [cn.li.academy.tab :only [tab-clojure-academy]]
        [forge-clj.util :only [get-tile-entity-at]] )
  (:import (net.minecraft.client.renderer.texture IIconRegister)
           (net.minecraft.world IBlockAccess)))

(def icons (atom {}))

(defn register-node-block-icons [^IIconRegister reg]
  (let [names (map (partial str "academy:node_" "basic" "_side_" ) (range 5))
        register-icon (fn [iname]
                        (.registerIcon reg iname))
        temp-icons (doall (map register-icon names))
        icon-map (zipmap (range 5) temp-icons)
        icon-top-disabled (register-icon "academy:node_top_0")
        icon-top-enabled (register-icon "academy:node_top_1")]
    (reset! icons icon-map)
    (reset! icons [:icon-top-disabled icon-top-disabled])
    (reset! icons [:icon-top-enabled icon-top-enabled])))

(defn get-node-block-icon
  ([side metadata]
   (get @icons (if (or (= side 0) (= side 1)) :icon-top-enabled 1)))
  ([^IBlockAccess world x y z side]
   (let [tile-entity (get-tile-entity-at world x y z)]
     ;todo
     )))

(defACblock block-node
            :block-name (str "ac_node_" "basic")
            :container? true
            :hardness 2.5
            :harvest-level ("pickaxe", 1)
            :creative-tab tab-clojure-academy
            :override {:register-block-icons register-node-block-icons
                       :get-icon get-node-block-icon
                       :get-render-block-pass (constantly -1)
                       :is-opaque-cube (constantly false)
                       :on-block-placed (constantly false)  ;todo
                       :create-new-tile-entity new-node-entity

                       })


