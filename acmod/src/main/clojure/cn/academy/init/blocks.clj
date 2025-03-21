(ns cn.academy.init.blocks
  (:require [cn.academy.blocks.block_matrix.core :as matrix]
            [clojure.tools.logging :as log]))

(defn init-blocks! []
  ;; Initialize all blocks in the mod
  (log/info "Initializing Academy blocks...")
  
  ;; Initialize the Wireless Matrix block
  (matrix/init!)
  
  (log/info "Academy blocks initialized successfully"))