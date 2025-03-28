(ns cn.mcmod.sound
  (:require [cn.mcmod.protocols :refer [IResourceLocation ISoundCategory ISoundEvent]]))

;; Add sound category protocol
(defprotocol ISoundCategory
  "Protocol for sound categories"
  (get-category-name [this] "Get the sound category name"))

;; Add ISoundEvent protocol
(defprotocol ISoundEvent
  "Protocol for sound events"
  (get-location [this] "Get resource location for the sound event")
  (get-category [this] "Get category for the sound event"))

;; Protocol for objects that can emit sounds
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

;; Extend sound category enum
(def sound-categories
  {:master "master"
   :music "music"
   :records "records"
   :weather "weather"
   :blocks "blocks"
   :hostile "hostile"
   :neutral "neutral"
   :players "players"
   :ambient "ambient"
   :voice "voice"})

;; Constructor functions
(defn create-resource-location [mod-id path]
  (->ResourceLocation mod-id path))

(defn create-sound-event 
  ([mod-id sound-id]
   (create-sound-event mod-id sound-id :blocks))
  ([mod-id sound-id category]
   (let [location (create-resource-location mod-id sound-id)]
     (->SoundEvent location category))))

(defn play-sound-at 
  ([world pos sound-event volume pitch]
   (play-sound-at world pos sound-event :blocks volume pitch))
  ([world pos sound-event category volume pitch]
   ;; Delegate to world's sound system via IWorld protocol
   (.playSound world
               (:x pos)
               (:y pos)
               (:z pos)
               sound-event
               category
               (float volume)
               (float pitch)
               false)))

;; Register a sound event in the game registry
(defn register-sound-event [registry mod-id sound-id]
  (let [sound-event (create-sound-event mod-id sound-id)]
    (registry/register-item! registry 
                            (str "sound." mod-id "." sound-id) 
                            sound-event)
    sound-event))
