(ns cn.academy.core.config)

(def ^:private config-state (atom {}))

(def default-config
  {:cat-engine
   {:energy-gen-rate 5.0
    :max-energy 100000.0
    :wireless-range 16
    :allow-interdimensional true}})

(defn get-config 
  ([path] (get-config path nil))
  ([path default]
   (get-in @config-state path default)))

(defn set-config! [path value]
  (swap! config-state assoc-in path value))

(defn load-config! [config-data]
  (reset! config-state (merge default-config config-data)))

(defn get-cat-engine-config []
  (get-config [:cat-engine] {}))