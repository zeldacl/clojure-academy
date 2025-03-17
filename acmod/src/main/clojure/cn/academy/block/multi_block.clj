(ns cn.academy.block.multi-block)

(defprotocol IMultiBlock
  (get-hardness [this])
  (get-light-level [this])
  (get-sub-blocks [this])
  (get-rot-center [this]))

(defprotocol IMultiBlockInfo
  (get-direction [this])  
  (set-direction! [this dir])
  (get-sub-id [this])
  (set-sub-id! [this id])
  (loaded? [this])
  (set-loaded! [this state]))

(defrecord MultiBlockInfo [direction sub-id loaded]
  IMultiBlockInfo
  (get-direction [_] @direction)
  (set-direction! [_ dir] (reset! direction dir))
  (get-sub-id [_] @sub-id)
  (set-sub-id! [_ id] (reset! sub-id id))
  (loaded? [_] @loaded)
  (set-loaded! [_ state] (reset! loaded state)))

(defn create-multi-block-info []
  (->MultiBlockInfo (atom :north) (atom 0) (atom false)))