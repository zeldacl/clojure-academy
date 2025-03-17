(ns cn.academy.core.data.generators)

(defprotocol IDataGenerator
  (generate-block-models [this])
  (generate-item-models [this])
  (generate-blockstates [this])
  (generate-localization [this]))

(defn generate-cat-engine-models []
  {:block-model
   {:parent "block/cube_all"
    :textures {:all "academy:blocks/cat_engine"}}
   
   :item-model
   {:parent "academy:block/cat_engine"}
   
   :blockstate
   {:variants
    {"" {:model "academy:block/cat_engine"}}}})

(def localizations
  {"en_us"
   {"block.academy.cat_engine" "Cat Engine"
    "ac.cat_engine.linked" "Linked to %s"
    "ac.cat_engine.unlink" "Unlinked"
    "ac.cat_engine.notfound" "No wireless node found in range"}
   
   "zh_cn"
   {"block.academy.cat_engine" "猫引擎"
    "ac.cat_engine.linked" "已连接到 %s"
    "ac.cat_engine.unlink" "已解除连接"
    "ac.cat_engine.notfound" "范围内未找到无线节点"}})