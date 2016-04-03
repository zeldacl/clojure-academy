(ns cn.li.forge-api.utils
  (:import (net.minecraft.world World)
           (net.minecraft.inventory IInventory)
           (net.minecraft.item ItemStack Item)
           (net.minecraft.entity.item EntityItem)
           (java.util Random)
           (net.minecraft.creativetab CreativeTabs)
           (cpw.mods.fml.common.registry GameRegistry)
           (net.minecraft.block Block)
           (net.minecraft.tileentity TileEntity)))

(defmacro setfield [this key value]
  `(swap! (.state ~this) into {~key ~value}))

(defmacro getfield [this key]
  `(@(.state ~this) ~key))

(defn get-tile-entity
  [^World world x y z]
  (.getTileEntity world (int x) (int y) (int z)))

(defn drop-items
  ([^World world x y z]
   (let [tile-entity (get-tile-entity world x y z)]
     (drop-items world x y z tile-entity)))
  ([^World world x y z tile-entity]
   (if (instance? IInventory tile-entity)
     (let [tile-entity ^IInventory tile-entity
           per-stack (fn [^ItemStack istack]
                       (if (and istack (> (.-stackSize istack) 0))
                         (let [rg (Random.)
                               rand-x (+ (* (rand) 0.8) 0.1)
                               rand-y (+ (* (rand) 0.8) 0.1)
                               rand-z (+ (* (rand) 0.8) 0.1)
                               factor 0.05
                               entity-item (EntityItem. world (+ x rand-x) (+ y rand-y) (+ z rand-z) (.copy istack))]
                           (if (.hasTagCompound istack)
                             (.setTagCompound (.getEntityItem entity-item) (.copy (.getTagCompound istack))))
                           (set! (.-motionX entity-item) (* (.nextGaussian rg) factor))
                           (set! (.-motionY entity-item) (+ (* (.nextGaussian rg) factor) 0.2))
                           (set! (.-motionZ entity-item) (* (.nextGaussian rg) factor))
                           (.spawnEntityInWorld world entity-item)
                           (set! (.-stackSize istack) 0))))]
       (doall (map #(per-stack (.getStackInSlot tile-entity %1)) (range (.getSizeInventory tile-entity))))))))

(defmacro deftab [tab-name & options]
  (let [obj-data (apply hash-map options)
        icon-name (:icon-name obj-data)]
    `(do
       (def ~tab-name (proxy [CreativeTabs] [~(str tab-name)]
                        (getTabIconItem []
                          (GameRegistry/findItem "academy" ~(str icon-name))
                          ;return item logo
                          ))))
    ))


(defmulti register
          (fn [obj & options] (type obj)))

(defmethod register Block
  ([obj]
    (register obj (.getUnlocalizedName obj)))
  ([obj forge-name]
    (GameRegistry/registerBlock obj forge-name)
    obj))

(defmethod register Item
  ([obj]
    (register obj (.getUnlocalizedName obj)))
  ([obj forge-name]
    (GameRegistry/registerItem obj forge-name)
    obj))

(defmethod register Class
  [obj forge-name]
  (when (isa? obj TileEntity)
    (GameRegistry/registerTileEntity obj forge-name)))





