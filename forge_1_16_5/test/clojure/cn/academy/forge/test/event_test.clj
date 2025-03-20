(ns cn.academy.forge.test.event-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]
            [cn.academy.forge.events :as events]
            [cn.academy.forge.gui :as gui])
  (:import [net.minecraftforge.event.world WorldEvent]
           [net.minecraftforge.event.server ServerStartingEvent]
           [net.minecraft.world World]
           [net.minecraft.inventory.container Container]
           [net.minecraft.client.gui.screen.inventory ContainerScreen]))

(deftest test-event-system
  (let [event-bus (events/create-event-bus)
        received-events (atom [])
        test-handler (fn [event] 
                      (swap! received-events conj event))]
    
    (testing "Event handler registration"
      (register-handler event-bus test-handler)
      
      (let [test-event {:type :test-event :data "test"}]
        (post-event event-bus test-event)
        (is (= [test-event] @received-events))))
        
    (testing "Multiple handlers"
      (let [second-handler (fn [event]
                            (swap! received-events conj [:second event]))]
        (register-handler event-bus second-handler)
        
        (let [test-event {:type :another-event :data "test2"}]
          (post-event event-bus test-event)
          (is (= 3 (count @received-events))))))))

(deftest test-event-bridge
  (let [event-bus (events/create-event-bus)
        bridge (events/create-event-bridge event-bus)
        received (atom [])]
    
    (testing "World event bridging"
      (register-handler event-bus #(swap! received conj %))
      
      (let [world (proxy [World] [])
            load-event (WorldEvent$Load. world)]
        (handle-world-load bridge world)
        (is (= 1 (count @received)))
        (is (= :world-load (:type (first @received))))))
        
    (testing "Server event bridging"
      (let [server (reify net.minecraft.server.MinecraftServer)
            start-event (ServerStartingEvent. server)]
        (handle-server-starting bridge server)
        (is (= 2 (count @received)))
        (is (= :server-starting (:type (second @received))))))))

(deftest test-gui-system
  (let [handler (gui/->ForgeGuiHandler)]
    
    (testing "Container creation"
      (let [inventory (atom {})
            player (reify net.minecraft.entity.player.PlayerEntity)
            container (create-container handler :machine player inventory nil)]
        (is (instance? Container (.delegate container)))
        (is (= 0 (get-slot-count container)))))
        
    (testing "Screen creation"
      (let [container (reify IContainer)
            player (reify net.minecraft.entity.player.PlayerEntity)
            screen (create-screen handler :machine container player)]
        (is (instance? ContainerScreen (.delegate screen)))
        (is (fn? (partial init screen)))
        (is (fn? (partial render-background screen 176 166))))))))