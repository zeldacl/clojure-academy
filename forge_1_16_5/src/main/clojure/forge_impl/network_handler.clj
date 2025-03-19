(ns forge-impl.network-handler
  (:require [mcmod.network :as net])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraftforge.fml.network NetworkEvent$Context NetworkRegistry NetworkDirection]
           [net.minecraft.util ResourceLocation]))

(defn create-packet-handler [packet-type]
  (proxy [java.util.function.BiConsumer] []
    (accept [packet ctx]
      (let [handler (net/get-packet-handler packet-type)]
        (handler packet ctx)))))

(defn register-packet [channel packet-id packet-type]
  (.. channel
      (messageBuilder packet-type (ResourceLocation. "cljacademy" packet-id))
      (encoder (fn [packet buf] (net/encode packet buf)))
      (decoder (fn [buf] (net/decode packet-type buf)))
      (consumer (create-packet-handler packet-type))
      (add)))

(defn create-network-channel []
  (let [channel (NetworkRegistry/newSimpleChannel
                 (ResourceLocation. "cljacademy" "main")
                 #(Integer/valueOf 1)
                 #(= % (Integer/valueOf 1))
                 #(= % (Integer/valueOf 1)))]
    channel))