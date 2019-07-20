(ns cn.li.mcmod.utils
  ;(:import (net.minecraftforge.fml.common FMLCommonHandler))
  (:require [clojure.string :as string])
  (:import (net.minecraft.world World)
           (net.minecraft.util.math BlockPos)))


;(def client? (.isClient (.getSide (FMLCommonHandler/instance))))

(defn handle-inner-classes
  "Helper function for gen-classname to handle changing '.'s into '$'s"
  [s]
  (let [class-name (string/split s #"\.")
        second-part (apply str (string/capitalize (first (second class-name))) (rest (second class-name)))]
    (str (first class-name) "$" second-part)))

(defn gen-classname
  "Given a symbol, returns a symbol representing a class name for java by capitalizing all words.
  Also turns '.'s into '$'s (in other words a '.' is used for inner classes)."
  [s]
  (let [s (str s)
        words (string/split s #"-")
        class-name (apply str (map string/capitalize words))
        class-name (if (.contains (str class-name) ".")
                     (handle-inner-classes class-name)
                     class-name)]
    (symbol class-name)))

(defn get-fullname
  "Given a namespace name and a class name, returns a fully qualified package name for
  a java class by using gen-classname on the class name and turning '-'s into '_'s in the package."
  [name-ns class-name]
  (symbol (str (string/replace name-ns #"-" "_") "." (gen-classname class-name))))

(defmacro with-prefix
  "Useful macro that takes a prefix (both strings and symbols work) and any number of statements.
  For each def/defn/def-/defn- statement within the macro, adds the prefix onto the name in each statement."
  [prefix & defs]
  (let [per-def (fn [possible-def]
                  (if (or (= (first possible-def) 'def) (= (first possible-def) 'defn) (= (first possible-def) 'def-) (= (first possible-def) 'defn-) (= (first possible-def) `def) (= (first possible-def) `defn) (= (first possible-def) `defn-))
                    (let [first-val (first possible-def)
                          def-name (second possible-def)
                          def-name (symbol (str prefix def-name))
                          def-statement (cons first-val (cons def-name (rest (rest possible-def))))]
                      def-statement)
                    possible-def))
        def-statements (cons `do (map per-def defs))]
    def-statements))

(defn get-tile-entity-at-world
  ([^World world pos]
   (.getTileEntity world pos))
  ([^World world x y z]
   (get-tile-entity-at-world world (BlockPos. (int x) (int y) (int z)))))
