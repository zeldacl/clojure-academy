(ns cn.academy.block.network.core
  (:require [mcmod.protocols :refer [INetworkHandler IPacket IBuffer]]
            [clojure.tools.logging :as log]))

;; Network state tracking
(def network-state
  (atom {:handlers {}
         :pending-updates {}}))

;; Message handling
(defmulti handle-message
  (fn [msg-type data world] msg-type))

(defmethod handle-message :block-destroyed
  [_ block _]
  (when-let [pos (mcmod.block/get-pos block)]
    (mcmod.block/remove-block! pos)))

(defmethod handle-message :update-energy
  [_ block amount]
  (when-let [energy-storage (mcmod.block/get-energy-storage block)]
    (.set-energy-stored! energy-storage amount)))

(defmethod handle-message :update-progress
  [_ block progress]
  (when-let [machine (mcmod.block/get-machine-state block)]
    (.set-progress! machine progress)))

(defmethod handle-message :multiblock-formed
  [_ controller members]
  (doseq [member members]
    (when-let [mb-part (mcmod.block/get-multiblock-part member)]
      (.set-controller! mb-part controller))))

(defmethod handle-message :multiblock-broken
  [_ member _]
  (when-let [mb-part (mcmod.block/get-multiblock-part member)]
    (.set-controller! mb-part nil)))

;; Network packet implementation
(defrecord BlockStatePacket [block-id state]
  IPacket
  (encode [_ buffer]
    (.write-string buffer block-id)
    (.write-map buffer state))
  
  (decode [_ buffer]
    {:block-id (.read-string buffer)
     :state (.read-map buffer)})
  
  (handle [this world]
    (when-let [block (mcmod.block/get-block-by-id (:block-id this))]
      (reset! (:state block) (:state this)))))

;; Network handler implementation  
(defrecord NetworkHandler [channel]
  INetworkHandler
  (send-packet! [_ packet target]
    (.send-packet channel packet target))
  
  (register-packet! [_ packet-type packet-class]
    (.register-packet channel packet-type packet-class))
  
  (handle-packet [_ packet world]
    (try 
      (.handle packet world)
      (catch Exception e
        (log/error "Error handling packet:" (.getMessage e))))))

;; Public interface
(defn send-message! [msg-type & args]
  (when-let [handler (get-in @network-state [:handlers msg-type])]
    (try
      (apply handler args)
      (catch Exception e
        (log/error "Error sending message:" msg-type (.getMessage e))))))

(defn register-handler! [msg-type handler]
  (swap! network-state assoc-in [:handlers msg-type] handler))

(defn create-network-handler [channel]
  (->NetworkHandler channel))

;; Initialize network system
(defn init-network! [network-handler]
  (.register-packet! network-handler :block-state BlockStatePacket)
  
  (register-handler! :block-destroyed handle-message)
  (register-handler! :update-energy handle-message)
  (register-handler! :update-progress handle-message)
  (register-handler! :multiblock-formed handle-message)
  (register-handler! :multiblock-broken handle-message))