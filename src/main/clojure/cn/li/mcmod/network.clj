(ns cn.li.mcmod.network
  (:import (net.minecraftforge.fml.network NetworkRegistry NetworkEvent$Context)
           (net.minecraft.util ResourceLocation)
           (java.util.function Supplier Predicate BiConsumer Function)
           (net.minecraftforge.fml.network.simple SimpleChannel)
           (net.minecraftforge.fml.common.network ByteBufUtils)
           (net.minecraft.network PacketBuffer)))


(defonce ^:dynamic *default-network* nil)
;(.writeCompoundTag PacketBuffer)
(defn create-network
  ([^ResourceLocation network-name networkProtocolVersion clientAcceptedVersions serverAcceptedVersions]
   (NetworkRegistry/newSimpleChannel network-name
     (proxy [Supplier] []
       (get [] networkProtocolVersion))
     (proxy [Predicate] []
       (test [v] (= v clientAcceptedVersions)))
     (proxy [Predicate] []
       (test [v] (= v serverAcceptedVersions)))))
  ([^ResourceLocation network-name]
   (create-network network-name "1.0" "1.0" "1.0"))
  ;(.newSimpleChannel NetworkRegistry/INSTANCE network-name)
  )

(defn write-buffer [packet buffer])

(defn read-buffer [buffer])

(defn register-message
  ([^SimpleChannel network index ^Class message-type encoder decoder message-consumer]
   (.registerMessage network index message-type
     (proxy [BiConsumer] []
       (accept [message packet-buffer]
         (encoder message packet-buffer)))
     (proxy [Function] []
       (apply [buffer]
         (decoder buffer)))
     (proxy [BiConsumer] []
       (accept [message ^Supplier ctx]
         (let [^NetworkEvent$Context ctx (.get ctx)]
           (.enqueueWork ctx (fn [] (message-consumer message ctx)))
           (.setPacketHandled ctx true))))))
  ([index message-type message-consumer]
   (register-message *default-network* index message-type write-buffer read-buffer message-consumer)))

(defn init-networks []
  )


(defmacro defnetwork [network-name]
  )