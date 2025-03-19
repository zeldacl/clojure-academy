(ns cn.academy.particles.energy-particle
  (:import [net.minecraft.client.particle IParticleRenderType SpriteTexturedParticle]
           [net.minecraft.client.world ClientWorld]
           [net.minecraft.util.math.vector Vector3d]))

(defn create-energy-particle [world x y z vx vy vz]
  (proxy [SpriteTexturedParticle] [world x y z vx vy vz]
    (getRenderType []
      (IParticleRenderType/PARTICLE_SHEET_TRANSLUCENT))
    
    (tick []
      (if (> (.age this) 20)
        (.remove this)
        (do
          (.setPos this 
                   (+ (.x this) (.xd this))
                   (+ (.y this) (.yd this))
                   (+ (.z this) (.zd this)))
          (set! (.age this) (inc (.age this))))))))

(defn spawn-particles [world pos count]
  (dotimes [_ count]
    (let [rand-velocity (fn [] (- (rand 0.1) 0.05))]
      (create-energy-particle world
                             (+ (:x pos) 0.5)
                             (+ (:y pos) 0.5)
                             (+ (:z pos) 0.5)
                             (rand-velocity)
                             (rand-velocity)
                             (rand-velocity)))))