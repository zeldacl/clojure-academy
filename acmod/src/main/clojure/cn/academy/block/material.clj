(ns cn.academy.block.material)

(defprotocol IMaterialMapper
  (get-material [this material-name]))

(def material-names
  {"rock" :rock
   "iron" :iron
   "wood" :wood
   "air" :air})

(defmulti create-material 
  "Create a Minecraft material based on the Forge version"
  (fn [forge-version material-key] forge-version))

(defn get-material-key [material-name]
  (get material-names material-name :rock))