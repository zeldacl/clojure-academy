(ns cn.academy.tech-system.energy-system.network.handler
  (:require [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [mcmod.block :as block]
            [mcmod.protocols :refer [INetworkHandler IPacket IBuffer]]
            [clojure.tools.logging :as log]))

(def ^:private handler-state
  (atom {:handlers {}
         :pending {}
         :channel nil}))

;; Message handling
(defmulti handle-message
  (fn [msg-type data world] msg-type))

(defmethod handle-message :node-destroyed
  [_ node world]
  (when-let [network (wireless/get-node-network node)]
    (wireless/remove-node! network node)))

(defmethod handle-message :update-energy
  [_ node amount _]
  (when (wireless/is-wireless-node? node)
    (wireless/set-energy! node amount)
    (optimization/optimize-update! node :energy amount)))

(defmethod handle-message :network-formed
  [_ matrix nodes _]
  (doseq [node nodes]
    (when (wireless/is-wireless-node? node)
      (wireless/connect! node matrix))))

(defmethod handle-message :network-broken
  [_ node _]
  (when (wireless/is-wireless-node? node)
    (wireless/disconnect! node)))

;; Block-specific message handlers
(defmethod handle-message :block-destroyed
  [_ block _]
  (when-let [pos (block/get-pos block)]
    (block/remove-block! pos)))

(defmethod handle-message :update-energy
  [_ block amount]
  (when-let [energy-storage (block/get-energy-storage block)]
    (.set-energy-stored! energy-storage amount)))

(defmethod handle-message :multiblock-formed
  [_ controller members]
  (doseq [member members]
    (when-let [mb-part (block/get-multiblock-part member)]
      (.set-controller! mb-part controller))))

(defmethod handle-message :multiblock-broken
  [_ member _]
  (when-let [mb-part (block/get-multiblock-part member)]
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
    (when-let [block (block/get-block-by-id (:block-id this))]
      (reset! (:state block) (:state this)))))

;; Enhanced network handler implementation
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

;; Public API
(defn send-message! [msg-type & args]
  (when-let [handler (get-in @handler-state [:handlers msg-type])]
    (try
      (apply handler args)
      (catch Exception e
        (log/error "Error sending message:" msg-type (.getMessage e))))))

(defn register-handler! [msg-type handler]
  (swap! handler-state assoc-in [:handlers msg-type] handler))

(defn set-network-channel! [channel]
  (reset! (:channel handler-state) channel))

;; Initialize network handlers
(defn init! []
  (let [handler (->NetworkHandler (:channel @handler-state))]
    (.register-packet! handler :block-state BlockStatePacket)
    
    (doseq [[msg-type handler-fn] {:node-destroyed handle-message
                                  :update-energy handle-message
                                  :network-formed handle-message
                                  :network-broken handle-message
                                  :block-destroyed handle-message
                                  :multiblock-formed handle-message
                                  :multiblock-broken handle-message}]
      (register-handler! msg-type handler-fn))
    
    (log/info "Network handler initialized")))