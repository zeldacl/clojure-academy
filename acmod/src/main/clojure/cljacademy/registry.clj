(ns cljacademy.registry
  (:require [cljacademy.blocks.matrix-tile :as matrix]))

(def tile-entities
  {"wireless.matrix" matrix/create-matrix-tile})