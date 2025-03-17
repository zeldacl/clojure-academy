(ns cn.academy.core.dev.commands
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.energy.chunk-cache :as cache])
  (:import [net.minecraft.command CommandBase ICommandSender]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.server MinecraftServer]))

(defn create-debug-command []
  (proxy [CommandBase] []
    (getName [] "acdebug")
    
    (getUsage [_] "/acdebug <metrics|cache|reload>")
    
    (execute [^MinecraftServer server sender args]
      (let [subcmd (first args)]
        (case subcmd
          "metrics"
          (do
            (log-info "Performance metrics:")
            (doseq [[metric data] (monitoring/get-metrics)]
              (log-info (format "%s: %d calls, %.2f ms avg" 
                              metric 
                              (:count data) 
                              (:avg-ms data)))))
          
          "cache"
          (let [world (.getEntityWorld sender)
                chunk-x (quot (.getX (.getPosition sender)) 16)
                chunk-z (quot (.getZ (.getPosition sender)) 16)
                cache (cache/get-or-create-cache world chunk-x chunk-z)
                nodes (cache/get-nodes cache)]
            (log-info "Energy nodes in current chunk:" (count nodes)))
          
          "reload"
          (do
            (require '[cn.academy.core.config :as config] :reload)
            (config/reload-config!)
            (log-info "Configuration reloaded"))
          
          (log-info "Unknown debug command:" subcmd))))
    
    (checkPermission [server sender]
      (or (.isSinglePlayer server)
          (.canUseCommand sender 4 "debug")))))