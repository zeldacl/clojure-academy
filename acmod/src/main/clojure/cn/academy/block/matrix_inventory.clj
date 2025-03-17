(ns cn.academy.block.matrix-inventory)

(defprotocol IMatrixInventory
  (valid-for-slot? [this slot item])
  (try-transfer-stack [this from-slot to-slot stack])
  (get-transfer-rules [this]))

(defrecord MatrixInventory [core]
  IMatrixInventory
  (valid-for-slot? [_ slot item]
    (case slot
      (0 1 2) (= (:type item) :constraint-plate)
      3 (= (:type item) :mat-core)
      false))
  
  (try-transfer-stack [this from-slot to-slot stack]
    (when (valid-for-slot? this to-slot stack)
      {:allow? true
       :max-size (if (#{0 1 2 3} to-slot) 1 64)}))
  
  (get-transfer-rules [_]
    [{:from-slots #{:player-inventory}
      :to-slots #{0 1 2}
      :item-type :constraint-plate}
     {:from-slots #{:player-inventory}
      :to-slots #{3}
      :item-type :mat-core}
     {:from-slots #{0 1 2 3}
      :to-slots #{:player-inventory}}]))