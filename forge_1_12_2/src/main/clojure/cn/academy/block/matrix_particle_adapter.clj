(ns cn.academy.block.matrix-particle-adapter
  (:require [cn.academy.block.matrix-particles :as particles])
  (:import [net.minecraft.util ResourceLocation]
           [net.minecraft.client.renderer GlStateManager]
           [net.minecraft.client.particle Particle]
           [org.lwjgl.opengl GL11]))

(def ^:private particle-texture
  (ResourceLocation. "academy:textures/effects/particles.png"))

(defprotocol IForgeParticleAdapter
  (spawn-particle [this world particle-info])
  (register-particles [this]))

(defrecord ForgeParticleAdapter [particle-system]
  IForgeParticleAdapter
  (spawn-particle [_ world {:keys [type pos velocity color lifetime]}]
    (let [particle (->MatrixParticle 
                    world 
                    (:x pos) (:y pos) (:z pos)
                    (nth velocity 0) (nth velocity 1) (nth velocity 2)
                    {:color color
                     :lifetime lifetime
                     :type type})]
      (.spawnParticle world particle)))
  
  (register-particles [_]
    ;; No registration needed in 1.12.2
    ))

(defrecord MatrixParticle [world x y z vx vy vz options]
  Particle
  (renderParticle [this buffer-in partial-ticks x y z dx dy dz]
    (let [[r g b] (:color options)
          texture-index (case (:type options)
                         :core-glow 0
                         :plate-energy 1
                         :shield-barrier 2)]
      (GlStateManager/pushMatrix)
      (.bindTexture Minecraft/getMinecraft particle-texture)
      
      (GlStateManager/enableBlend)
      (GlStateManager/blendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
      
      (let [buffer (.getWorldRenderer buffer-in)
            prev-pos (.getInterpolatedPosition this partial-ticks)]
        (.begin buffer 7 DefaultVertexFormats/PARTICLE_POSITION_TEX_COLOR_LMAP)
        (doto buffer
          (.pos (- (.x prev-pos) dx) (- (.y prev-pos) dy) (- (.z prev-pos) dz))
          (.tex (/ texture-index 8.0) 0)
          (.color r g b 0.6)
          (.lightmap 240 240)
          .endVertex))
      
      (buffer-in .draw)
      (GlStateManager/disableBlend)
      (GlStateManager/popMatrix)))
  
  (onUpdate [this]
    (when (> (.particleAge this) (:lifetime options))
      (.setExpired this))
    (.setRBGColorF this 
                   (nth (:color options) 0)
                   (nth (:color options) 1) 
                   (nth (:color options) 2))))

(defn create-adapter [particle-system]
  (->ForgeParticleAdapter particle-system))