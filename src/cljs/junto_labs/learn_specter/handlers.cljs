(ns junto-labs.learn-specter.handlers
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

