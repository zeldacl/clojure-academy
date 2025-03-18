(ns cn.academy.energy.client.wireless-particles
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.lambdalib2.util.math :as math])
  (:import [net.minecraft.client.particle IParticleRenderType Particle]
           [net.minecraft.client Minecraft]
           [net.minecraft.util ResourceLocation]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.world World]
           [java.util Random]))

(def ^:private ENERGY_BEAM_TEX
  (ResourceLocation. "cljacademy" "textures/effects/energy_beam.png"))

(def ^:private random (Random.))

(defprotocol IEnergyParticle
  (get-alpha [this])
  (get-scale [this])
  (get-color [this])
  (update-motion! [this]))

(defrecord EnergyBeamParticle [world x y z target-x target-y target-z]
  Particle
  (tick [this]
    (when (> (.age this) (.maxAge this))
      (.setExpired this))
    (update-motion! this))
  
  (render [this buffer-in partial-ticks]
    (let [camera (.getActiveRenderInfo (Minecraft/getInstance))
          view-x (.getProjectedView camera)
          view-y (.getProjectedView camera)
          view-z (.getProjectedView camera)
          builder ^IVertexBuilder buffer-in]
      (.vertex builder 
        (- x view-x) (- y view-y) (- z view-z)
        (.getRed (get-color this))
        (.getGreen (get-color this))
        (.getBlue (get-color this))
        (get-alpha this))
      (.vertex builder
        (- target-x view-x) (- target-y view-y) (- target-z view-z)
        (.getRed (get-color this))
        (.getGreen (get-color this))
        (.getBlue (get-color this))
        (get-alpha this))))

  IEnergyParticle
  (get-alpha [_]
    (let [life-ratio (/ (.age this) (.maxAge this))]
      (* 0.8 (- 1.0 life-ratio))))
  
  (get-scale [_]
    (+ 0.2 (* 0.1 (.nextFloat random))))
  
  (get-color [_]
    (java.awt.Color. 0.2 0.6 1.0))
  
  (update-motion! [this]
    (let [dx (- target-x x)
          dy (- target-y y)
          dz (- target-z z)
          dist (math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))
          speed 0.1]
      (set! (.motionX this) (* dx speed))
      (set! (.motionY this) (* dy speed))
      (set! (.motionZ this) (* dz speed)))))

(defn spawn-beam! [^World world x y z target-x target-y target-z]
  (let [particle (->EnergyBeamParticle world x y z target-x target-y target-z)]
    (.addParticle world particle x y z 0 0 0)
    particle))

(defn spawn-connection-particles! [^World world source target]
  (let [source-pos (wireless/get-position source)
        target-pos (wireless/get-position target)]
    (spawn-beam! world 
                 (:x source-pos) (:y source-pos) (:z source-pos)
                 (:x target-pos) (:y target-pos) (:z target-pos))))