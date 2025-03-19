(ns mcmod.sound
  (:import [net.minecraft.util SoundCategory SoundEvent ResourceLocation]))

(defprotocol ISoundEmitter
  "Protocol for objects that can emit sounds"
  (play-sound [this sound-id pos data] "Play a sound at the given position"))

(defn create-sound-event [mod-id sound-id]
  (SoundEvent. (ResourceLocation. mod-id sound-id)))

(defn play-sound-at [world pos sound-event volume pitch]
  (.playSound world
              (:x pos)
              (:y pos)
              (:z pos)
              sound-event
              SoundCategory/BLOCKS
              (float volume)
              (float pitch)
              false))