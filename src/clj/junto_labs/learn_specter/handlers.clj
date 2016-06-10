(ns junto-labs.learn-specter.handlers
  (:require [taoensso.timbre                :as log]
            [junto-labs.learn-specter.utils :as u  ]
            [com.rpl.specter                :as s  :refer :all]
            [com.rpl.specter.macros         :as sm :refer :all]))

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

(defn str-eval [s]
  (try [nil (-> s read-string eval u/ppr-str)]
    (catch Throwable t [false (u/ppr-str t)])))

(defmethod event-msg-handler :str/eval ; TODO unsafe
  [{:as ev-msg :keys [?data ?reply-fn]}]
  (when ?reply-fn
    (log/debug "Evaling" ?data "...")
    (?reply-fn (str-eval ?data))))

; TODO abstract
(defmethod event-msg-handler :challenge/specter-1 ; TODO unsafe
  ^{:doc "Use `setval`"}
  [{:as ev-msg :keys [?data ?reply-fn]}]
  (when ?reply-fn
    (log/debug "Evaling challenge 'Specter 1' on" ?data "...")
    (?reply-fn
      (try
        (let [form   (read-string ?data)
              in     {:ab [2 3 4]}
              out    {:ab [2 3 4 5 6 7 8]}
              evaled (eval form)]
          (if (fn? evaled)
              (let [result (evaled in)]
                (if (and (doto (first (u/prewalk-find #(= % 'setval) form)) (println "is find"))
                         (doto (= result out) (println "is eq")))
                    [true (u/ppr-str result)]
                    [nil  (u/ppr-str result)]))
              [nil  (u/ppr-str evaled)]))
        (catch Throwable t [false (u/ppr-str t)])))))

