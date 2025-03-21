(ns cn.academy.core.dev.commands
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.energy.chunk-cache :as cache]
            [mcmod.commands :as cmd]
            [mcmod.world :as world]
            [mcmod.position :as position]
            [mcmod.server :as server]))

(defn create-debug-command []
  (reify cmd/ICommand
    (get-name [_] "acdebug")
    
    (get-usage [_] "/acdebug <metrics|cache|reload>")
    
    (execute [_ context args]
      (let [subcmd (first args)
            source (:source context)]
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
          (let [world-obj (world/get-entity-world source)
                pos (position/get-position source)
                chunk-x (quot (:x pos) 16)
                chunk-z (quot (:z pos) 16)
                cache (cache/get-or-create-cache world-obj chunk-x chunk-z)
                nodes (cache/get-nodes cache)]
            (log-info "Energy nodes in current chunk:" (count nodes)))
          
          "reload"
          (do
            (require '[cn.academy.core.config :as config] :reload)
            (config/reload-config!)
            (log-info "Configuration reloaded"))
          
          (log-info "Unknown debug command:" subcmd))
        1))
    
    (get-permission-level [_] 4)))