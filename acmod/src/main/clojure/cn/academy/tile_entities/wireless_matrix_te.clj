(ns cn.academy.tile-entities.wireless-matrix-te
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap]
            [mcmod.sound :as sound]
            [mcmod.perf :as perf]
            [mcmod.logging :as log]
            [mcmod.stats :as stats]
            [mcmod.cache :as cache]
            [mcmod.concurrent :as concurrent]
            [mcmod.circuit-breaker :as cb]
            [mcmod.nbt :as nbt]
            [mcmod.world :as world]
            [cn.academy.config.mod-config :as mod-config]
            [cn.academy.energy.transfer-handler :as energy]
            [cn.academy.network.energy-sync-packet :refer [->EnergySyncPacket]]
            [cn.academy.particles.energy-particle :as particles]
            [mcmod.lifecycle :as lifecycle])
  (:import [java.util.concurrent.locks ReentrantLock]))

(def ^:private matrix-hum (sound/create-sound-event "cljacademy" "block.wireless_matrix.hum"))
(def ^:private matrix-transfer (sound/create-sound-event "cljacademy" "block.wireless_matrix.transfer"))

(defrecord WirelessMatrixTE []
  ITileEntity
  (init [this]
    (let [initialized (assoc this
                            :energy-lock (ReentrantLock.)
                            :target-cache (cache/create-cache :expire-after-minutes 1)
                            :energy-storage (concurrent/->AtomicValue 
                                           (cap/create-energy-storage 
                                             :capacity (:energy-capacity (mod-config/get-wireless-matrix-config))
                                             :max-receive (:max-transfer-rate (mod-config/get-wireless-matrix-config))
                                             :max-extract (:max-transfer-rate (mod-config/get-wireless-matrix-config))))
                            :circuit-breaker (cb/create-circuit-breaker 
                                             :failure-threshold 3
                                             :reset-timeout-ms 10000))]
      (lifecycle/register-lifecycle initialized)
      initialized))

  (tick [this]
    (log/with-error-logging
      (perf/with-timing "wireless_matrix.tick"
        (let [config (mod-config/get-wireless-matrix-config)
              energy-storage (get-value (:energy-storage this))
              world (:world this)
              pos (:pos this)]
          (when (and (zero? (mod (System/currentTimeMillis) 20))
                    (not (nil? energy-storage)))
            (let [current-energy (cap/get-energy-stored energy-storage)
                  max-energy (cap/get-max-energy-stored energy-storage)
                  energy-percent (/ current-energy max-energy)]
              
              ;; Track energy level statistics
              (stats/track-stat "wireless_matrix.energy_level" current-energy)
              
              ;; Transfer energy to nearby blocks if circuit breaker allows
              (when (and (not (world/is-client-side? world))
                        (> current-energy 0)
                        (cb/allow-execution? (:circuit-breaker this)))
                (perf/with-timing "wireless_matrix.energy_transfer"
                  (concurrent/with-lock (:energy-lock this)
                    (let [cached-targets (or (cache/get-value (:target-cache this) pos)
                                           (let [targets (energy/find-nearby-energy-tiles world pos 5)]
                                             (cache/put-value (:target-cache this) pos targets)
                                             targets))
                          transfer-count (count cached-targets)]
                      
                      ;; Track number of potential transfer targets
                      (stats/track-stat "wireless_matrix.transfer_targets" transfer-count)
                      
                      ;; Process transfers concurrently with circuit breaker protection
                      (doseq [target-te cached-targets]
                        (concurrent/run-async
                          #(try
                             (when-let [transferred (energy/transfer-energy 
                                                   this target-te 
                                                   (:max-transfer-rate config))]
                               (cb/record-success (:circuit-breaker this))
                               
                               ;; Track successful energy transfers
                               (stats/track-stat "wireless_matrix.energy_transferred" transferred)
                               (stats/track-stat "wireless_matrix.transfer_count" 1)
                               
                               (log/info "Transferred %d energy to target at [%d,%d,%d]"
                                        transferred
                                        (-> target-te :pos :x)
                                        (-> target-te :pos :y)
                                        (-> target-te :pos :z))
                               (sound/play-sound this :transfer pos nil)
                               (particles/spawn-particles world pos 2))
                             (catch Exception e
                               (cb/record-failure (:circuit-breaker this))
                               (log/error "Energy transfer failed: %s" (.getMessage e))))))))))

              ;; Play ambient hum when containing energy
              (when (and (not (world/is-client-side? world))
                        (> current-energy 0))
                (sound/play-sound-at world pos matrix-hum 
                                   (* (:sound-volume config) energy-percent) 
                                   (+ (:base-pitch config) (* 0.4 energy-percent))))


              ;; Spawn ambient particles on client side
              (when (and (world/is-client-side? world)
                        (> current-energy 0))
                (particles/spawn-particles world pos 
                                         (int (* (:particle-count config) 
                                               energy-percent))))


              ;; Send sync packet to clients
              (when-not (world/is-client-side? world)
                (let [packet (->EnergySyncPacket 
                              {:x (:x pos)
                               :y (:y pos)
                               :z (:z pos)}
                              current-energy)]
                  ;; Send packet to tracking clients
                  ))))))))

  (save [this]
    (log/with-error-logging
      (let [tag (nbt/create-compound)]
        (when-let [energy-storage (get-value (:energy-storage this))]
          (nbt/put-int tag "energy" (cap/get-energy-stored energy-storage))
          (log/debug "Saved energy storage state: %d"
                    (cap/get-energy-stored energy-storage)))
        tag)))
  
  (load [this data]
    (log/with-error-logging
      (let [{:keys [nbt]} data
            this (init this)]
        (when (nbt/contains? nbt "energy")
          (let [stored-energy (nbt/get-int nbt "energy")]
            (cap/receive-energy (get-value (:energy-storage this)) stored-energy false)
            (log/debug "Loaded energy storage state: %d" stored-energy)))
        this)))
  
  cap/ICapabilityProvider
  (has-capability? [this capability-type side]
    (= (get-name capability-type) "forge:energy"))
  
  (get-capability [this capability-type side]
    (when (has-capability? this capability-type side)
      (get-value (:energy-storage this))))
  
  (invalidate-capabilities [this]
    (cache/invalidate (:target-cache this) (:pos this)))
  
  sound/ISoundEmitter
  (play-sound [this sound-id pos data]
    (let [world (:world this)
          config (mod-config/get-wireless-matrix-config)]
      (when-not (world/is-client-side? world)
        (case sound-id
          :transfer (sound/play-sound-at world pos matrix-transfer 
                                       (:sound-volume config) 1.0)
          :hum (sound/play-sound-at world pos matrix-hum 
                                   (:sound-volume config) 
                                   (:base-pitch config))))))

  lifecycle/ILifecycle
  (start [this]
    (log/debug "Starting WirelessMatrixTE at pos %s" (:pos this))
    (cb/reset (:circuit-breaker this))
    this)
  
  (stop [this]
    (log/debug "Stopping WirelessMatrixTE at pos %s" (:pos this))
    (when-let [cache (:target-cache this)]
      (cache/invalidate cache (:pos this)))
    this))