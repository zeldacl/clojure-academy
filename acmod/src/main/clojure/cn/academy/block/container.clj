(ns cn.academy.block.container
  (:require [mcmod.protocols :refer [IContainer IInventoryHandler ISlot]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Container state tracking  
(def container-state
  (atom {:active-containers {}}))

;; Slot implementation
(defrecord ContainerSlot [id inventory index validator]
  ISlot
  (get-stack [_]
    (.get-stack-in-slot inventory index))
  
  (set-stack [_ stack]
    (.set-stack-in-slot inventory index stack))
  
  (get-max-stack-size [_]
    (.get-slot-limit inventory index))
  
  (is-valid? [_ stack]
    (if validator
      (validator stack)
      true))
  
  (can-take? [_]
    (.can-extract inventory index))
  
  (can-place? [this stack]
    (and (.is-valid? this stack)
         (.can-insert inventory index))))

;; Container implementation
(defrecord MachineContainer [block slots state-atom]
  IContainer
  (get-slots [_]
    slots)
  
  (get-slot [_ index]
    (get slots index))
  
  (transfer-stack [this slot-from slot-to]
    (when-let [stack (.get-stack slot-from)]
      (when (and (.can-take? slot-from)
                 (.can-place? slot-to stack))
        (let [transfer-amount (min (.get-max-stack-size slot-to)
                                 (.get-stack-size stack))]
          (.set-stack slot-to (.split-stack stack transfer-amount))))))
  
  (on-slot-changed [_ slot]
    (when-let [machine (get-in block [:state :machine])]
      (.on-inventory-changed machine (:id slot))))
  
  (can-interact-with? [_ player]
    true)
  
  (on-closed [_ player]
    (when-let [machine (get-in block [:state :machine])]
      (.on-container-closed machine player))))

;; Inventory handler implementation  
(defrecord InventoryHandler [state-atom]
  IInventoryHandler
  (get-slots [_]
    (count (:stacks @state-atom)))
  
  (get-stack-in-slot [_ slot]
    (get-in @state-atom [:stacks slot]))
  
  (set-stack-in-slot [_ slot stack]
    (swap! state-atom assoc-in [:stacks slot] stack))
  
  (get-slot-limit [_ _]
    64)
  
  (can-extract [_ slot]
    (contains? (:stacks @state-atom) slot))
  
  (can-insert [_ slot]
    (not (get-in @state-atom [:locked-slots slot] false))))

;; Factory functions
(defn create-slot [id inventory index & [validator]]
  (->ContainerSlot id inventory index validator))

(defn create-container [block slots]
  (->MachineContainer block slots (atom {})))

(defn create-inventory-handler [size]
  (->InventoryHandler 
    (atom {:stacks {}
           :locked-slots #{}})))

;; Container management
(defn register-container! [container]
  (swap! container-state assoc-in 
         [:active-containers (:id container)] container))

(defn get-container [id]
  (get-in @container-state [:active-containers id]))

;; Slot helpers
(defn create-input-slot [inventory index]
  (create-slot :input inventory index))

(defn create-output-slot [inventory index]
  (create-slot :output inventory index 
               (fn [_] false))) ; Output slots don't accept manual insertion

(defn create-upgrade-slot [inventory index valid-upgrades]
  (create-slot :upgrade inventory index
               #(contains? valid-upgrades (get-type %))))

;; Initialize container system
(defn init-containers! []
  (reset! container-state {:active-containers {}}))