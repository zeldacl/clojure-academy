(ns cn.academy.energy.transfer-handler
  (:require [mcmod.capabilities :as cap]
            [cn.academy.config.mod-config :as config])
  (:import [net.minecraft.util Direction]
           [net.minecraft.tileentity TileEntity]))

(defn find-nearby-energy-tiles [world pos range]
  (for [x (range (- range) (inc range))
        y (range (- range) (inc range))
        z (range (- range) (inc range))
        :let [check-pos (-> pos
                           (update :x + x)
                           (update :y + y)
                           (update :z + z))
              tile (.getTileEntity world 
                                 (:x check-pos) 
                                 (:y check-pos) 
                                 (:z check-pos))]
        :when (and tile
                  (not= [(:x pos) (:y pos) (:z pos)]
                        [(:x check-pos) (:y check-pos) (:z check-pos)])
                  (cap/has-capability? tile "forge:energy" nil))]
    tile))

(defn transfer-energy [source-te target-te max-transfer]
  (when-let [source-storage (cap/get-capability source-te "forge:energy" nil)]
    (when-let [target-storage (cap/get-capability target-te "forge:energy" nil)]
      (let [energy-available (cap/extract-energy source-storage max-transfer true)
            energy-accepted (cap/receive-energy target-storage energy-available true)]
        (when (pos? energy-accepted)
          (cap/extract-energy source-storage energy-accepted false)
          (cap/receive-energy target-storage energy-accepted false)
          energy-accepted)))))