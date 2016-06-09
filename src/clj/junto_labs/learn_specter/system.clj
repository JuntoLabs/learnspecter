(ns junto-labs.learn-specter.system
  (:require
    [com.stuartsierra.component          :as component]
    [junto-labs.learn-specter.routes     :as routes   ]
    [junto-labs.learn-specter.handlers   :as handlers ]
    [junto-labs.learn-specter.websockets :as ws       ]
    [junto-labs.learn-specter.server     :as server   ]))

(def config
  (let [host        "localhost"
        port        80
        server-type :immutant]
    {:server     {:type           server-type
                  :routes-var     #'routes/routes
                  :make-routes-fn routes/make-base-routes
                  :host           host
                  :port           port
                  :root-path      (u/->path (System/getProperty "user.dir") "/dev-resources/public")
                  :ws-uri         "/chan"}
     :websockets {:uri         "/chan" ; TODO add this to routes
                  :host        (str host ":" port)
                  :server-type server-type
                  :msg-handler handlers/event-msg-handler}}))

(defonce system
  (component/system-map
    {:server     (server/map->Server (:server config))
     :websockets (component/using 
                   (ws/map->ChannelSocket (:websockets config))
                   [:server])}))