(ns cljacademy.client.container.registry
  (:require [cn.academy.block.matrix.registry :as matrix]))

(def containers
  {"wireless.matrix" (matrix/register-container)})