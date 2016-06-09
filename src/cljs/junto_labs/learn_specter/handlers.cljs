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
    (log/warn "Responding" "reply-fn?" ?reply-fn)
  
    (when ?reply-fn
      (?reply-fn {:unhandled-event event}))))