(ns cn.li.academy.energy.hepler
  (:import (net.minecraft.item ItemStack)
           (net.minecraft.nbt NBTTagCompound)))

(def block-node-configs {
                        "basic" {:id 0 :name "basic" :max-energy 15000 :bandwidth 150 :range 9 :capacity 5}
                        "standard" {:id 1 :name "standard" :max-energy 50000 :bandwidth 300 :range 12 :capacity 10}
                        "advanced" {:id 2 :name "advanced" :max-energy 200000 :bandwidth 900 :range 19 :capacity 20}
                        })

(defn node-type->id [type]
  (get-in block-node-configs [type :id]))

(defn get-node-config-by-id [id]
  (first (filter #(== (:id %1) id) (vals block-node-configs))))

(defn get-node-bandwidth-by-id [id]
  (:bandwidth (get-node-config-by-id id)))

(defn get-node-max-energy-by-id [id]
  (:max-energy (get-node-config-by-id id)))


(defn load-stack-tag [stack]
  (let [tag (.stackTagCompound stack)]
    (when (not tag)
      (set! (.stackTagCompound stack) (NBTTagCompound.)))
    (.stackTagCompound stack)))

(defn get-stack-energy [stack]
  (.getDouble (load-stack-tag stack) "energy"))

(defn get-stack-max-energy [stack]
  (.getMaxEnergy (.getItem stack)))

(defn set-stack-energy [^ItemStack stack amt]
  (let [item (.getItem stack)
        energy (min (.getMaxEnergy item) amt)
        approx-damage (Math/round (* (- 1 (/ energy (get-stack-max-energy stack))) (.getMaxDamage stack)))]
    (.setDouble (load-stack-tag stack) "energy" energy)
    (.setItemDamage stack approx-damage)))

(defn pull-enrgy-from-stack [^ItemStack stack amt ignore-bandwidth]
  (let [item (.getItem stack)
        cur (get-stack-energy stack)
        bandwidth (if ignore-bandwidth (.getBandwidth item) cur)
        give (min cur amt bandwidth)]
    (set-stack-energy stack (- cur give))
    give))

(defn charge-enrgy-to-stack [stack ^double amt ignoreBandwidth]
  (let [item (.getItem stack)
        lim (if ignoreBandwidth Double/MAX_VALUE (.getBandwidth item))
        cur (get-stack-energy stack)
        all-energy (+ cur amt)
        max-energy (.getMaxEnergy item)
        spare (max (- all-energy max-energy) 0)
        charg-energy (min (- max-energy cur) amt)
        namt (* (Math/signum amt) (min (Math/abs amt) lim))]
    (set-stack-energy stack (+ cur namt))
    (+ spare (- amt namt))))




