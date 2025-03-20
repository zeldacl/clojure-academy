(ns setup-dev
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn check-prerequisites []
  (println "Checking development prerequisites...")
  
  ;; Check Java version
  (let [java-version (System/getProperty "java.version")
        major-version (Integer/parseInt (first (str/split java-version #"\D")))]
    (when (> major-version 8)
      (println "WARNING: Project requires Java 8. Current version:" java-version)))
      
  ;; Check Clojure installation
  (try
    (require '[clojure.tools.deps.alpha :as deps])
    (println "Clojure tools.deps found")
    (catch Exception _
      (println "ERROR: Clojure tools.deps not found. Please install Clojure CLI tools"))))

(defn setup-dev-environment []
  ;; Create development directories if needed
  (doseq [dir ["run/config"
               "run/mods"
               "run/logs"
               "src/generated/resources"
               "src/test/resources"
               "src/integration-test/resources"]]
    (.mkdirs (io/file dir)))
    
  ;; Copy sample config files
  (when-not (.exists (io/file "run/config/forge.toml"))
    (spit "run/config/forge.toml"
          (str "[general]\n"
               "gameTestServer=true\n"
               "debugLogging=true\n")))
               
  ;; Set up test resources
  (when-not (.exists (io/file "src/test/resources/pack.mcmeta"))
    (spit "src/test/resources/pack.mcmeta"
          "{\"pack\":{\"description\":\"Test resources\",\"pack_format\":6}}")))

(defn configure-ide []
  (println "Configuring IDE settings...")
  
  ;; Create .idea settings for IntelliJ if used
  (when (.exists (io/file ".idea"))
    (spit ".idea/clojure.xml"
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
           <project version=\"4\">
             <component name=\"ClojureProjectResolveSettings\">
               <item key=\"clojure.source.paths\" value=\"src/main/clojure;src/test/clojure\" />
             </component>
           </project>"))
           
  ;; Create .vscode settings for VS Code if used  
  (when (.exists (io/file ".vscode"))
    (spit ".vscode/settings.json"
          (str "{\n"
               "  \"calva.replConnectSequences\": [{\n"
               "    \"name\": \"Academy Craft Development\",\n"
               "    \"projectType\": \"Gradle\"\n"
               "  }]\n"
               "}"))))

(defn -main []
  (println "Setting up Academy Craft development environment...")
  
  (check-prerequisites)
  (setup-dev-environment)
  (configure-ide)
  
  (println "\nDevelopment environment setup complete!")
  (println "Run './gradlew build' to build the project")
  (println "Run './gradlew test integrationTest' to run tests")
  (println "Run './gradlew runClient' to start Minecraft with the mod"))