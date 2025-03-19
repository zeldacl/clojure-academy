(ns mcmod.dev
  (:require [mcmod.protocols :refer :all]
            [mcmod.registry :as registry]
            [clojure.pprint :refer [pprint]]))

(defn debug-print [& args]
  (when (System/getProperty "mcmod.debug")
    (apply println args)))

(defn inspect-registry [registry]
  (debug-print "\nRegistry Contents:")
  (debug-print "Blocks:")
  (pprint (get-registered-blocks registry))
  (debug-print "\nItems:")
  (pprint (get-registered-items registry))
  (debug-print "\nTile Entities:")
  (pprint (get-registered-tile-entities registry)))

(defmacro with-dev-context [& body]
  `(try
     (System/setProperty "mcmod.debug" "true")
     ~@body
     (finally
       (System/setProperty "mcmod.debug" "false"))))