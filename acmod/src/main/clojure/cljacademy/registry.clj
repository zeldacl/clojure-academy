(ns cljacademy.registry
  (:require [cn.academy.block.matrix.registry :as matrix]))

(def tile-entities
  {"wireless.matrix" (matrix/register-tile-entity)})