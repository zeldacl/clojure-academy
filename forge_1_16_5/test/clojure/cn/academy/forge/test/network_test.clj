(ns cn.academy.forge.test.network-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]
            [cn.academy.forge.network :as network])
  (:import [net.minecraft.network PacketBuffer NetworkManager]
           [net.minecraftforge.fml.network NetworkDirection SimpleChannel]))

(defrecord TestPacket [data]
  IPacket
  (encode [this buf]
    (.writeString ^PacketBuffer buf data))
    
  (decode [this buf]
    (.readString ^PacketBuffer buf))
    
  (handle [this ctx]
    (println "Handling packet:" data)))

(deftest test-network-bridge
  (let [bridge (network/create-network-bridge "acmod")
        channel (create-channel bridge "test_channel")]
        
    (testing "Channel creation"
      (is (map? channel))
      (is (contains? channel :channel))
      (is (contains? channel :message-id)))
      
    (testing "Message registration"
      (let [packet (->TestPacket "test_data")]
        (register-message bridge
                         channel
                         (->TestPacket nil)
                         encode
                         decode 
                         handle
                         :client->server)))))

(deftest test-packet-buffer
  (let [buf (PacketBuffer. (io.netty.buffer.Unpooled/buffer))]
    
    (testing "Buffer protocol implementation"
      (write-long buf 123456789)
      (is (= 123456789 (read-long buf)))
      
      (write-string buf "test string")
      (is (= "test string" (read-string buf)))
      
      (write-boolean buf true)
      (is (true? (read-boolean buf)))
      
      (.clear buf))))