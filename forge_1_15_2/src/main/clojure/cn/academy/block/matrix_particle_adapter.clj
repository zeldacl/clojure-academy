(ns cn.academy.block.matrix-particle-adapter
  (:require [cn.academy.block.matrix-particles :as particles])
  (:import [net.minecraft.client.particle IParticleRenderType]
           [net.minecraft.particles BasicParticleType]
           [net.minecraft.util ResourceLocation]))

(defprotocol IForgeParticleAdapter
  (spawn-particle [this world particle-info])
  (register-particles [this])
  (create-particle-type [this name])
  (get-render-type [this]))

(defrecord ForgeParticleAdapter [particle-system]
  IForgeParticleAdapter
  (spawn-particle [_ world {:keys [type pos velocity color lifetime]}]
    (let [particle-type (case type
                         :core-glow :matrix_core
                         :plate-energy :matrix_energy
                         :shield-barrier :matrix_shield)
          [x y z] pos
          [vx vy vz] velocity
          [r g b] color]
      (.addParticle world 
                    (get @particle-types particle-type)
                    x y z 
                    vx vy vz 
                    {:color [r g b]
                     :lifetime lifetime})))
  
  (register-particles [_]
    (doseq [id [:matrix_core :matrix_energy :matrix_shield]]
      (create-particle-type (name id))))
  
  (create-particle-type [_ name]
    (BasicParticleType. false))
  
  (get-render-type [_]
    IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT))

(def particle-types (atom {}))

(defrecord MatrixParticle [world x y z vx vy vz options]
  net.minecraft.client.particle.IParticle
  (tick [this]
    (when (> (.age this) (:lifetime options))
      (.setExpired this))
    (let [[r g b] (:color options)]
      (.setColor this r g b)))
  
  (render [this buffer-in partial-ticks]
    (.renderParticle this buffer-in partial-ticks)))

(defn create-adapter [particle-system]
  (->ForgeParticleAdapter particle-system))