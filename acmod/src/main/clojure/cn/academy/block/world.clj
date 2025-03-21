(ns cn.academy.block.world
  (:require [mcmod.protocols :refer [IWorld IBlockAccess IBlockPos]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; World state tracking
(def world-state
  (atom {:active-blocks {}
         :block-updates {}}))

;; Block position implementation
(defrecord BlockPosition [x y z]
  IBlockPos
  (get-x [_] x)
  (get-y [_] y)
  (get-z [_] z)
  (offset [this direction amount]
    (case direction
      :north (->BlockPosition x y (- z amount))
      :south (->BlockPosition x y (+ z amount))
      :east (->BlockPosition (+ x amount) y z)
      :west (->BlockPosition (- x amount) y z)
      :up (->BlockPosition x (+ y amount) z)
      :down (->BlockPosition x (- y amount) z)))
  (get-manhattan-distance [this other]
    (+ (Math/abs (- x (.get-x other)))
       (Math/abs (- y (.get-y other)))
       (Math/abs (- z (.get-z other))))))

;; Block access implementation
(defrecord WorldBlockAccess [world]
  IBlockAccess
  (get-block-at [_ pos]
    (.get-block world pos))
  
  (set-block-at [_ pos block]
    (.set-block world pos block))
  
  (get-block-state [_ pos]
    (.get-block-state world pos))
  
  (notify-block-update [_ pos]
    (.notify-neighbors world pos))
  
  (is-air [_ pos]
    (nil? (.get-block world pos)))
  
  (get-redstone-power [_ pos side]
    (.get-redstone-power world pos side)))

;; World interaction helpers
(defn create-position [x y z]
  (->BlockPosition x y z))

(defn get-block-access [world]
  (->WorldBlockAccess world))

;; Block update tracking
(defn schedule-block-update! [world pos delay]
  (let [update-time (+ (System/currentTimeMillis) delay)]
    (swap! world-state update :block-updates 
           (fn [updates]
             (update updates update-time 
                     (fnil conj #{}) pos)))))

(defn process-block-updates! [world]
  (let [current-time (System/currentTimeMillis)
        pending-updates (into {} 
                            (filter (fn [[time _]] (<= time current-time))
                                  (:block-updates @world-state)))]
    (doseq [[_ positions] pending-updates
            pos positions]
      (.notify-neighbors world pos))
    (swap! world-state update :block-updates 
           #(apply dissoc % (keys pending-updates)))))

;; Block tracking
(defn register-block! [block pos]
  (swap! world-state assoc-in [:active-blocks pos] block))

(defn unregister-block! [pos]
  (swap! world-state update :active-blocks dissoc pos))

(defn get-active-blocks []
  (vals (:active-blocks @world-state)))

;; Block neighbor handling
(defn get-neighbors [pos]
  [((.offset pos :north 1))
   ((.offset pos :south 1))
   ((.offset pos :east 1))
   ((.offset pos :west 1))
   ((.offset pos :up 1))
   ((.offset pos :down 1))])

(defn notify-neighbors! [world pos]
  (doseq [neighbor-pos (get-neighbors pos)]
    (when-let [block (.get-block world neighbor-pos)]
      (.on-neighbor-changed block pos))))

;; Initialize world system
(defn init-world! []
  (reset! world-state {:active-blocks {}
                       :block-updates {}}))