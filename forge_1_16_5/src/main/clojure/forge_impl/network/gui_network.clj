(ns forge-impl.network.gui-network
  (:require [forge-impl.gui.gui-registry :as gui-registry]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.network PacketBuffer]
           [net.minecraftforge.fml.network NetworkDirection NetworkEvent$Context NetworkRegistry]
           [net.minecraft.util ResourceLocation]
           [java.util.function Supplier]))

;; Network channel
(def ^:private CHANNEL 
  (NetworkRegistry/newSimpleChannel
    (ResourceLocation. "mcmod" "gui")
    #(Integer/valueOf 1)
    #(= % 1)
    #(= % 1)))

;; Message for opening GUI
(defrecord OpenGuiMessage [container-id pos]
  
  ;; Encode message to buffer
  (encode [_ buf]
    (.writeString buf container-id)
    (.writeBlockPos buf pos))
  
  ;; Decode message from buffer
  (decode [_ buf]
    (->OpenGuiMessage
      (.readString buf)
      (.readBlockPos buf)))
  
  ;; Handle message
  (handle [this ctx]
    (let [sender (.. ctx (get) getSender)
          player (.. ctx (get) getPlayer)]
      ;; Execute on main thread
      (.enqueueWork ctx
        (fn []
          (try
            (gui-registry/open-gui! player pos container-id)
            (catch Exception e
              (log/error e "Error opening GUI")))))
      (.setPacketHandled ctx true))))

;; Register messages
(defn register-messages! []
  (.registerMessage CHANNEL 0
                   OpenGuiMessage
                   #(.encode ^OpenGuiMessage %1 %2)
                   #((.decode (OpenGuiMessage.) %))
                   #(.handle ^OpenGuiMessage %1 %2)
                   (Optional/of NetworkDirection/PLAY_TO_CLIENT)))

;; Send open GUI message to client
(defn send-open-gui! [player container-id pos]
  (let [connection (.connection player)
        message (->OpenGuiMessage container-id pos)]
    (.send CHANNEL 
           NetworkDirection/PLAY_TO_CLIENT
           message
           connection)))