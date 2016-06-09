(ns junto-labs.learn-specter.routes
  (:require ; WEBSOCKETS
            [taoensso.sente                   :as ws       ]
            ; ROUTING
            [compojure.core                   :as route
              :refer [GET ANY POST defroutes]              ]
            [compojure.route
              :refer [resources not-found]                 ]
            [ring.middleware.defaults         :as defaults ]
            [com.stuartsierra.component       :as component]
            [junto-labs.learn-specter.utils   :as u        ]))

(def sys-map (lens res/systems (fn-> :global :sys-map qcore/deref*)))

(def ring-ajax-get-or-ws-handshake (lens sys-map (fn-> :connection :ajax-get-or-ws-handshake-fn)))
(def ring-ajax-post                (lens sys-map (fn-> :connection :ajax-post-fn)))

(def server-root (u/->path (System/getProperty "user.dir") "/dev-resources/public")) ; TODO use config map for this

(def main-page #(slurp (u/->path server-root "index.html")))

(def not-found-page "<h1>Page not found. Sorry!</h1>")

(def chan-uri "/chan")

(defroutes routes*
  (GET "/"        req (fn [req]
                        {:headers {"Content-Type" "text/html"}
                         :body    (main-page)})) ; TODO make so it intelligently caches
  (GET  chan-uri  req (let [get-f @ring-ajax-get-or-ws-handshake]
                        (assert (nnil? get-f))
                        (get-f req)))
  (POST chan-uri  req (let [post-f @ring-ajax-post]
                        (assert (nnil? post-f))
                        (post-f req)))
  (not-found not-found-page))

(defn wrap-middleware [routes]
  (-> routes
      (defaults/wrap-defaults
        (-> defaults/secure-site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in [:static   :resources   ] false)
            (assoc-in [:static   :files       ] false)))))

(defroutes routes (wrap-middleware routes*))