(ns cn.academy.block.lang
  (:require [mcmod.protocols :refer [ILocalizationProvider]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;; Localization state tracking
(def lang-state
  (atom {:translations {}
         :fallback "en_us"
         :current-locale nil}))

;; Localization provider implementation
(defrecord LocalizationProvider [state-atom]
  ILocalizationProvider
  (set-locale! [_ locale]
    (swap! state-atom assoc :current-locale locale))
  
  (get-locale []
    (:current-locale @state-atom))
  
  (add-translations! [_ locale translations]
    (swap! state-atom update-in [:translations locale] merge translations))
  
  (get-translation [this key & args]
    (let [locale (:current-locale @state-atom)
          fallback (:fallback @state-atom)
          translation (or (get-in @state-atom [:translations locale key])
                        (get-in @state-atom [:translations fallback key])
                        key)]
      (if args
        (apply format translation args)
        translation))))

;; Factory function
(defn create-provider []
  (->LocalizationProvider lang-state))

;; Translation file handling
(def translation-path "assets/academy/lang/")

(defn load-translation-file! [locale]
  (try
    (let [file-path (str translation-path locale ".edn")]
      (when (.exists (io/file file-path))
        (let [translations (edn/read-string (slurp file-path))
              provider (create-provider)]
          (.add-translations! provider locale translations))))
    (catch Exception e
      (log/error "Error loading translations for" locale ":" (.getMessage e)))))

;; Default translations
(def default-translations
  {"en_us" 
   {:machine {:status {:active "Active"
                      :inactive "Inactive"
                      :error "Error: {0}"
                      :no-energy "Insufficient Energy"
                      :no-resources "Missing Resources"}
             :upgrade {:installed "Upgrade Installed: {0}"
                      :removed "Upgrade Removed: {0}"
                      :invalid "Invalid Upgrade"}
             :gui {:title "{0} Machine"
                   :energy "Energy: {0}/{1}"
                   :progress "Progress: {0}%"}}
    
    :network {:status {:connected "Connected"
                      :disconnected "Disconnected"
                      :error "Network Error: {0}"}
             :messages {:packet-sent "Sent {0} packets"
                       :packet-received "Received {0} packets"}}
    
    :world {:interactions {:place "Placed {0}"
                          :break "Broke {0}"
                          :interact "Interacted with {0}"}
           :errors {:invalid-position "Invalid Position"
                   :chunk-not-loaded "Chunk Not Loaded"}}
    
    :errors {:generic "An error occurred: {0}"
            :validation "Validation failed: {0}"
            :timeout "Operation timed out: {0}"}}})

;; Message formatting helpers
(defn format-machine-status [status & args]
  (let [provider (create-provider)
        key (keyword (str "machine.status." (name status)))]
    (.get-translation provider key args)))

(defn format-network-message [type & args]
  (let [provider (create-provider)
        key (keyword (str "network.messages." (name type)))]
    (.get-translation provider key args)))

(defn format-error [type & args]
  (let [provider (create-provider)
        key (keyword (str "errors." (name type)))]
    (.get-translation provider key args)))

;; Initialize localization system
(defn init-lang! []
  (reset! lang-state {:translations {}
                      :fallback "en_us"
                      :current-locale "en_us"})
  
  ;; Load default translations
  (let [provider (create-provider)]
    (doseq [[locale translations] default-translations]
      (.add-translations! provider locale translations)))
  
  ;; Load additional translation files
  (doseq [file (.listFiles (io/file translation-path))
          :let [locale (str/replace (.getName file) #".edn$" "")]
          :when (.isFile file)]
    (load-translation-file! locale)))