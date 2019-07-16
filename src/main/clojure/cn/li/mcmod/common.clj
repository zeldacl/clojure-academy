(ns cn.li.mcmod.common)

(defn vec->map [v] (into {} (map vec (partition 2 v))))

(defmacro defclass
  ([name meta]
    (let [name-ns (get meta :ns *ns*)
          prefix (str name "-")
          gen-data {}]
      `(do
         (gen-class
           :name name
           :prefix ~prefix
           ~@gen-data)))))
