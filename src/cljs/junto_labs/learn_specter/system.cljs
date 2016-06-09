(ns junto-labs.learn-specter.system
  (:require
    [com.stuartsierra.component          :as component]
    [junto-labs.learn-specter.handlers   :as handlers ]
    [junto-labs.learn-specter.websockets :as ws       ]
    [junto-labs.learn-specter.utils      :as u        ])
  (:require-macros
    [taoensso.timbre :as log]))

(def config
  {:websockets {:endpoint    "/chan"
                :host        "0.0.0.0"
                :port        32000
                :msg-handler handlers/event-msg-handler*}})

(defonce system-map
  (component/system-map ; TODO logging
    :websockets (ws/map->ChannelSocket (:websockets config))))
(enable-console-print!)

(defonce system (atom system-map))

(defn -main []
  (log/debug "System stopping if running...")
  (swap! system component/stop )`
  (swap! system component/start)
  (log/debug "System started."))

(-main)