(ns cn.li.bridge.nbt.api)

(defprotocol INBTSerializable
  "NBT serialization functionality"
  (serialize-nbt [this] "Serialize to NBT")
  (deserialize-nbt [this nbt] "Deserialize from NBT"))

(defprotocol INBTConverter
  "NBT data conversion"
  (to-nbt [this value] "Convert value to NBT")
  (from-nbt [this nbt] "Convert NBT to value"))

(defprotocol INBTStorage
  "NBT data storage"
  (put-value [this key value] "Store value with key")
  (get-value [this key] "Get value by key")
  (remove-value [this key] "Remove value by key")
  (get-all-keys [this] "Get all stored keys"))