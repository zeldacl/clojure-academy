(ns cn.li.mcmod.utils
  ;(:import (net.minecraftforge.fml.common FMLCommonHandler))
  (:require [clojure.string :as string])
  (:import (net.minecraft.world World)
           (net.minecraft.util.math BlockPos)
           (net.minecraft.inventory IInventory InventoryHelper)
           (net.minecraft.block BlockState ContainerBlock)
           (net.minecraft.entity.player PlayerEntity)
           (net.minecraft.util SoundEvents)
           (net.minecraftforge.registries GameData)
           (net.minecraftforge.common.util NonNullSupplier LazyOptional)
           (net.minecraftforge.fml.common.thread SidedThreadGroups)
           (net.minecraftforge.fml DistExecutor)
           (java.util.function Supplier)))


(defn ->NonNullSupplier [create-fn]
  (reify net.minecraftforge.common.util.NonNullSupplier
    (get [this]
      (create-fn))))

(defn ->LazyOptional [create-fn]
  (LazyOptional/of (->NonNullSupplier create-fn)))

(defn ensure-registered []
  (let [_ SoundEvents/AMBIENT_CAVE]
    (GameData/init)))

(ensure-registered)


(def client? (= (.getThreadGroup (Thread/currentThread)) SidedThreadGroups/CLIENT))
(defn vec->map [v] (into {} (map vec (partition 2 v))))

(defn handle-inner-classes [s]
  (let [class-name (string/split s #"\.")
        second-part (apply str (string/capitalize (first (second class-name))) (rest (second class-name)))]
    (str (first class-name) "$" second-part)))

(defn gen-classname [s]
  (let [s (str s)
        words (string/split s #"-")
        class-name (apply str (map string/capitalize words))
        class-name (if (.contains (str class-name) ".")
                     (handle-inner-classes class-name)
                     class-name)]
    (symbol class-name)))

(defn get-fullname
  [name-ns class-name]
  (symbol (str (string/replace name-ns #"-" "_") "." (gen-classname class-name))))

(defn gen-method [k]
  (let [key-name (name k)
        words (string/split key-name #"-")
        method-name (apply str (first words) (map string/capitalize (rest words)))]
    (symbol method-name)))

(defn update-map-keys [func m]
  (into {} (map #(vector (func (key %1)) (val %1)) m)))

(defn construct [klass & args]
  (clojure.lang.Reflector/invokeConstructor klass (into-array Object args)))

(defmacro with-prefix [prefix & defs]
  (let [per-def (fn [possible-def]
                  (if (or
                        (= (first possible-def) 'def)
                        (= (first possible-def) 'defn)
                        (= (first possible-def) 'def-)
                        (= (first possible-def) 'defn-)
                        (= (first possible-def) `def)
                        (= (first possible-def) `defn)
                        (= (first possible-def) `defn-))
                    (let [first-val (first possible-def)
                          def-name (second possible-def)
                          def-name (symbol (str prefix def-name))
                          def-statement (cons first-val (cons def-name (rest (rest possible-def))))]
                      def-statement)
                    possible-def))
        def-statements (cons `do (map per-def defs))]
    def-statements))

(defn get-tile-entity-at-world
  ([^World world pos]
   (.getTileEntity world pos))
  ([^World world x y z]
   (get-tile-entity-at-world world (BlockPos. (int x) (int y) (int z)))))

(defn blockstate->block [^BlockState state]
  (.getBlock state))

(defn same-block? [block1 block2]
  (identical? (blockstate->block block1) (blockstate->block block2)))

(defn drop-inventory-items [world pos, block-obj]
  (let [tileentity (get-tile-entity-at-world world pos)]
    (when (instance? IInventory tileentity)
      (InventoryHelper/dropInventoryItems ^World world ^BlockPos pos ^IInventory tileentity)
      (.updateComparatorOutputLevel ^World world pos block-obj))))

(defn get-container [^ContainerBlock block ^BlockState state, ^World worldIn, ^BlockPos pos]
  (.getContainer block state worldIn pos))

(defn open-gui [^PlayerEntity player ^BlockState state, ^World worldIn, ^BlockPos pos ^ContainerBlock block]
  (when-let [inamedcontainerprovider (get-container block state worldIn pos)]
    (.openContainer player inamedcontainerprovider)
    ;(.addStat player (.getOpenStat block))
    ))

(defmacro run-for-dist [client-fn sever-fn]
  `(DistExecutor/runForDist
    (proxy [Supplier] []
      (get []
        (proxy [Supplier] []
          (get []
            (~client-fn)
            ))))
    (proxy [Supplier] []
      (get []
        (proxy [Supplier] []
          (get []
            (~sever-fn)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
