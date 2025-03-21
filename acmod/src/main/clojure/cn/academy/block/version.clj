(ns cn.academy.block.version
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.persistence :as persist]
            [cn.academy.block.validation :as validation]
            [clojure.tools.logging :as log]))

;; Version tracking
(def version-state
  (atom {:current-version "1.0.0"
         :migrations {}
         :block-versions {}}))

;; Version comparison
(defn parse-version [version-str]
  (mapv #(Integer/parseInt %)
        (clojure.string/split version-str #"\.")))

(defn version< [v1 v2]
  (let [v1-parts (parse-version v1)
        v2-parts (parse-version v2)]
    (loop [p1 v1-parts
           p2 v2-parts]
      (cond
        (empty? p1) (seq p2)
        (empty? p2) false
        :else (let [n1 (first p1)
                    n2 (first p2)]
                (if (= n1 n2)
                  (recur (rest p1) (rest p2))
                  (< n1 n2)))))))

;; Migration registration
(defn register-migration! [from-version to-version migration-fn]
  (swap! version-state assoc-in 
         [:migrations [from-version to-version]] 
         migration-fn))

;; Config migration
(defn migrate-config! [config from-version to-version]
  (loop [current-config config
         current-version from-version]
    (if (= current-version to-version)
      current-config
      (if-let [next-version (->> (keys (:migrations @version-state))
                                (filter #(and (= (first %) current-version)
                                           (version< current-version (second %))))
                                (map second)
                                (sort)
                                first)]
        (if-let [migration (get-in @version-state [:migrations [current-version next-version]])]
          (recur (migration current-config) next-version)
          (throw (Exception. (str "No migration path from " current-version " to " next-version))))
        (throw (Exception. (str "No migration path to target version " to-version)))))))

;; Block version management
(defn get-block-version [block]
  (get-in @version-state [:block-versions (:id block)] "1.0.0"))

(defn set-block-version! [block version]
  (swap! version-state assoc-in [:block-versions (:id block)] version))

;; Version update handling
(defn update-block-version! [block target-version]
  (error/with-safe-execution (:id block) :version
    (let [current-version (get-block-version block)]
      (when (version< current-version target-version)
        (let [new-config (migrate-config! (:config block) 
                                        current-version 
                                        target-version)]
          (when (validation/validate-config! new-config)
            (swap! block assoc :config new-config)
            (set-block-version! block target-version)
            (persist/save-block-data! block)
            true))))))

;; Default migrations
(def default-migrations
  {"1.0.0" {"1.1.0" 
            (fn [config]
              (-> config
                  (update :energy-capacity #(* % 1.5))
                  (assoc :version "1.1.0")))
            
            "1.1.0" {"1.2.0"
                     (fn [config]
                       (-> config
                           (assoc :auto-output true)
                           (assoc :version "1.2.0")))}})

;; Initialize version system
(defn init-version-system! []
  ;; Register default migrations
  (doseq [[from-version migrations] default-migrations
          [to-version migration-fn] migrations]
    (register-migration! from-version to-version migration-fn))
  
  ;; Update loaded blocks to current version
  (let [current-version (:current-version @version-state)]
    (doseq [block (mcmod.block/get-loaded-blocks)]
      (update-block-version! block current-version))))