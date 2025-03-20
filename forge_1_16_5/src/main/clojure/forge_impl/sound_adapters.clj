(ns forge-impl.sound-adapters
  (:require [mcmod.protocols :refer [IResourceLocation ISoundCategory ISoundEvent]])
  (:import [net.minecraft.util ResourceLocation SoundCategory SoundEvent]))

(defrecord ForgeResourceLocation [^ResourceLocation delegate]
  IResourceLocation
  (get-namespace [_] (.getNamespace delegate))
  (get-path [_] (.getPath delegate)))

(defrecord ForgeSoundEvent [^SoundEvent delegate]
  ISoundEvent
  (get-location [_] 
    (->ForgeResourceLocation (.getLocation delegate)))
  (get-category [_] 
    (.getRegistryName delegate)))

(defn create-forge-resource-location [namespace path]
  (->ForgeResourceLocation (ResourceLocation. namespace path)))

(defn create-forge-sound-event [^ResourceLocation location]
  (->ForgeSoundEvent (SoundEvent. location)))

(defn to-forge-sound-category [category]
  (case category
    :master SoundCategory/MASTER
    :music SoundCategory/MUSIC
    :records SoundCategory/RECORDS
    :weather SoundCategory/WEATHER
    :blocks SoundCategory/BLOCKS
    :hostile SoundCategory/HOSTILE
    :neutral SoundCategory/NEUTRAL
    :players SoundCategory/PLAYERS
    :ambient SoundCategory/AMBIENT
    :voice SoundCategory/VOICE
    SoundCategory/BLOCKS)) ; Default to blocks if unknown