(ns cn.li.mcmod.network
  (:require                                                 ;[cn.li.mcmod.core :refer [defclass]]
            [cn.li.mcmod.utils :refer [with-prefix defclass]]
            [cn.li.mcmod.nbt :refer [read-tag-data! map->nbt]])
  (:import (net.minecraftforge.fml.network NetworkRegistry NetworkEvent$Context)
           (net.minecraft.util ResourceLocation)
           (java.util.function Supplier Predicate BiConsumer Function)
           (net.minecraftforge.fml.network.simple SimpleChannel)
           (net.minecraftforge.fml.common.network ByteBufUtils)
           (net.minecraft.network PacketBuffer)
           (net.minecraft.nbt CompoundNBT)))

(defclass nbt-packet Object {
                      :constructors {[clojure.lang.PersistentArrayMap] []
                                     [] []}
                      :state data
                      :init init})

(with-prefix nbt-packet-
  (defn init
    ([]
     [[] (atom {})])
    ([nbt-map]
     [[] (atom nbt-map)])))

(defn write-packet [^NbtPacket packet ^PacketBuffer buffer]
  (let [converted-data (deref (.-data packet))
        nbt-data (map->nbt converted-data (CompoundNBT.))]
    (.writeCompoundTag buffer nbt-data)))

(defn read-packet [^PacketBuffer buffer]
  (let [nbt-data (.readCompoundTag buffer)
        packet (NbtPacket.)]
    (read-tag-data! (.-data packet) nbt-data)))


(defonce ^:dynamic *default-network* nil)

(defonce ^:dynamic *message-handles* (atom {}))
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

;(defn write-buffer [packet ^PacketBuffer buffer])
;
;(defn read-buffer [^PacketBuffer buffer])

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
   (register-message *default-network* index message-type write-packet read-packet message-consumer)))

(defn listen [channel topic handles]
  (swap! *message-handles* update-in [(keyword channel) (keyword topic)] (fn [a] (conj (or a []) handles))))

(defn send-to-server
  [channel topic payload]
  (let [nbt-map {:c channel :t topic :p payload}
        packet (NbtPacket. nbt-map)]
    (.sendToServer *default-network* packet)))

(defn send-to
  [network-manager channel topic payload direction]
  (let [nbt-map {:c channel :t topic :p payload}
        packet (NbtPacket. nbt-map)]
    (.sendTo *default-network* packet network-manager direction)))


(defn init-networks [network-name]
  (let [network (create-network network-name)]
    (alter-var-root #'*default-network* network)
    (register-message network 0 nbt-packet write-packet read-packet
      (fn [^NbtPacket message ^NetworkEvent$Context ctx]
        (let [{:keys [c t p]} (deref (.-data message))]
          (when-let [handles (get-in *message-handles* [c t])]
            (doseq [handle handles]
              (handle p ctx))))))))


(defmacro defnetwork [network-name]
  )