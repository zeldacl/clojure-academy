(ns mcmod.test.protocols-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]))

;; Test mock implementations
(defrecord MockBlock [properties events]
  IBlock
  (get-properties [this]
    properties)
  
  (on-placed [this world pos placer]
    (swap! events conj [:placed pos]))
    
  (on-broken [this world pos]
    (swap! events conj [:broken pos]))
    
  (on-activated [this world pos player hand]
    (swap! events conj [:activated pos])))

(defrecord MockEnergyStorage [current max]
  IEnergyStorage
  (receive-energy [this amount simulate]
    (let [space (- @max @current)
          actual (min amount space)]
      (when-not simulate
        (swap! current + actual))
      actual))
      
  (extract-energy [this amount simulate]
    (let [actual (min amount @current)]
      (when-not simulate
        (swap! current - actual))
      actual))
      
  (get-energy-stored [this]
    @current)
    
  (get-max-energy-stored [this]
    @max)
    
  (can-receive? [this] true)
  (can-extract? [this] true))

(defrecord MockWirelessNode [range connections energy]
  IWirelessNode
  (get-range [this]
    @range)
    
  (get-max-connections [this]
    4)
    
  (get-max-energy [this]
    1000)
    
  (get-energy [this]
    @energy)
    
  (connect [this other]
    (when (can-connect? this other)
      (swap! connections conj other)
      true))
      
  (disconnect [this other]
    (swap! connections disj other)
    true)
    
  (can-connect? [this other]
    (and (instance? MockWirelessNode other)
         (< (count @connections) (get-max-connections this)))))

;; Test cases
(deftest test-block-protocol
  (let [events (atom [])
        block (->MockBlock {:material :iron} events)]
    
    (testing "Block properties"
      (is (= :iron (get-in (get-properties block) [:material]))))
      
    (testing "Block events"
      (on-placed block nil [1 1 1] nil)
      (is (= [[:placed [1 1 1]]] @events))
      
      (on-broken block nil [1 1 1])
      (is (= [[:placed [1 1 1]]
              [:broken [1 1 1]]] @events))
              
      (on-activated block nil [1 1 1] nil nil)
      (is (= [[:placed [1 1 1]]
              [:broken [1 1 1]]
              [:activated [1 1 1]]] @events)))))

(deftest test-energy-storage-protocol
  (let [current (atom 0)
        max (atom 1000)
        storage (->MockEnergyStorage current max)]
        
    (testing "Energy storage operations"
      (is (= 0 (get-energy-stored storage)))
      (is (= 1000 (get-max-energy-stored storage)))
      
      (is (= 500 (receive-energy storage 500 false)))
      (is (= 500 (get-energy-stored storage)))
      
      (is (= 200 (extract-energy storage 200 false)))
      (is (= 300 (get-energy-stored storage)))
      
      (is (= 700 (receive-energy storage 1000 false)))
      (is (= 1000 (get-energy-stored storage)))
      
      (is (= 0 (receive-energy storage 100 false))))))

(deftest test-wireless-node-protocol
  (let [range (atom 8)
        connections (atom #{})
        energy (atom 500)
        node1 (->MockWirelessNode range connections energy)
        node2 (->MockWirelessNode (atom 8) (atom #{}) (atom 300))
        node3 (->MockWirelessNode (atom 8) (atom #{}) (atom 200))]
        
    (testing "Wireless node properties"
      (is (= 8 (get-range node1)))
      (is (= 4 (get-max-connections node1)))
      (is (= 500 (get-energy node1)))
      
      (is (true? (connect node1 node2)))
      (is (= #{node2} @connections))
      
      (is (true? (connect node1 node3)))
      (is (= #{node2 node3} @connections))
      
      (is (true? (disconnect node1 node2)))
      (is (= #{node3} @connections)))))