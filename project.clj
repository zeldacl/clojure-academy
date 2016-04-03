(defproject clojureacamedy "0.1.0"
            :description ""
            :dependencies [[org.clojure/clojure "1.6.0"]
                           [com.google.guava/guava "18.0"]
                           [org.apache.logging.log4j/log4j-core "2.3"]
                           ;[net.minecraftforge.forge/forgeSrc "1.7.10-10.13.4.1448-1.7.10"]
                           ;[net.minecraftforge.gradle/ForgeGradle "1.2-SNAPSHOT"]
                           ]
            :plugins [[lein-cljfmt "0.1.10"]
                      [lein-kibit "0.1.2"]
                      [jonase/eastwood "0.2.1"]]
            :prep-tasks [["compile" "cn.li.academy.block.tileentity.TileEntityInventory"] "javac"]
            :aot :all
            :source-paths ["src/main/clojure"]
            :repositories [["forge" "http://files.minecraftforge.net/maven"]
                           ["clojars" "http://clojars.org/repo"]])