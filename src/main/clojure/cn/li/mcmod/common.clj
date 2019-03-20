(ns cn.li.mcmod.common)

(defmacro defclass
  ([name meta]
    (let [prefix (str name "-")
          gen-data {}]
      `(do
         (gen-class
           :name name
           :prefix ~prefix
           ~@gen-data)))))
