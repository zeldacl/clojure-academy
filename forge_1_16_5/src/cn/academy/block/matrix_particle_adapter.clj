(ns cn.academy.block.matrix-particle-adapter
  (:require [cn.academy.block.matrix-particles :as particles])
  (:import [net.minecraft.client.particle IParticleRenderType]
           [net.minecraft.particles ParticleType]
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
                    particle-type
                    x y z 
                    vx vy vz 
                    (double-array [r g b lifetime]))))
  
  (register-particles [_]
    (doseq [[id render-type] [[:matrix_core IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT]
                             [:matrix_energy IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT]
                             [:matrix_shield IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT]]]
      (create-particle-type (name id))))
  
  (create-particle-type [_ name]
    (ParticleType. 
      (ResourceLocation. "academy" name)
      false
      (reify net.minecraft.client.particle.IParticleFactory
        (createParticle [_ type world x y z vx vy vz data]
          (let [[r g b lifetime] data]
            (-> (doto (net.minecraft.client.particle.SpriteTexturedParticle. world x y z vx vy vz)
                  (.setColor (float r) (float g) (float b))
                  (.setLifetime (int lifetime)))
                (.setSprite (.getSpriteSet Minecraft/getInstance (ResourceLocation. "academy" name)))))))))
  
  (get-render-type [_]
    IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT))

(defn create-adapter [particle-system]
  (->ForgeParticleAdapter particle-system))