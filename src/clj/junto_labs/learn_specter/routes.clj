(ns junto-labs.learn-specter.routes
  (:require [taoensso.timbre                  :as log      ]
            [taoensso.sente                   :as ws       ]
            [compojure.core                   :as route
              :refer [GET ANY POST defroutes]              ]
            [compojure.route
              :refer [resources not-found]                 ]
            [ring.middleware.defaults         :as defaults ]
            [com.stuartsierra.component       :as component]
            [junto-labs.learn-specter.utils   :as u        ]))

(defn main-page [server-root-path]
  (slurp (u/->path server-root-path "index.html")))

(def not-found-page "<h1>Page not found. Sorry!</h1>")

(defn wrap-middleware [routes]
  (-> routes
      (defaults/wrap-defaults
        (-> defaults/site-defaults ; was secure-site-defaults
            (assoc-in [:static   :resources   ] false)
            (assoc-in [:static   :files       ] false)))))

(defn base-routes
  [{:keys [root-path]}]
  [(GET "/" [] (fn [req]
                 (log/debug "In main page with req" req)
                 {:headers {"Content-Type" "text/html"}
                  :body    (main-page root-path)}))]) ; TODO make so it intelligently caches

(defn make-base-routes
  [opts]
  (wrap-middleware
    (apply route/routes (base-routes opts))))

(defn ws-routes
  [{:keys [ws-uri get-fn post-fn]}]
  (assert (fn? get-fn )) ; TODO use clojure.spec
  (assert (fn? post-fn))
  (assert (string? ws-uri))
  [(GET  ws-uri req (do (log/debug "in websocket GET with" req) (get-fn  req)))
   (POST ws-uri req (do (log/debug "in websocket POST with" req) (post-fn req)))])

(defn make-routes 
  [opts]
  (wrap-middleware
    (apply route/routes (concat (base-routes opts)
                                (ws-routes   opts)
                                [(not-found not-found-page)]))))

(defonce routes nil) ; just a var which will be modified by Server component