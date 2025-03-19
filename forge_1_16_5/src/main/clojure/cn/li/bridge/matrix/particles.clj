(ns cn.li.bridge.matrix.particles
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.structure :as structure]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.particles ParticleTypes]
           [net.minecraft.util.math BlockPos Vector3d]
           [net.minecraft.world World]
           [net.minecraft.world.server ServerWorld]))

(def ^:private PARTICLE_COUNT 10)
(def ^:private PARTICLE_SPEED 0.05)
(def ^:private PARTICLE_SPREAD 0.25)

(defn spawn-formation-particles!
  "Spawn particles for Matrix formation"
  [^World world ^BlockPos pos]
  (when (instance? ServerWorld world)
    (let [server-world ^ServerWorld world
          x (+ (.getX pos) 0.5)
          y (+ (.getY pos) 0.5)
          z (+ (.getZ pos) 0.5)]
      (dotimes [_ PARTICLE_COUNT]
        (.spawnParticle server-world
                       ParticleTypes/ENCHANT
                       x y z
                       1  ; count
                       PARTICLE_SPREAD PARTICLE_SPREAD PARTICLE_SPREAD  ; spread
                       PARTICLE_SPEED)))))

(defn spawn-energy-transfer-particles!
  "Spawn particles for energy transfer between positions"
  [^World world ^BlockPos source ^BlockPos target]
  (when (instance? ServerWorld world)
    (let [server-world ^ServerWorld world
          sx (+ (.getX source) 0.5)
          sy (+ (.getY source) 0.5)
          sz (+ (.getZ source) 0.5)
          tx (+ (.getX target) 0.5)
          ty (+ (.getY target) 0.5)
          tz (+ (.getZ target) 0.5)
          dx (- tx sx)
          dy (- ty sy)
          dz (- tz sz)
          total-dist (Math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))
          step-size (/ total-dist PARTICLE_COUNT)]
      (dotimes [i PARTICLE_COUNT]
        (let [t (/ i PARTICLE_COUNT)
              px (+ sx (* dx t))
              py (+ sy (* dy t))
              pz (+ sz (* dz t))]
          (.spawnParticle server-world
                         ParticleTypes/END_ROD
                         px py pz
                         1  ; count
                         0 0 0  ; spread
                         0))))))  ; speed

(defn spawn-structure-particles!
  "Spawn particles to highlight the Matrix structure"
  [^World world ^BlockPos core-pos]
  (when (instance? ServerWorld world)
    (doseq [pos (structure/get-structure-blocks core-pos)]
      (spawn-formation-particles! world pos))))

(defn spawn-error-particles!
  "Spawn particles indicating an invalid structure"
  [^World world ^BlockPos pos]
  (when (instance? ServerWorld world)
    (let [server-world ^ServerWorld world
          x (+ (.getX pos) 0.5)
          y (+ (.getY pos) 0.5)
          z (+ (.getZ pos) 0.5)]
      (dotimes [_ PARTICLE_COUNT]
        (.spawnParticle server-world
                       ParticleTypes/SMOKE
                       x y z
                       1  ; count
                       PARTICLE_SPREAD PARTICLE_SPREAD PARTICLE_SPREAD  ; spread
                       PARTICLE_SPEED)))))