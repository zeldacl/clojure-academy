(ns cn.li.mcmod.global
  (:require [clojure.tools.logging :as log]))


(defonce ^:dynamic *mod-id* "cljacademy")

(defn set-mod-id [mod-id]
  (log/info "vzvzvfasfasdasda " *mod-id* mod-id)
  (alter-var-root #'*mod-id* (constantly mod-id)))

(defonce ^:dynamic *item-group* nil)

(defonce ^:dynamic *blocks* (atom {}))
(defonce ^:dynamic *tile-entities* (atom {}))
(defonce ^:dynamic *block-items* (atom {}))
(defonce ^:dynamic *containers* (atom {}))