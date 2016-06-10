(ns junto-labs.learn-specter.handlers
  (:require [taoensso.timbre                :as log]
            [junto-labs.learn-specter.utils :as u  ]))

(defmulti event-msg-handler :id) ; Dispatch on event-id

; Wrap for logging, catching, etc.:
(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default ; Fallback
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (log/warn "Unhandled event:" ev-msg "from" uid)
    
    (when ?reply-fn
      (log/warn "Responding to callback")
      (?reply-fn {:unhandled-event event}))))

(defmethod event-msg-handler :chsk/uidport-open
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (log/debug "uidport-open"))

(defmethod event-msg-handler :chsk/uidport-close
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (log/debug "uidport-close"))

(defmethod event-msg-handler :chsk/ws-ping
  ; Do nothing
  [ev-msg])

(defmethod event-msg-handler :str/eval ; TODO unsafe
  [{:as ev-msg :keys [?data ?reply-fn]}]
  (when ?reply-fn
    (log/debug "Evaling" ?data "...")
    (let [evaled (try [true (-> ?data read-string eval u/ppr-str)]
                   (catch Throwable t [false (u/ppr-str t)]))]
      (?reply-fn evaled))))