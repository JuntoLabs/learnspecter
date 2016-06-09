(ns junto-labs.learn-specter.system
  (:require
    [com.stuartsierra.component          :as component]
    [junto-labs.learn-specter.routes     :as routes   ]
    [junto-labs.learn-specter.handlers   :as handlers ]
    [junto-labs.learn-specter.websockets :as ws       ]
    [junto-labs.learn-specter.server     :as server   ]
    [junto-labs.learn-specter.utils      :as u        ]))

(def config
  {:server     {:type           :immutant
                :routes-var     #'routes/routes
                :make-routes-fn routes/make-base-routes
                :host           "localhost"
                :port           32000
                :root-path      (u/->path (System/getProperty "user.dir") "/dev-resources/public")}
   :websockets {:endpoint       "/chan"
                :msg-handler    handlers/event-msg-handler*
                :make-routes-fn routes/make-routes}})

(defonce system
  (component/system-map ; TODO logging
    :server     (server/map->Server (:server config))
    :websockets (component/using 
                  (ws/map->ChannelSocket (:websockets config))
                  [:server])))

(defn -main [] (component/start system))