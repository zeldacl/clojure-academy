(ns cn.academy.block.interaction
  (:require [mcmod.protocols :refer [IInteractionHandler IInputHandler]]
            [cn.academy.block.gui :as gui]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Interaction state tracking
(def interaction-state
  (atom {:active-interactions {}
         :interaction-history []}))

;; Interaction handler implementation
(defrecord BlockInteractionHandler [block]
  IInteractionHandler
  (can-interact? [_ player side]
    (when-let [machine (get-in block [:state :machine])]
      (.is-accessible? machine side)))
  
  (on-interact [this player side hand]
    (error/with-safe-execution (:id block) :interaction
      (when (.can-interact? this player side)
        (when-let [machine (get-in block [:state :machine])]
          (let [result (.handle-interaction machine player side hand)]
            (swap! interaction-state update :interaction-history
                   conj {:block (:id block)
                        :player player
                        :type :machine
                        :result result})
            result)))))
  
  (get-interaction-type [_ side]
    (when-let [machine (get-in block [:state :machine])]
      (or (.get-access-type machine side)
          :none))))

;; Input handler implementation
(defrecord BlockInputHandler [block]
  IInputHandler
  (handle-key-press [_ key scancode modifiers]
    (when-let [gui (gui/get-active-gui)]
      (.handle-key-press gui key scancode modifiers)))
  
  (handle-mouse-click [_ button x y]
    (when-let [gui (gui/get-active-gui)]
      (.handle-mouse-click gui button x y)))
  
  (handle-mouse-scroll [_ delta]
    (when-let [gui (gui/get-active-gui)]
      (.handle-mouse-scroll gui delta)))
  
  (get-hover-text [_ x y]
    (when-let [machine (get-in block [:state :machine])]
      (.get-status-text machine))))

;; Factory functions
(defn create-interaction-handler [block]
  (->BlockInteractionHandler block))

(defn create-input-handler [block]
  (->BlockInputHandler block))

;; Interaction tracking
(defn start-interaction! [block player]
  (let [interaction-id (str (random-uuid))]
    (swap! interaction-state assoc-in 
           [:active-interactions interaction-id]
           {:block block
            :player player
            :start-time (System/currentTimeMillis)})
    interaction-id))

(defn end-interaction! [interaction-id]
  (when-let [{:keys [block player start-time]} 
             (get-in @interaction-state [:active-interactions interaction-id])]
    (let [duration (- (System/currentTimeMillis) start-time)]
      (swap! interaction-state update :interaction-history
             conj {:block (:id block)
                   :player player
                   :type :session
                   :duration duration}))
    (swap! interaction-state update :active-interactions dissoc interaction-id)))

;; Initialize interaction system
(defn init-interaction! []
  (reset! interaction-state 
          {:active-interactions {}
           :interaction-history []}))