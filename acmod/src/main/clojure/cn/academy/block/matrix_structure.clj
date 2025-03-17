(ns cn.academy.block.matrix-structure
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IStructureValidator
  (validate-structure [this world pos])
  (find-structure-blocks [this world pos])
  (form-structure! [this world pos])
  (break-structure! [this world pos]))

(defn relative-to-absolute [base-pos [rx ry rz]]
  {:x (+ (:x base-pos) rx)
   :y (+ (:y base-pos) ry)
   :z (+ (:z base-pos) rz)})

(defrecord MatrixStructure [block-validator]
  IStructureValidator
  (validate-structure [_ world pos]
    (every? #(block-validator world (relative-to-absolute pos %))
            matrix/matrix-structure))
  
  (find-structure-blocks [_ world pos]
    (map #(relative-to-absolute pos %) 
         matrix/matrix-structure))
  
  (form-structure! [this world pos]
    (when (validate-structure this world pos)
      (doseq [block-pos (find-structure-blocks this world pos)]
        (mark-as-formed! world block-pos))))
  
  (break-structure! [this world pos]
    (doseq [block-pos (find-structure-blocks this world pos)]
      (mark-as-broken! world block-pos))))

(defn create-structure [block-validator]
  (->MatrixStructure block-validator))