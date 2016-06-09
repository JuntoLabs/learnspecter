(ns junto-labs.learn-specter.state
    (:require-macros [cljs.core.async.macros :refer [go-loop]])
    (:require [re-frame.core                       :as re
                :refer [dispatch]                        ]
              [junto-labs.learn-specter.dispatches       ]))

(def state-0 {:messages []
              :dom      {}})

(defonce init-state! (dispatch [:reset state-0]))