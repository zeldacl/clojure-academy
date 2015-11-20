(ns cn.li.academy.block.BlockNode
  (:gen-class :extends net.minecraft.block.BlockContainer
              :init "init"
              :state "state"
              :constructors {[String] [net.minecraft.block.material.Material]}
              :methods [[^{SideOnly Side/CLIENT} registerBlockIcons [net.minecraft.client.renderer.texture.IIconRegister] void]])
  (:import (net.minecraft.client.renderer.texture IIconRegister)
    (cpw.mods.fml.relauncher SideOnly Side)
    (net.minecraft.world IBlockAccess)
    (net.minecraft.block.material Material)))

(defn -init
  [^String note-type]
  [[Material/rock] (atom {:node-type note-type})])

(defn  -registerBlockIcons
  [this ^IIconRegister ir]
  (let [node-type (:node-type @(.state this))
        updateMap {:iconTop-disabled (.registerIcon ir "academy:node_top_0")
                   :iconTop-enabled (.registerIcon ir "academy:node_top_1")
                   :side-icon (for [x (range 5)]
                                (.registerIcon ir (str "academy:node_" node-type "_side_" x)))}]
    (swap! (.state this) assoc updateMap)))

(defn -getIcon [this side meta]
  (let [{:keys [iconTop-enabled side-icon]} @(.state this)]
    (if (or (= side 0) (= side 1))
      iconTop-enabled
      (nth side-icon 1))))

(defn -getRenderBlockPass [this]
  -1)

(defn -isOpaqueCube [this]
  false)

(defn -getIcon
  [this ^IBlockAccess world x y z side])
