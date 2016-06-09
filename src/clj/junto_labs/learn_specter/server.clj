(ns junto-labs.learn-specter.server
  (:require
    [taoensso.timbre                         :as log      ]
    [com.stuartsierra.component              :as component]
    [immutant.web                            :as imm      ]
    [org.httpkit.server                      :as kit      ]))

(defrecord
  ^{:doc "A web server. Currently only the :immutant server @type is supported."}
  Server
  [routes-var make-routes-fn server type host port ssl-port stop-fn ran
   root-path ws-uri]
  component/Lifecycle
    (start [this]
      (let [stop-fn-f (atom (fn []))]
        (try
          (assert (contains? #{:immutant :http-kit} type)) ; TODO use clojure.spec
          (assert (var? routes-var))

          (let [opts {:host     (or host     "localhost")
                      :port     (or port     80)}
                _ (when make-routes-fn ; sets up routes
                    (alter-var-root routes-var
                      (constantly (make-routes-fn (merge this opts)))))
                _ (log/debug "Launching server with options:" (assoc opts :type type))
                server (condp = type
                         :immutant (imm/run        routes-var opts)
                         :http-kit (kit/run-server routes-var opts))
                _ (reset! stop-fn-f
                    (condp = type
                      :immutant #(when server (imm/stop server))
                      :http-kit #(when server (server))))]
            (log/debug "Server launched.")
            (assoc this
              :ran     server
              :server  (condp = type
                         :immutant (imm/server server)
                         :http-kit nil) ; Doesn't expose this
              :port    port
              :stop-fn @stop-fn-f))
        (catch Throwable e
          (log/warn e)
          (@stop-fn-f)
          (throw e)))))
    (stop [this]
      (try
        (stop-fn)
        (catch Throwable e
          (log/warn e)))
      (assoc this
        :stop-fn nil)))