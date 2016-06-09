(ns junto-labs.learn-specter.dispatches
  (:require
    [junto-labs.learn-specter.utils  :as u ]
    [junto-labs.learn-specter.events :as ev]
    [re-frame.core      :as re
      :refer [dispatch subscribe]]
    [taoensso.timbre])
  (:require-macros
    [taoensso.timbre    :as log]))

(set! (-> js/document .-body .-onkeydown) ; Prevent backspace
  #(dispatch [:key-down (ev/capture %
                          (fn [e] (let [k (-> e :keys :key)]
                                    (or (= k :backspace)
                                        (= k :delete   )))))]))

(re/register-handler :reset
  (fn [db [_ v]]
    (log/debug "Resetting state to" v)
    v))

(re/register-handler :receive-message
  (fn [db [_ e]]
    (update db :messages conj e)))

(re/register-handler :test-ws-connectivity
  (fn [db _]
    ((get db :ws-fn) [:event/name "Message"] 200 
      (fn [e] (dispatch [:receive-message e])))
    db))

(re/register-handler :set-ws-fn
  (fn [db [_ f]]
    (assoc db :ws-fn f)))

(re/register-handler :dom
  (fn [db [_ id v]]
    (assoc-in db [:dom id] v)))

(defn eval-on-server! [db x to-id]
  ((get db :ws-fn) [:str/eval x] 5000
    (fn [evaled]
      (log/debug "Evaled on server!" evaled)
      (dispatch [:dom to-id evaled]))))

(re/register-handler :eval
  (fn [db [_ from-id to-id]]
    (eval-on-server! db
      @(subscribe [:dom from-id])
      to-id)
    (assoc-in db [:dom to-id] "Evaling...")))

(def dispatch-map ; if it returns truthy, it continues with default
  {:repl.line (fn [e] (if (-> e :keys :key (= :enter))
                          (do (log/debug "Enter pressed!")
                              (dispatch [:eval :repl.line :evaled])
                              (dispatch [:add-line]))
                          true))})

; ===== USER INTERACTION EVENTS =====

(defn key-down-default ; is to append
  [db e focused]
  (let [k         (-> e :keys :key)
        update-fn (if (or (= k :backspace)
                          (= k :delete   ))
                      #(u/pop-str %)
                      #(str % (-> e :keys :key-str)))]
    (update-in db [:dom focused] update-fn)))

(re/register-handler :key-down
  (fn [db [_ e]]
    (log/debug "Key down!" (:keys e))
    (let [focused @(subscribe [:focused])]
      (if-let [handler (get dispatch-map focused)]
        (if (handler e)
            (key-down-default db e focused)
            db)
        (do (log/debug "No handler found for" focused)
            (key-down-default db e focused))))))

(re/register-handler :click
  (fn [db [_ e]]
    (log/debug "Click!" e)
    db))

(re/register-handler :focus
  (fn [db [_ id]]
    (log/debug "Focusing" id)
    (assoc db :focused id)))