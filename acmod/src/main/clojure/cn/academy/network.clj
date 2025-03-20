(ns cn.academy.network
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

;; Message type definitions
(defrecord NodeSyncMessage [pos energy connections]
  IPacket
  (encode [this buf]
    (doto buf
      (write-long (pos->long pos))
      (write-long energy)
      (write-string (pr-str connections))))
      
  (decode [this buf]
    (let [pos (long->pos (read-long buf))
          energy (read-long buf)
          connections (read-edn (read-string buf))]
      (assoc this 
             :pos pos
             :energy energy
             :connections connections)))
             
  (handle [this ctx]
    (when-let [world (get-world ctx)]
      (when-let [te (get-tile-entity world (:pos this))]
        (receive-energy te (:energy this) false)
        (reset! (:connections te) (:connections this))
        (mark-dirty te)))))

(defrecord NodeConnectionMessage [from to connect?]
  IPacket  
  (encode [this buf]
    (doto buf
      (write-long (pos->long from))
      (write-long (pos->long to))
      (write-boolean connect?)))
      
  (decode [this buf]  
    (let [from (long->pos (read-long buf))
          to (long->pos (read-long buf))
          connect? (read-boolean buf)]
      (assoc this
             :from from
             :to to
             :connect? connect?)))
             
  (handle [this ctx]
    (when-let [world (get-world ctx)]
      (when-let [from-te (get-tile-entity world (:from this))]
        (when-let [to-te (get-tile-entity world (:to this))]
          (if (:connect? this)
            (connect from-te to-te)
            (disconnect from-te to-te)))))))

;; Network channel creation and message registration
(defn create-network! [network-bridge]
  (let [channel (create-channel network-bridge core/MOD-ID)]
    
    ;; Register node sync message
    (register-message channel 
                     "node_sync"
                     ->NodeSyncMessage
                     #(encode %1 %2)
                     #(decode (->NodeSyncMessage) %1)
                     #(handle %1 %2)
                     :client->server)
                     
    ;; Register node connection message 
    (register-message channel
                     "node_connect"
                     ->NodeConnectionMessage
                     #(encode %1 %2)
                     #(decode (->NodeConnectionMessage) %1)
                     #(handle %1 %2)
                     :client->server)
                     
    channel))

;; Network message sending helpers
(defn send-node-sync! [channel node]
  (let [msg (->NodeSyncMessage (get-position node)
                              (get-energy node)
                              @(:connections node))]
    (send-to-server channel msg)))
    
(defn send-node-connect! [channel from-node to-node connect?]
  (let [msg (->NodeConnectionMessage (get-position from-node)
                                    (get-position to-node)
                                    connect?)]
    (send-to-server channel msg)))