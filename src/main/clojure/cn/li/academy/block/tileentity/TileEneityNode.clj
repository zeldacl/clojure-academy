(ns cn.li.academy.block.tileentity.TileEneityNode
  (:gen-class :extends cn.li.academy.block.tileentity.TileEntityInventory
              :implements [net.minecraft.inventory.IInventory]
              :init init
              :state state
              :constructors {[] [String int]}))

(defn -init
  []
  [["wireless_node" 2] (atom {})])

(defn -updateEntity [this]
  (when-not (.isRemote (.getWorldObj this))))
