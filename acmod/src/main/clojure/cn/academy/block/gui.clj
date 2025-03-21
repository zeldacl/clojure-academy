(ns cn.academy.block.gui
  (:require [mcmod.protocols :refer [IGui IGuiComponent IContainerProvider]]
            [cn.academy.block.lang :as lang]
            [clojure.tools.logging :as log]))

;; GUI state tracking
(def gui-state
  (atom {:active-gui nil
         :components {}}))

;; GUI component base implementation
(defrecord GuiComponent [id config state-atom]
  IGuiComponent
  (init-component [_]
    (swap! state-atom merge (:initial-state config)))
  
  (render [_ x y partial-ticks]
    (when-let [renderer (:renderer config)]
      (renderer @state-atom x y partial-ticks)))
  
  (handle-mouse-click [_ x y button]
    (when-let [handler (:click-handler config)]
      (handler @state-atom x y button)))
  
  (handle-key-press [_ key-code scan-code modifiers]
    (when-let [handler (:key-handler config)]
      (handler @state-atom key-code scan-code modifiers)))
  
  (is-mouse-over? [_ x y]
    (let [{:keys [x1 y1 x2 y2]} (:bounds @state-atom)]
      (and (>= x x1) (<= x x2)
           (>= y y1) (<= y y2)))))

;; Machine GUI implementation
(defrecord MachineGui [block container components]
  IGui
  (init [this]
    (doseq [component (vals components)]
      (.init-component component))
    (reset! (:state block) 
            (assoc @(:state block) :gui this)))
  
  (render [_ mouse-x mouse-y partial-ticks]
    (doseq [component (vals components)]
      (.render component mouse-x mouse-y partial-ticks)))
  
  (on-mouse-clicked [_ mouse-x mouse-y button]
    (doseq [component (vals components)]
      (when (.is-mouse-over? component mouse-x mouse-y)
        (.handle-mouse-click component mouse-x mouse-y button))))
  
  (on-key-pressed [_ key-code scan-code modifiers]
    (doseq [component (vals components)]
      (.handle-key-press component key-code scan-code modifiers)))
  
  (on-close [_]
    (reset! (:state block) 
            (dissoc @(:state block) :gui)))
  
  (draw-background [this mouse-x mouse-y]
    (when-let [renderer (:background-renderer this)]
      (renderer mouse-x mouse-y)))
  
  (draw-foreground [this mouse-x mouse-y]
    (when-let [renderer (:foreground-renderer this)]
      (renderer mouse-x mouse-y)))
  
  IContainerProvider
  (get-container [_]
    container))

;; Factory functions
(defn create-component [id config]
  (->GuiComponent id config (atom {})))

(defn create-machine-gui [block container components]
  (->MachineGui block container components))

;; GUI management
(defn get-active-gui []
  (:active-gui @gui-state))

(defn register-component! [gui-id component]
  (swap! gui-state assoc-in [:components gui-id (:id component)] component))

(defn open-gui! [gui]
  (.init gui)
  (swap! gui-state assoc :active-gui gui))

(defn close-gui! []
  (when-let [gui (get-active-gui)]
    (.on-close gui)
    (swap! gui-state assoc :active-gui nil)))

;; Component helpers
(defn create-energy-bar [config]
  (create-component :energy-bar
    (merge
      {:initial-state {:x1 0 :y1 0 :x2 0 :y2 0}
       :renderer (fn [state x y _]
                  (let [energy (get state :energy 0)
                        max-energy (get state :max-energy 1)
                        height (* (/ energy max-energy) 
                                (- (:y2 state) (:y1 state)))]
                    ;; Draw energy bar
                    ))}
      config)))

(defn create-progress-bar [config]
  (create-component :progress-bar
    (merge
      {:initial-state {:x1 0 :y1 0 :x2 0 :y2 0 :progress 0}
       :renderer (fn [state x y _]
                  (let [progress (get state :progress 0)
                        width (* progress 
                              (- (:x2 state) (:x1 state)))]
                    ;; Draw progress bar
                    ))}
      config)))

;; Initialize GUI system
(defn init-gui! []
  (reset! gui-state {:active-gui nil
                     :components {}}))