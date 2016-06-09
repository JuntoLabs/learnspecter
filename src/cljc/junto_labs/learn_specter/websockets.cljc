(ns junto-labs.learn-specter.websockets
           (:require
             [taoensso.sente                          :as ws       ]
             [taoensso.timbre                         :as log      ]
             [com.stuartsierra.component              :as component]
     #?(:clj [taoensso.sente.server-adapters.immutant :as a-imm    ]))
  #?(:cljs (:require-macros
             [taoensso.timbre :as log])))

(defrecord 
  ^{:doc "A WebSocket-channel abstraction of Sente's functionality.

          Creates a Sente WebSocket channel and Sente WebSocket channel
          message router.

          @chan-recv  : ChannelSocket's receive channel
          @chan-send! : ChannelSocket's send API fn
          @chan-state : Watchable, read-only atom
          @packer     : Client<->server serialization format"
    :usage '(map->ChannelSocket {:uri         "/chan"
                                 :packer      :edn
                                 :msg-handler my-msg-handler})}
  ChannelSocket
  [uri host chan chan-recv send-fn chan-state type server-type packer
   stop-fn ajax-post-fn ajax-get-or-ws-handshake-fn msg-handler
   connected-uids]
  component/Lifecycle
    (start [this]
      (let [stop-fn-f (atom (fn []))]
        (try
          (log/debug "Starting channel-socket with:" this)
           ; TODO for all these assertions, use clojure.spec!
          (assert (string? uri) #{uri})
          (assert (fn? msg-handler))
          (assert (or (nil? type) (contains? #{:auto :ajax :ws} type)))
        #?(:clj 
          (assert (contains? #{:immutant})))
          (assert (keyword? packer))

          (let [{:keys [chsk ch-recv send-fn state] :as socket}
                 (ws/make-channel-socket!
                   #?(:clj (condp = server-type
                             :immutant a-imm/sente-web-server-adapter)
                      :cljs uri)
                   {:type   (or type :auto)
                    :packer (or packer :edn)
                    #?@(:cljs
                    [:host host])})
                _ (reset! stop-fn-f (ws/start-chsk-router! ch-recv msg-handler))]
            (log/debug "Channel-socket started.")
            (assoc this
              :chan                        chsk
              :chan-recv                   ch-recv
              :send-fn                     send-fn
              :chan-state                  state
              :stop-fn                     @stop-fn-f
              :ajax-post-fn                (:ajax-post-fn                socket)
              :ajax-get-or-ws-handshake-fn (:ajax-get-or-ws-handshake-fn socket)
              :connected-uids              (:connected-uids              socket)))
          (catch #?(:clj Throwable :cljs js/Error) e
            (log/warn "Error in ChannelSocket:" e)
            (@stop-fn-f)
            (throw e)))))
    (stop [this]
      (try (when stop-fn (stop-fn))
        (catch #?(:clj Throwable :cljs js/Error) e
          (log/warn "Error in ChannelSocket:" e)))
      ; TODO should assoc other vals as nil?
      this))