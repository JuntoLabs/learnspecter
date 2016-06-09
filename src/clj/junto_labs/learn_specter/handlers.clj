(ns junto-labs.learn-specter.handlers)

(defmulti event-msg-handler :id) ; Dispatch on event-id
(def send-msg! (lens res/systems (fn-> :global :sys-map deref* :connection :send-fn)))

; Wrap for logging, catching, etc.:
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))

#?(:clj
(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event:" ev-msg "from" uid)
    (println "Responding" "reply-fn?" ?reply-fn)
  
    (when ?reply-fn
      (?reply-fn {:unhandled-event event})))))

#?(:clj
(defmethod event-msg-handler :chsk/uidport-open
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (log/pr :debug "uidport-open")))

#?(:clj
(defmethod event-msg-handler :chsk/uidport-close
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (log/pr :debug "uidport-close")))

(defmethod event-msg-handler :chsk/ws-ping
  ; Do nothing
  [ev-msg])