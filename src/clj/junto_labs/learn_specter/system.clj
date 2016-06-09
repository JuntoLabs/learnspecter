(ns junto-labs.learn-specter.system
  (:require
    [taoensso.timbre                     :as log      ]
    [com.stuartsierra.component          :as component]
    [junto-labs.learn-specter.routes     :as routes   ]
    [junto-labs.learn-specter.handlers   :as handlers ]
    [junto-labs.learn-specter.websockets :as ws       ]
    [junto-labs.learn-specter.server     :as server   ]
    [junto-labs.learn-specter.utils      :as u        ]
    [environ.core
      :refer [env]                                    ]))

(def config
  {:server     {:type           :immutant
                :routes-var     #'routes/routes
                :make-routes-fn routes/make-base-routes
                :host           "localhost"
                :port           8080
                :root-path      (u/->path (System/getProperty "user.dir") "/dev-resources/public")}
   :websockets {:endpoint       "/chan"
                :msg-handler    handlers/event-msg-handler*
                :make-routes-fn routes/make-routes}})

(defn make-system-map [config]
  (component/system-map ; TODO logging
    :server     (server/map->Server (:server config))
    :websockets (component/using 
                  (ws/map->ChannelSocket (:websockets config))
                  [:server])))

(defonce system (atom nil))

(defn -main []
  (swap! system
    (fn [s]
      (when s
        (log/debug "System stopping...")
        (component/stop s)
        (log/debug "System stopped."))))
  (log/debug "System starting...")
  (reset! system (-> config make-system-map component/start))
  (log/debug "System started."))

(when (-> env :profile (= "dev")) (-main))