(ns mcmod.sound
  (:require [mcmod.protocols :refer [IResourceLocation ISoundCategory ISoundEvent]]))

(defprotocol ISoundEmitter
  "Protocol for objects that can emit sounds"
  (play-sound [this sound-id pos data] "Play a sound at the given position"))

;; Basic implementations of sound protocols
(defrecord ResourceLocation [namespace path]
  IResourceLocation
  (get-namespace [_] namespace)
  (get-path [_] path))

(defrecord SoundEvent [location category]
  ISoundEvent
  (get-location [_] location)
  (get-category [_] category))

;; Constructor functions
(defn create-resource-location [mod-id path]
  (->ResourceLocation mod-id path))

(defn create-sound-event [mod-id sound-id]
  (let [location (create-resource-location mod-id sound-id)]
    (->SoundEvent location :blocks)))

(defn play-sound-at [world pos sound-event volume pitch]
  ;; Delegate to world's sound system via IWorld protocol
  (.playSound world
              (:x pos)
              (:y pos)
              (:z pos)
              sound-event
              :blocks  ; Default to blocks category
              (float volume)
              (float pitch)
              false))