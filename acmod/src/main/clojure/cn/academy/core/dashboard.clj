(ns cn.academy.core.dashboard
  (:require [cn.academy.core.monitoring :as monitoring]
            [cn.academy.core.profiling :as profiling]
            [cn.academy.core.diagnostics :as diagnostics]
            [cn.academy.block.network.monitor :as network]
            [cn.academy.block.world.monitor :as world]
            [cn.academy.block.render.monitor :as render]
            [cn.academy.core.config :as config]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [ring.adapter.jetty :as jetty]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [clojure.tools.logging :as log]))

;; Dashboard state
(def dashboard-state
  (atom {:server nil
         :last-update nil}))

;; Dashboard page generation
(defn render-metric-chart [title metrics]
  [:div.metric-chart
   [:h3 title]
   [:div.chart-container
    [:canvas {:id (str "chart-" (name title))
             :data-metrics (pr-str metrics)}]]])

(defn render-system-metrics []
  (let [runtime (Runtime/getRuntime)
        mb (/ 1024.0 1024.0)]
    [:div.system-metrics
     [:h2 "System Metrics"]
     [:div.metrics-grid
      [:div.metric
       [:h4 "Memory Usage"]
       [:div.value 
        (format "%.1f MB / %.1f MB"
                (/ (- (.totalMemory runtime) 
                     (.freeMemory runtime)) 
                   mb)
                (/ (.maxMemory runtime) mb))]]
      [:div.metric
       [:h4 "Active Threads"]
       [:div.value (.activeCount (Thread/currentThread))]]
      [:div.metric
       [:h4 "GC Time"]
       [:div.value (format "%.2f ms" 
                          (monitoring/get-metric "system" "gc-time"))]]]]))

(defn render-performance-metrics []
  [:div.performance-metrics
   [:h2 "Performance"]
   [:div.metrics-grid
    (render-metric-chart "Frame Times" 
                        (render/analyze-frame-times))
    (render-metric-chart "Network Traffic"
                        (network/generate-network-report))
    (render-metric-chart "World Performance"
                        (world/check-world-health))]])

(defn render-diagnostics []
  [:div.diagnostics
   [:h2 "Diagnostics"]
   [:div.reports
    [:div.report
     [:h3 "Recent Errors"]
     [:pre (diagnostics/generate-report)]]
    [:div.report
     [:h3 "Performance Hotspots"]
     [:pre (profiling/generate-profile-report "machine")]
     [:pre (profiling/generate-profile-report "world")]
     [:pre (profiling/generate-profile-report "render")]]]])

(defn render-configuration []
  [:div.configuration
   [:h2 "Configuration"]
   [:form {:method "POST" :action "/config/update"}
    [:div.config-section
     [:h3 "Monitoring Thresholds"]
     (for [[category metrics] (get-in @config/config-state 
                                    [:monitoring :thresholds])]
       [:div.category
        [:h4 (name category)]
        (for [[metric value] metrics]
          [:div.setting
           [:label (name metric)]
           [:input {:type "number"
                   :name (str (name category) "." (name metric))
                   :value value}]])])]
    [:div.config-section
     [:h3 "Profiling"]
     [:div.setting
      [:label "Enabled"]
      [:input {:type "checkbox"
               :name "profiling.enabled"
               :checked (get-in @config/config-state 
                              [:profiling :enabled])}]]
     [:div.setting
      [:label "Sample Rate"]
      [:input {:type "number"
               :name "profiling.sample-rate"
               :value (get-in @config/config-state 
                            [:profiling :sample-rate])}]]]
    [:button {:type "submit"} "Save Configuration"]]])

(defn render-dashboard []
  (page/html5
    [:head
     [:title "AcademyCraft Monitoring Dashboard"]
     [:meta {:charset "UTF-8"}]
     (page/include-css "/css/dashboard.css")
     (page/include-js "/js/charts.js")]
    [:body
     [:div#dashboard
      [:header
       [:h1 "AcademyCraft Monitoring Dashboard"]
       [:div.last-update
        "Last Updated: " 
        (.format (java.text.SimpleDateFormat. "HH:mm:ss")
                (java.util.Date.))]]
      (render-system-metrics)
      (render-performance-metrics)
      (render-diagnostics)
      (render-configuration)]]))

;; Web server routes
(defroutes dashboard-routes
  (GET "/" [] (render-dashboard))
  (GET "/metrics" [] 
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (monitoring/get-all-metrics)})
  (route/resources "/"))

;; Server management
(defn start-dashboard! 
  "Start dashboard web server"
  [& {:keys [port] :or {port 8080}}]
  (when-not (:server @dashboard-state)
    (try
      (let [server (jetty/run-jetty dashboard-routes
                                   {:port port :join? false})]
        (swap! dashboard-state assoc 
               :server server
               :last-update (System/currentTimeMillis))
        (log/info "Started monitoring dashboard on port" port)
        true)
      (catch Exception e
        (log/error "Failed to start dashboard:" (.getMessage e))
        false))))

(defn stop-dashboard!
  "Stop dashboard web server"
  []
  (when-let [server (:server @dashboard-state)]
    (.stop server)
    (swap! dashboard-state dissoc :server)
    (log/info "Stopped monitoring dashboard")
    true))