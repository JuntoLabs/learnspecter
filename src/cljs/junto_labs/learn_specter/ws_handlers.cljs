(ns junto-labs.learn-specter.ws-handlers
  (:require [taoensso.timbre :as log]))

(defmulti event-msg-handler :id) ; Dispatch on event-id

; Wrap for logging, catching, etc.:
(defn event-msg-handler* [ev-msg]
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (log/warn "Unhandled event:" ev-msg "from" uid)
    
    (when ?reply-fn
      (?reply-fn {:unhandled-event event}))))

(defmethod event-msg-handler :chsk/handshake
  [_]
  (log/debug "Websocket handshake completed."))

(defmethod event-msg-handler :chsk/state
  [e]
  (if (:first-open? e)
      (log/debug "Socket first open.")
      (log/debug "State change in socket:" e)))



; (defmulti push-msg-handler (fn [[id _]] id)) ; Dispatch on event key which is 1st elem in vector

; (defmethod push-msg-handler :rente/testevent
;   [[_ event]]
;   (js/console.log "PUSHed :rente/testevent from server: %s " (pr-str event)))

; (defmulti event-msg-handler :id) ; Dispatch on event-id
; ;; Wrap for logging, catching, etc.:

; (defmethod event-msg-handler :default ; Fallback
;     [{:as ev-msg :keys [event]}]
;     (js/console.log "Unhandled event: %s" (pr-str event)))

; (defmethod event-msg-handler :chsk/state
;   [{:as ev-msg :keys [?data]}]
;   (if (= ?data {:first-open? true})
;     (js/console.log "Channel socket successfully established!")
;     (js/console.log "Channel socket state change: %s" (pr-str ?data))))

; (defmethod event-msg-handler :chsk/recv
;   [{:as ev-msg :keys [?data]}]
;   (push-msg-handler ?data))

; (defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
;   (event-msg-handler ev-msg))

; (defn test-socket-callback []
;   (chsk-send!
;     [:rente/testevent {:message "Hello socket Callback!"}]
;     2000
;     #(js/console.log "CALLBACK from server: " (pr-str %))))

; (defn test-socket-event []
;   (chsk-send! [:rente/testevent {:message "Hello socket Event!"}]))
