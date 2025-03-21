(ns cn.academy.block.gui.core
  (:require [clojure.tools.logging :as log]))

;; GUI registry
(def gui-registry (atom {}))

(defn register-gui! [block-type gui-def]
  (swap! gui-registry assoc block-type gui-def))

;; GUI definitions for different block types
(def machine-guis
  {:metal-former {:title "Metal Former"
                  :slots [{:type :input :x 56 :y 17}
                         {:type :output :x 116 :y 35}
                         {:type :battery :x 56 :y 53}]
                  :progress-bar {:x 76 :y 35
                               :texture "academy:textures/gui/progress_bar.png"}
                  :energy-bar {:x 166 :y 17
                             :texture "academy:textures/gui/energy_bar.png"}}
   
   :imag-fusor {:title "Imaginary Reconstructor"
                :slots [{:type :input :x 56 :y 17}
                       {:type :input :x 56 :y 53}
                       {:type :output :x 116 :y 35}
                       {:type :battery :x 36 :y 35}]
                :progress-bar {:x 76 :y 35
                             :texture "academy:textures/gui/progress_bar.png"}
                :energy-bar {:x 166 :y 17
                           :texture "academy:textures/gui/energy_bar.png"}}
   
   :phase-generator {:title "Phase Generator"
                    :slots [{:type :battery :x 56 :y 35}]
                    :energy-bar {:x 166 :y 17
                               :texture "academy:textures/gui/energy_bar.png"}}})

;; Initialize GUI system
(defn init-guis! []
  (doseq [[block-type gui-def] machine-guis]
    (register-gui! block-type gui-def))
  true)

;; GUI creation and handling
(defn create-gui [block-type player block world]
  (when-let [gui-def (get @gui-registry block-type)]
    (mcmod.gui/create-container-gui
      {:def gui-def
       :block block
       :player player
       :world world
       :handlers {:on-slot-change (fn [slot stack]
                                  (mcmod.gui/mark-dirty!))
                 :on-energy-change (fn [new-energy]
                                   (mcmod.gui/mark-dirty!))}})))

;; Open GUI for a block
(defn open-gui! [block-type player block world]
  (when-let [gui (create-gui block-type player block world)]
    (mcmod.gui/open-gui! player gui)))