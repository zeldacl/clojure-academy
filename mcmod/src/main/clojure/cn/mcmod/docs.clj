(ns cn.mcmod.docs
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [cn.mcmod.registry :as registry]
            [cn.mcmod.protocols :refer :all]
            [cn.mcmod.capabilities :as capabilities])
  (:import [java.io File]))

(defn extract-block-docs [block-entry]
  (let [[block-id block] block-entry]
    {:id block-id
     :properties {:hardness (get-hardness block)
                  :resistance (get-resistance block)
                  :light-level (get-light-level block)}
     :capabilities (if (satisfies? capabilities/ICapabilityProvider block)
                    (list "Energy Storage")
                    [])}))

(defn generate-block-docs [registry]
  (for [block-entry (registry/get-blocks registry)]
    (extract-block-docs block-entry)))

(defn save-docs [docs output-dir]
  (let [dir (io/file output-dir)]
    (.mkdirs dir)
    (spit (io/file dir "blocks.md")
          (str "# Block Documentation\n\n"
               (str/join "\n\n"
                        (for [block docs]
                          (str "## " (:id block) "\n"
                               "\nProperties:\n"
                               (str/join "\n"
                                       (for [[k v] (:properties block)]
                                         (str "- " (name k) ": " v)))
                               "\n\nCapabilities:\n"
                               (str/join "\n"
                                       (map #(str "- " %) (:capabilities block))))))))))

(defn generate-docs [registry output-dir]
  (let [block-docs (generate-block-docs registry)]
    (save-docs block-docs output-dir)))
