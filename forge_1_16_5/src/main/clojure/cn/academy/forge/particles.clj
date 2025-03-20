(ns cn.academy.forge.particles
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.particles ParticleType IParticleData]
           [net.minecraft.client.particle Particle]
           [net.minecraft.world World]
           [net.minecraft.network PacketBuffer]))

(defrecord ForgeParticle [^Particle delegate]
  IParticle
  (tick [this]
    (.tick delegate))
    
  (render [this buffer partial-ticks]
    (.render delegate buffer partial-ticks))
    
  (set-color [this r g b]
    (.setColor delegate r g b))
    
  (set-alpha [this alpha]
    (.setAlpha delegate alpha))
    
  (set-lifetime [this ticks]
    (.setLifetime delegate ticks))
    
  (is-alive? [this]
    (.isAlive delegate)))

(defrecord ForgeParticleType [id sprite-set factory]
  IParticleType
  (get-id [this]
    id)
    
  (get-sprite-set [this]
    sprite-set)
    
  (create-particle [this world x y z vx vy vz data]
    (factory world x y z vx vy vz data)))

(defrecord ForgeParticleData [type data]
  IParticleData
  (write-to-network [this buffer]
    (doto ^PacketBuffer buffer
      (.writeResourceLocation (get-id type))
      (.writeString (pr-str data))))
      
  (write-to-command [this builder]
    (.append builder (str (get-id type) " " (pr-str data))))
    
  (get-particle-type [this]
    type))

(defrecord ForgeParticleManager [world]
  IParticleManager
  (register-factory [this type factory]
    (let [sprite-set (.getSpriteSet factory)
          particle-type (->ForgeParticleType 
                         (get-id type)
                         sprite-set
                         factory)]
      (.register this type particle-type)))
      
  (spawn-particle [this data x y z vx vy vz]
    (let [world ^World (:world this)
          type (get-particle-type data)]
      (.addParticle world type x y z vx vy vz)))
      
  (add-effect [this effect]
    (spawn-particles effect world (.getPosition effect)))
    
  (clear-effects [this]
    (.clearEffects world)))

(defrecord ForgeParticleEffect [particles duration finished?]
  IParticleEffect
  (spawn-particles [this world pos]
    (doseq [particle @particles]
      (let [[x y z] pos
            [vx vy vz] (:velocity particle)]
        (spawn-particle world 
                       (:type particle)
                       (+ x (:offset-x particle))
                       (+ y (:offset-y particle))
                       (+ z (:offset-z particle))
                       vx vy vz))))
                       
  (get-particles [this]
    @particles)
    
  (update-effect [this]
    (when (> @duration 0)
      (swap! duration dec)))
      
  (is-finished? [this]
    (or @finished?
        (<= @duration 0))))

;; Factory functions
(defn create-particle-manager [world]
  (->ForgeParticleManager world))

(defn create-particle-effect [particles duration]
  (->ForgeParticleEffect (atom particles)
                        (atom duration)
                        (atom false)))