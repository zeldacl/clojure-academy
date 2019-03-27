(ns cn.li.mcmod.common)

(defn vec->map [v] (into {} (map vec (partition 2 v))))

(defmacro defclass
  ([name meta]
    (let [prefix (str name "-")
          gen-data {}]
      `(do
         (gen-class
           :name name
           :prefix ~prefix
           ~@gen-data)))))
