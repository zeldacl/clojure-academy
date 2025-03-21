(ns cn.academy.energy.impl.wireless-world-data
  (:require [cn.academy.energy.impl.wireless-net :as wireless-net]
            [cn.academy.energy.api.wireless :as wireless]
            [mcmod.world :as world]
            [mcmod.nbt :as nbt]))

(defrecord WirelessWorldData [world networks node-lookup matrix-lookup]
  world/IWorldSavedData
  (write-data [this]
    (let [tag (nbt/create-compound)
          nets-tag (nbt/create-list)]
      (doseq [[ssid net] @networks]
        (let [net-tag (nbt/create-compound)]
          (nbt/put-string net-tag "ssid" ssid)
          (nbt/put-tag net-tag "data" (wireless/save-to-nbt net))
          (nbt/add-to-list nets-tag net-tag)))
      (nbt/put-tag tag "networks" nets-tag)
      tag))
  
  (load-data [this tag]
    (reset! networks {})
    (reset! node-lookup {})
    (reset! matrix-lookup {})
    (let [nets-tag (nbt/get-tag tag "networks")]
      (doseq [i (range (nbt/get-list-size nets-tag))]
        (let [net-tag (nbt/get-compound-at nets-tag i)
              ssid (nbt/get-string net-tag "ssid")
              net-data (nbt/get-tag net-tag "data")
              matrix (wireless/find-matrix world net-data)
              net (wireless-net/create-network this matrix ssid "")]
          (wireless/load-from-nbt! net net-data)
          (swap! networks assoc ssid net)
          (swap! matrix-lookup assoc matrix net)
          (doseq [node (wireless/get-nodes net)]
            (swap! node-lookup assoc node net)))))))

(defn create-world-data [world]
  (->WirelessWorldData 
    world
    (atom {}) ; networks by SSID
    (atom {}) ; node -> network lookup
    (atom {}))) ; matrix -> network lookup

(defn get-network-by-ssid [world-data ssid]
  (get @(:networks world-data) ssid))

(defn get-network-by-node [world-data node]
  (get @(:node-lookup world-data) node))

(defn get-network-by-matrix [world-data matrix]
  (get @(:matrix-lookup world-data) matrix))

(defn get-networks-in-range [world-data x y z range max-count]
  (->> @(:networks world-data)
       (vals)
       (filter #(wireless/in-range? % x y z range))
       (take max-count)))

(defn register-network! [world-data network]
  (let [ssid (wireless/get-ssid network)
        matrix (wireless/get-matrix network)]
    (swap! (:networks world-data) assoc ssid network)
    (swap! (:matrix-lookup world-data) assoc matrix network)))