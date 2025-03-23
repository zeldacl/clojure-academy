(ns cn.academy.forge-modern.data.data-generator
  (:require [cn.academy.core.data.generators :as generators]
            [clojure.data.json :as json])
  (:import [net.minecraft.data DataGenerator DirectoryCache]
           [net.minecraft.data.provider BlockStateProvider LanguageProvider]
           [net.minecraft.util ResourceLocation]
           [java.nio.file Path]))

(defn write-json! [^Path path data ^DirectoryCache cache]
  (let [json-str (json/write-str data)]
    (.putNew cache path (.getBytes json-str))))

(defn create-block-model-provider [^DataGenerator generator mod-id]
  (proxy [BlockStateProvider] [generator]
    (getName [] (str mod-id "_block_models"))
    
    (registerStatesAndModels []
      (let [{:keys [block-model blockstate]} (generators/generate-cat-engine-models)
            block-path (.resolve (.getOutputFolder this) 
                               (str "assets/" mod-id "/models/block/cat_engine.json"))
            state-path (.resolve (.getOutputFolder this)
                               (str "assets/" mod-id "/blockstates/cat_engine.json"))]
        (write-json! block-path block-model (.getCache this))
        (write-json! state-path blockstate (.getCache this))))))

(defn create-language-provider [^DataGenerator generator mod-id lang]
  (proxy [LanguageProvider] [generator mod-id lang]
    (add [key value]
      (proxy-super add key value))
    
    (addTranslations []
      (let [translations (get generators/localizations lang)]
        (doseq [[k v] translations]
          (.add this k v))))))

(defn register-providers [^DataGenerator generator mod-id]
  (.addProvider generator (create-block-model-provider generator mod-id))
  (.addProvider generator (create-language-provider generator mod-id "en_us"))
  (.addProvider generator (create-language-provider generator mod-id "zh_cn")))