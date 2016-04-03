(ns cn.li.academy.energy.tileentitis
  (:use [cn.li.forge-api.tileentities :only [deftileentityinventory]])
  (:use [cn.li.forge-api.utils :only [setfield getfield]])
  (:use [cn.li.academy.config :only [node-config]])
  ;(:use [cn.li.academy.energy.items :only [ImagEnergyItem]])
  (:import (net.minecraft.item ItemStack)
           (net.minecraft.tileentity TileEntity)
    ;(cn.li.academy.common ImagEnergyItem)
           ))

(print "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb$$$$$$$$$$$^^^^^^^^^^^^^^^^^!!!!!!!!!!!!!!!!!!!!!!!")

(defn- get-node-type [^TileEntity etity]
  (let [metadata (.getBlockMetadata etity)]
    (condp = metadata
      0 (:basic node-config)
      1 (:standard node-config)
      2 (:advanced node-config))))

(defn- get-bandwidth [etity]
  (:bandwidth (get-node-type etity)))

(defn get-max-energy [etity]
  (:max-energy (get-node-type etity)))

(defn supported? [^ItemStack stack]
  ;(instance? ImagEnergyItem (.getItem stack))
  )

(defn- get-item-energy [stack]
  (.getDouble (.stackTagCompound stack) "energy"))

(defn- get-item-description [stack]
  (format "%.0f/%.0f IF" (get-item-energy stack)))

(defn- set-item-energy [stack amt]
  (let [item (.getItem stack)
        energy (min (.getMaxEnergy item) amt)
        approx-damage (int (Math/round (* (- 1 (/ amt (.getMaxEnergy item))) (.getMaxDamage stack))))]
    (.setDouble (.stackTagCompound stack) "energy" energy)
    (.setItemDamage stack approx-damage)))

(defn- pull-item [stack amt ignore-bandwidth]
  (let [item (.getItem stack)
        cur (get-item-energy stack)
        give (min amt cur)
        give (if (not ignore-bandwidth) (min give (.getBandwidth item)) give)]
    (set-item-energy stack (- cur give))
    give))

(defn- charge-item
  ([stack amt]
   (charge-item stack amt false))
  ([stack ^double amt ignore-bandwidth]
   (let [item (.getItem stack)
         lim (if ignore-bandwidth Double/MAX_VALUE (.getBandwidth item))
         cur (get-item-energy stack)
         [spare amt] (if (> (+ amt cur) (.getMaxEnergy item))
                       [(- (+ cur amt) (.getMaxEnergy item)) (- (.getMaxEnergy item) cur)]
                       [0.0 amt])
         namt (* (Math/signum amt) (Math/min (Math/abs amt) lim))]
     (set-item-energy stack (+ cur namt))
     spare)))

(deftileentityinventory tile-entity-node "wireless_node" 2
                        :fields {:energy       0
                                 :enabled      false
                                 :charging-in  false
                                 :charging-out false
                                 :name         ""}
                        :sync-data [:energy :enabled :charging-in :charging-out :name])

(defn- update-charge-in [etity]
  (let [stack (.getStackInSlot etity 0)]
    (if (and stack (supported? stack))
      (let [energy (getfield etity :energy)
            pull (pull-item stack (min (get-bandwidth etity) (- (get-max-energy etity) energy)) false)]
        (setfield etity :energy (+ energy pull))
        (setfield etity :charging-in (not (== pull 0))))
      (setfield etity :charging-in false))))

(defn- update-charge-out [etity]
  (let [stack (.getStackInSlot etity 1)]
    (if (and stack (supported? stack))
      (let [cur (getfield etity :energy)
            energy (min cur (get-bandwidth etity))]
        (when-let [left (and (> energy 0) (charge-item stack energy))]
          (setfield etity :energy (- cur energy left))
          (setfield etity :charging-out (not (== left energy)))))
      (setfield etity :charging-out false))))

(defn tile-entity-node-updateEntity [this]
  (when (.isRemote (.getWorldObj this))
    (update-charge-in this)
    (update-charge-out this)))
