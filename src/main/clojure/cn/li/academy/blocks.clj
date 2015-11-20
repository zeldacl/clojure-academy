(ns cn.li.academy.blocks
  (:use [cn.li.api.block :only [defblockContainer]])
  (:import (net.minecraft.block BlockContainer)
           (net.minecraft.block.material Material)
           (cpw.mods.fml.relauncher SideOnly Side)
           (net.minecraft.client.renderer.texture IIconRegister)
           (net.minecraft.util IIcon)))

(defn init-block [] ())

;(defblockContainer
;    BlockNode
;    [node-type]
;    {:material Material/rock}
;    [icons (atom {})]
;
;  (^{SideOnly Side/CLIENT} registerBlockIcons [^IIconRegister ir]
;                                              (let [iconTop-disabled (.registerIcon ir "academy:node_top_0")
;                                                    iconTop-enabled (.registerIcon ir "academy:node_top_1")
;                                                    side-icon (for [x (range 5)]
;                                                                (.registerIcon ir (str "academy:node_" node-type "_side_" x)))]
;                                                (swap! icons assoc {:iconTop-disabled iconTop-disabled
;                                                                    :iconTop-enabled  iconTop-enabled
;                                                                    :side-icon        side-icon})))
;  (^{SideOnly Side/CLIENT} getIcon [side meta]
;                                   (if (or (= side 0) (= side 1))
;                                     ;(:iconTop-enabled @icons)
;                                     (nth (:side-icon @icons) 1)))
;  (^{SideOnly Side/CLIENT} getRenderBlockPass []
;                                              -1)
;  (^{SideOnly Side/CLIENT} isOpaqueCube []
;                                        false)
;    ;(^{SideOnly Side/CLIENT} getIcon [world x y z side])
;  )
;
;(def node-a (BlockNode "a"))

;
;
;
;