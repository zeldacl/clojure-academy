(ns cljacademy.client.registry
  (:require [cn.academy.block.matrix.registry :as matrix]
            [cn.academy.block.matrix.render :as matrix-render]))

(def renderers
  {"wireless.matrix" (matrix/register-renderer)})