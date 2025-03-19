(ns mcmod.docs
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [mcmod.registry :as registry])
  (:import [java.io File]))

(defn extract-block-docs [block]
  {:id (registry/get-block-id block)
   :properties {:hardness (get-hardness block)
                :resistance (get-resistance block)
                :light-level (get-light-level block)}
   :capabilities (if (satisfies? mcmod.capabilities/ICapabilityProvider block)
                  (list "Energy Storage"))})

(defn generate-block-docs [registry]
  (for [block (registry/get-registered-blocks registry)]
    (extract-block-docs block)))

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