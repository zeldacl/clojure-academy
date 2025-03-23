(ns cn.academy.dev.repl
  (:require [cn.academy.core.util.logging :refer [log-info log-debug]]
            [cn.academy.dev.util :as dev]
            [clojure.main :as main]
            [clojure.string :as str])
  (:import [java.io PushbackReader StringReader]))

(def ^:private repl-state (atom {}))

(defn create-dev-repl []
  (let [current-ns (ns-name *ns*)]
    (try
      (in-ns 'cn.academy.dev.repl)
      (binding [*ns* *ns*]
        (let [input "(+ 1 2)"]
          (with-in-str input
            (main/repl 
              :init #(do
                      (println "Academy Dev REPL")
                      (println "Type :help for available commands"))
              :prompt #(print "academy=> ")
              :caught (fn [e]
                       (log-debug e "REPL Error")
                       (println (str "Error: " (.getMessage e))))))))
      (finally
        (in-ns current-ns)))))

(defn evaluate [code]
  (try
    (let [rdr (PushbackReader. (StringReader. code))
          result (eval (read rdr))]
      [:ok result])
    (catch Throwable t
      [:error (.getMessage t)])))

(defn handle-command [cmd]
  (case cmd
    ":help" (str/join "\n"
              ["Available commands:"
               ":help    - Show this help"
               ":reload  - Reload the mod"
               ":diag    - Generate diagnostic report"
               ":profile - Start profiling"
               ":clear   - Clear caches"])
    ":reload" (dev/reload-mod!)
    ":diag" (dev/dump-diagnostics!)
    ":profile" (dev/start-profiling!)
    ":clear" (dev/clear-cache!)
    nil))

(defmacro with-dev-session [& body]
  `(binding [*ns* (find-ns 'cn.academy.dev.repl)]
     ~@body))

(defn start-dev-session! []
  (log-info "Starting development session...")
  (create-dev-repl))