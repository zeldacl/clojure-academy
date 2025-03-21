(ns cn.academy.block.particle
  (:require [mcmod.protocols :refer [IParticleSystem IParticle IParticleRenderer]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Particle system state
(def particle-state
  (atom {:active-particles #{}
         :particle-types {}}))

;; Particle implementation
(defrecord Particle [type pos velocity lifetime state-atom]
  IParticle
  (get-position [_]
    @(:pos state-atom))
  
  (get-velocity [_]
    @(:velocity state-atom))
  
  (update! [this delta]
    (swap! (:pos state-atom) #(mapv + % @(:velocity state-atom)))
    (when-let [behavior (get-in @particle-state [:particle-types type :behavior])]
      (behavior this delta))
    (swap! state-atom update :age + delta))
  
  (is-alive? [_]
    (< (:age @state-atom) lifetime))
  
  (get-age [_]
    (:age @state-atom))
  
  (get-alpha [this]
    (let [age (.get-age this)]
      (if (< age (/ lifetime 2))
        (min 1.0 (/ age (/ lifetime 4)))
        (max 0.0 (- 1.0 (/ (- age (/ lifetime 2))
                           (/ lifetime 2)))))))
  
  (get-scale [this]
    (let [age (.get-age this)
          base-scale (get-in @particle-state [:particle-types type :scale] 1.0)]
      (* base-scale (- 1.0 (/ age lifetime))))))

;; Particle system implementation
(defrecord ParticleSystem [renderer state-atom]  
  IParticleSystem
  (spawn-particle! [_ type pos velocity lifetime]
    (let [particle (->Particle type pos velocity lifetime 
                              (atom {:age 0
                                    :pos pos
                                    :velocity velocity}))]
      (swap! particle-state update :active-particles conj particle)
      particle))
  
  (register-particle-type! [_ type config]
    (swap! particle-state assoc-in [:particle-types type] config))
  
  (update-particles! [_ delta]
    (swap! particle-state update :active-particles
           (fn [particles]
             (into #{}
                   (filter #(do (.update! % delta)
                               (.is-alive? %)))
                   particles))))
  
  (render-particles! [_]
    (doseq [particle (:active-particles @particle-state)]
      (.render-particle renderer 
                       (.get-position particle)
                       (get-in @particle-state 
                             [:particle-types (.get-type particle) :texture])
                       (.get-alpha particle)
                       (.get-scale particle)))))

;; Factory functions
(defn create-particle-system [renderer]
  (->ParticleSystem renderer (atom {})))

;; Particle type registration
(defn register-particle-type! [system type texture & [config]]
  (.register-particle-type! system type 
                          (merge {:texture texture
                                :scale 1.0
                                :behavior (fn [p _] nil)}
                                config)))

;; Particle spawning helpers
(defn spawn-particle! [system type pos & [config]]
  (let [{:keys [velocity lifetime]
         :or {velocity [0 0 0]
              lifetime 20}} (or config {})]
    (.spawn-particle! system type pos velocity lifetime)))

(defn spawn-particles! [system type pos count & [config]]
  (dotimes [_ count]
    (spawn-particle! system type pos config)))

;; Particle behaviors
(def particle-behaviors
  {:float (fn [particle delta]
            (swap! (:velocity (:state-atom particle)) 
                   update 1 + (* 0.01 delta)))
   
   :sink (fn [particle delta]
           (swap! (:velocity (:state-atom particle)) 
                  update 1 - (* 0.01 delta)))
   
   :spiral (fn [particle delta]
            (let [age (.get-age particle)
                  angle (* age 0.1)
                  radius (* 0.1 (- 1.0 (/ age (:lifetime particle))))]
              (swap! (:velocity (:state-atom particle))
                     (fn [v]
                       [(* radius (Math/cos angle))
                        (nth v 1)
                        (* radius (Math/sin angle))]))))})

;; Initialize particle system
(defn init-particles! []
  (reset! particle-state 
          {:active-particles #{}
           :particle-types {}}))