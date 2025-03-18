(ns cn.academy.block.material-impl
  (:require [cn.academy.block.material :as material])
  (:import [net.minecraft.block.material Material]))

(defmethod material/create-material :1.15.2 [_ material-key]
  (case material-key
    :rock Material/ROCK
    :iron Material/IRON
    :wood Material/WOOD
    :air Material/AIR
    Material/ROCK))  ; default to ROCK if unknown

(defn create-material-mapper []
  (reify material/IMaterialMapper
    (get-material [_ material-name]
      (material/create-material :1.15.2 (material/get-material-key material-name)))))