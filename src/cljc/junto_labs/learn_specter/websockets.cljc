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
          @packer     : Client<->server serialization format"}
  ChannelSocket
  [endpoint host port chan chan-recv send-fn chan-state type packer
   stop-fn post-fn get-fn msg-handler
   connected-uids
   #?@(:clj [server make-routes-fn])]
  component/Lifecycle
    (start [this]
      (let [stop-fn-f (atom (fn []))]
        (try
          (log/debug "Starting channel-socket with:" this)
           ; TODO for all these assertions, use clojure.spec!
          (assert (string? endpoint) #{endpoint})
          (assert (fn? msg-handler))
  #?(:clj (assert (fn? make-routes-fn)))
          (assert (or (nil? type) (contains? #{:auto :ajax :ws} type)))

          (let [packer (or packer :edn)
                {:keys [chsk ch-recv send-fn state] :as socket}
                 (ws/make-channel-socket!
                   #?(:clj  (condp = (:type server)
                              :immutant a-imm/sente-web-server-adapter)
                      :cljs endpoint)
                   {:type   (or type   :auto)
                    :packer packer
         #?@(:cljs [:host   (str host ":" port)])})
                _ (reset! stop-fn-f (ws/start-chsk-router! ch-recv msg-handler))
                this' (assoc this
                        :chan           chsk
                        :chan-recv      ch-recv
                        :send-fn        send-fn
                        :chan-state     state
                        :packer         packer
                        :stop-fn        @stop-fn-f
                        :post-fn        (:ajax-post-fn                socket)
                        :get-fn         (:ajax-get-or-ws-handshake-fn socket)
                        :connected-uids (:connected-uids              socket))]
            #?(:clj (alter-var-root (:routes-var server)
                      (constantly (make-routes-fn (merge this' server)))))
            (log/debug "Channel-socket started.")
            this')
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