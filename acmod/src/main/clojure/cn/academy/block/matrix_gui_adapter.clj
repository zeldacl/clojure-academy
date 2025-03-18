(ns cn.academy.block.matrix-gui-adapter
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IGuiAdapter
  "Protocol for platform-independent GUI handling"
  (open-gui [this player world pos]
    "Open matrix GUI for player")
  (close-gui [this player]
    "Close player's current GUI"))

(defprotocol IContainerAdapter
  "Protocol for platform-independent container handling"
  (create-container [this player tile]
    "Create container instance")
  (add-slot [this inventory slot-index x y]
    "Add slot to container")
  (transfer-stack [this player slot]
    "Handle stack transfer")
  (can-interact [this player]
    "Check if player can interact"))

(defprotocol IInventoryAdapter
  "Protocol for platform-independent inventory handling"
  (get-slot-count [this]
    "Get total number of slots")
  (get-stack [this slot]
    "Get stack in slot")
  (set-stack [this slot stack]
    "Set stack in slot")
  (remove-stack [this slot amount]
    "Remove items from slot")
  (is-empty [this]
    "Check if inventory is empty"))

(defprotocol ISlotAdapter
  "Protocol for platform-independent slot handling"
  (get-max-stack-size [this]
    "Get max stack size")
  (can-insert [this stack]
    "Check if stack can be inserted")
  (can-extract [this stack]
    "Check if stack can be extracted")
  (on-slot-changed [this]
    "Handle slot content change"))

(defrecord MatrixGuiConfig []
  (slot-positions [_]
    {:core [78 36]  ; Core slot position
     :plates [[78 11]   ; Top plate
              [53 60]   ; Left plate 
              [104 60]] ; Right plate
     :inventory-start [8 84]  ; Player inventory start position
     :hotbar-start [8 142]}) ; Hotbar start position

(defrecord MatrixContainer [tile player inventory-adapter]
  IContainerAdapter
  (create-container [_ player tile]
    (let [config (->MatrixGuiConfig)
          positions (:plates (slot-positions config))]
      (doto (->MatrixContainer tile player (create-inventory-adapter))
        (add-plate-slots positions)
        (add-core-slot (:core (slot-positions config)))
        (add-player-inventory))))
  
  (add-slot [this inventory slot-index x y]
    (conj (:slots this) 
          {:inventory inventory
           :index slot-index
           :x x
           :y y}))
  
  (transfer-stack [_ player slot]
    (let [stack (get-stack slot)]
      (cond
        ; Transfer from player to matrix
        (and (player-slot? slot)
             (matrix-accepts? tile stack))
        (move-to-matrix tile stack)
        
        ; Transfer from matrix to player
        (and (matrix-slot? slot)
             (player-accepts? player stack))
        (move-to-player player stack)
        
        :else false)))
  
  (can-interact [_ player]
    (< (.getDistanceSq player (.getPosition tile)) 64.0)))

(defn create-gui-adapter []
  "Create new GUI adapter instance"
  {:config (->MatrixGuiConfig)
   :container-factory #(->MatrixContainer %1 %2 (create-inventory-adapter))})

(defn- add-plate-slots [container positions]
  (doseq [[idx [x y]] (map-indexed vector positions)]
    (add-slot container (:tile container) idx x y)))

(defn- add-core-slot [container [x y]]
  (add-slot container (:tile container) 3 x y))

(defn- add-player-inventory [container]
  (let [config (->MatrixGuiConfig)
        [inv-x inv-y] (:inventory-start (slot-positions config))
        [hot-x hot-y] (:hotbar-start (slot-positions config))]
    ; Add main inventory slots
    (doseq [row (range 3)
            col (range 9)]
      (add-slot container 
                (:player container)
                (+ (* row 9) col 9)
                (+ inv-x (* col 18))
                (+ inv-y (* row 18))))
    ; Add hotbar slots
    (doseq [col (range 9)]
      (add-slot container
                (:player container)
                col
                (+ hot-x (* col 18))
                hot-y))))

(defn- player-slot? [slot]
  (>= (:index slot) 9))

(defn- matrix-slot? [slot]
  (< (:index slot) 4))

(defn- matrix-accepts? [tile stack]
  (cond
    ; Core slot
    (= (:index slot) 3)
    (matrix/is-core-item? stack)
    
    ; Plate slots
    (< (:index slot) 3)
    (matrix/is-plate-item? stack)
    
    :else false))

(defn- player-accepts? [player stack]
  (or (empty-slot? player)
      (can-stack? player stack)))

(defn- move-to-matrix [tile stack]
  (when-let [target-slot (find-matrix-slot tile stack)]
    (set-stack target-slot stack)
    true))

(defn- move-to-player [player stack]
  (when-let [target-slot (find-player-slot player stack)]
    (set-stack target-slot stack)
    true))

(defn- find-matrix-slot [tile stack]
  (if (matrix/is-core-item? stack)
    (get-core-slot tile)
    (find-empty-plate-slot tile)))

(defn- find-player-slot [player stack]
  (or (find-matching-stack player stack)
      (find-empty-slot player)))

(defn- find-empty-plate-slot [tile]
  (->> (range 3)
       (filter #(nil? (get-stack tile %)))
       first))

(defn- create-inventory-adapter []
  (reify IInventoryAdapter
    (get-slot-count [_] 4)  ; 3 plates + 1 core
    (get-stack [this slot] nil)  ; Implementation provided by platform adapter
    (set-stack [this slot stack] nil)  ; Implementation provided by platform adapter
    (remove-stack [this slot amount] nil)  ; Implementation provided by platform adapter
    (is-empty [this] true)))  ; Implementation provided by platform adapter