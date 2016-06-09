(ns junto-labs.learn-specter.system
  (:require
    [com.stuartsierra.component           :as component]
    [junto-labs.learn-specter.ws-handlers :as handlers ]
    [junto-labs.learn-specter.websockets  :as ws       ]
    [junto-labs.learn-specter.utils       :as u        ]
    [junto-labs.learn-specter.views       :as views    ]
    [junto-labs.learn-specter.state       :as state    ]
    [junto-labs.learn-specter.subs                     ]
    [junto-labs.learn-specter.dispatches               ]
    [junto-labs.learn-specter.styles      :as styles
      :refer [styles]                                  ]
    [reagent.core                         :as rx       ]
    [re-frame.core                        :as re
      :refer [dispatch]                                ]
    [garden.core
      :refer [css]                                     ])
  (:require-macros
    [taoensso.timbre :as log]))

(def config
  {:websockets {:endpoint    "/chan"
                :host        "localhost"
                :port        8080
                :msg-handler handlers/event-msg-handler*}})

(defonce system-map
  (component/system-map ; TODO logging
    :websockets (ws/map->ChannelSocket (:websockets config))))
(enable-console-print!)

(defonce system (atom system-map))

(defn render! []
  (styles/replace-css-at! "dynamic" (css styles))
  (let [dom-root (or (.getElementById js/document "root") ; was failing until this
                     (doto (.createElement js/document "div")
                           (-> .-id (set! "root"))
                           (->> (.appendChild (.-body js/document)))))]
    (rx/render [views/root] dom-root)))

(defn -main []
  (log/debug "System stopping if running...")
  (swap! system component/stop )
  (swap! system component/start)
  (log/debug "System started.")
  (render!)
  (log/debug "View rendered."))

(defn tests [system]
  (dispatch [:test-ws-connectivity (-> system :websockets :send-fn)]))

(-main)
(tests @system)
