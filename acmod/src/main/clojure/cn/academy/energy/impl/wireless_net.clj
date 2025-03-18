(ns cn.academy.energy.impl.wireless-net
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.academy.energy.client.wireless-particles :as particles]
            [cn.academy.energy.security.wireless-security :as security]
            [cn.lambdalib2.util.math :as math])
  (:import [net.minecraft.nbt NBTTagCompound NBTTagList]))

(def ^:private UPDATE-INTERVAL 40)
(def ^:private BUFFER-MAX 2000.0)

(defrecord NetworkNode [x y z node-ref]
  Object
  (toString [_] (str "NetworkNode[" x "," y "," z "]")))

(defrecord WirelessNetwork [world-data matrix nodes buffer ssid password security-provider]
  wireless/INetwork
  (get-ssid [_] @ssid)
  
  (set-ssid! [_ new-ssid] 
    (reset! ssid new-ssid))
  
  (get-password [_] @password)
  
  (reset-password! [this old-pass new-pass]
    (when (.verify-password @security-provider old-pass @password)
      (reset! password (.hash-password @security-provider new-pass))
      true))
  
  (get-load [_]
    (count @nodes))
  
  (get-capacity [_]
    (when-let [mat (wireless/get-matrix matrix)]
      (.getCapacity mat)))
  
  (get-matrix [_] 
    (:node-ref matrix))
  
  (add-node! [this node pass]
    (when-let [security @security-provider]
      (when (and (.check-rate-limit security node)
                 (.verify-password security pass @password))
        (let [mat (wireless/get-matrix matrix)
              range (.getRange mat)
              node-pos (wireless/get-position node)
              mat-pos (wireless/get-position matrix)
              dist-sq (math/dist-sq node-pos mat-pos)]
          (when (and (<= dist-sq (* range range))
                    (< (wireless/get-load this) (wireless/get-capacity this)))
            (let [new-node (map->NetworkNode 
                            {:x (:x node-pos)
                             :y (:y node-pos)
                             :z (:z node-pos)
                             :node-ref (atom node)})]
              (swap! nodes conj new-node)
              (.log-access-attempt security node pass true)
              true))))))
  
  (remove-node! [_ node]
    (swap! nodes #(remove (fn [n] (= (:node-ref n) node)) %)))
  
  (tick! [this]
    (when-let [mat (wireless/get-matrix matrix)]
      ;; Balance energy across nodes and spawn visualization particles
      (let [node-list @nodes
            world (.getWorld mat)
            shuffled (shuffle node-list)
            node-stats (for [n shuffled
                           :let [node (wireless/get-node (:node-ref n))]
                           :when node]
                       {:node node
                        :energy (.getEnergy node)
                        :max-energy (.getMaxEnergy node)})
            total-energy (reduce + (map :energy node-stats))
            total-max (reduce + (map :max-energy node-stats))
            target-percent (/ total-energy total-max)
            bandwidth (.getBandwidth mat)]
        
        ;; Energy transfer visualization
        (when (and (not (.isRemote world))
                  (zero? (mod (System/currentTimeMillis) 1000))) ; Every second
          (doseq [{:keys [node]} node-stats]
            (particles/spawn-connection-particles! world node mat)))
        
        ;; Energy balancing
        (loop [nodes node-stats
               transfer-left bandwidth
               new-buffer @buffer]
          (if (and (seq nodes) (pos? transfer-left))
            (let [{:keys [node energy max-energy]} (first nodes)
                  target (* max-energy target-percent)
                  delta (- target energy)
                  capped-delta (math/constrain delta 
                                             (- transfer-left) 
                                             transfer-left)
                  final-delta (math/constrain capped-delta
                                            (- new-buffer)
                                            (- BUFFER-MAX new-buffer))]
              (.setEnergy node (+ energy final-delta))
              (recur (rest nodes)
                     (- transfer-left (math/abs final-delta))
                     (+ new-buffer final-delta)))
            (reset! buffer new-buffer))))))
  
  (save-to-nbt [this]
    (let [tag (NBTTagCompound.)]
      (.setString tag "ssid" @ssid)
      (.setString tag "password" @password)
      (.setDouble tag "buffer" @buffer)
      (let [nodes-tag (NBTTagList.)]
        (doseq [node @nodes]
          (.appendTag nodes-tag (wireless/save-node-to-nbt node)))
        (.setTag tag "nodes" nodes-tag))
      tag))
  
  (load-from-nbt! [this tag]
    (reset! ssid (.getString tag "ssid"))
    (reset! password (.getString tag "password"))
    (reset! buffer (.getDouble tag "buffer"))
    (let [nodes-tag (.getTag tag "nodes")]
      (reset! nodes
        (for [i (range (.tagCount nodes-tag))]
          (wireless/load-node-from-nbt (.getCompoundTagAt nodes-tag i)))))))

(defn create-network [world-data matrix ssid password]
  (let [network (->WirelessNetwork
                  world-data
                  matrix
                  (atom [])
                  (atom 0.0)
                  (atom ssid)
                  (atom password)
                  (atom nil))
        security (security/create-security-provider)]
    (reset! (:security-provider network) security)
    (reset! (:password network) (.hash-password security password))
    network))