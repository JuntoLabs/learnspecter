(ns junto-labs.learn-specter.websockets
  (:require
    [taoensso.timbre                         :as log      ]
    [com.stuartsierra.component              :as component]
    [immutant.web                            :as imm      ]))

#?(:clj
(defrecord
  ^{:doc "A web server. Currently only the :immutant server @type is supported."}
  Server
  [routes server type host port ssl-port stop-fn ran]
  component/Lifecycle
    (start [this]
      (let [stop-fn-f (atom (fn []))]
        (try
          (assert (contains? #{:immutant} type)) ; TODO use clojure.spec

          (let [opts {:host     (or host     "localhost")
                      :port     (or port     80)
                      :ssl-port (or ssl-port 443)}
                _ (log/debug "Launching server with options:" (assoc opts :type type))
                server (condp = type
                         :immutant (imm/run routes opts))
                _ (reset! stop-fn-f
                    (condp = type
                      :immutant #(when server
                                   (imm/stop server))))]
            (log/debug "Server launched.")
            (assoc this
              :ran     server
              :server  (condp = type
                         :immutant (imm/server server))
              :port    port
              :stop-fn @stop-fn-f))
        (catch Throwable e
          (log/warn e)
          (@stop-fn-f)
          (throw e)))))
    (stop [this]
      (try
        (condp = type
          :immutant (stop-fn))
        (catch Throwable e
          (log/warn e)))
      (assoc this
        :stop-fn nil))))