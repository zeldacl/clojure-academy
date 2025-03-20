(ns mcmod.block.block-state
  (:require [mcmod.protocols :refer :all]))

;; Block property definitions
(defprotocol IBlockProperty
  (get-name [this] "Get property name")
  (get-values [this] "Get possible values")
  (get-default [this] "Get default value")
  (validate [this value] "Validate if value is allowed"))

;; Block state definitions
(defprotocol IBlockState
  (get-properties [this] "Get all properties")
  (get-property [this name] "Get property by name")
  (get-property-value [this name] "Get current value of property")
  (with-property [this name value] "Create new state with updated property")
  (get-default-state [this] "Get default state"))

;; Property implementations
(defrecord BooleanProperty [name default]
  IBlockProperty
  (get-name [_] name)
  (get-values [_] [true false])
  (get-default [_] default)
  (validate [_ value] (boolean? value)))

(defrecord IntegerProperty [name min max default]
  IBlockProperty
  (get-name [_] name)
  (get-values [_] (range min (inc max)))
  (get-default [_] default)
  (validate [_ value]
    (and (integer? value)
         (<= min value max))))

(defrecord EnumProperty [name values default]
  IBlockProperty
  (get-name [_] name)
  (get-values [_] values)
  (get-default [_] default)
  (validate [_ value]
    (contains? (set values) value)))

;; Factory functions
(defn create-bool-property 
  ([name] (create-bool-property name false))
  ([name default] 
   (->BooleanProperty name default)))

(defn create-int-property [name min max default]
  (->IntegerProperty name min max default))

(defn create-enum-property [name values default]
  (->EnumProperty name values default))

;; Block state implementation
(defrecord BlockState [properties values]
  IBlockState
  (get-properties [_] properties)
  
  (get-property [_ name]
    (get properties name))
  
  (get-property-value [_ name]
    (get values name))
  
  (with-property [this name value]
    (if-let [prop (get-property this name)]
      (if (validate prop value)
        (assoc-in this [:values name] value)
        this)
      this))
  
  (get-default-state [_]
    (->BlockState properties 
                  (reduce (fn [m [k v]]
                           (assoc m k (get-default v)))
                         {}
                         properties))))

(defn create-block-state [properties]
  (let [state (->BlockState properties {})]
    (get-default-state state)))