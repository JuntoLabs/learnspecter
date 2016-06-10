(ns junto-labs.learn-specter.dispatches
  (:require
    [junto-labs.learn-specter.utils  :as u ]
    [junto-labs.learn-specter.events :as ev]
    [junto-labs.learn-specter.subs   :as s ]
    [re-frame.core      :as re
      :refer [dispatch subscribe]]
    [taoensso.timbre])
  (:require-macros
    [taoensso.timbre    :as log]))

(set! (-> js/document .-body .-onkeydown) ; Prevent backspace or navigation via arrow keys
  #(dispatch [:key-down (ev/capture %
                          (fn [e] (let [k (-> e :keys :key)]
                                    (or (= k :backspace)
                                        (= k :delete   )
                                        (= k :up       )
                                        (= k :down     )
                                        (= k :space    )))))]))

(re/register-handler :reset
  (fn [db [_ v]]
    (log/debug "Resetting state to" v)
    v))

(re/register-handler :set-ws-fn
  (fn [db [_ f]]
    (assoc db :ws-fn f)))

(re/register-handler :dom
  (fn [db [_ id v]]
    (assoc-in db [:dom id] v)))

(re/register-handler :add-evaled
  (fn [db [_ to-eval-str [success? evaled-str]]]
    (update db :evaled conj [to-eval-str success? evaled-str])))

(defn eval-on-server! [db x]
  ((get db :ws-fn) [:str/eval x] 5000
    (fn [evaled]
      (log/debug "Evaled on server!" evaled)
      (dispatch [:add-evaled x evaled]))))

(re/register-handler :eval
  (fn [db [_ s]]
    (if (nil? s)
        (assoc-in db [:dom :evaled] "")
        (do (eval-on-server! db s)
            (assoc-in db [:dom :evaled] "...")))
    ))

(defn doto-history [db action]
  (log/debug "Doto history" action)
  (let [history (s/evaled db)
        latest-i  (max 0 (-> history count dec))
        update-fn (condp = action
                    :decrement   (fn [i] (cond
                                           (zero? i) 0
                                           (nil?  i) latest-i
                                           :else     (dec i)))
                    :increment   (fn [i] (cond
                                           (nil? i) nil
                                           (>= i (-> history count dec)) nil
                                           :else    (inc i)))
                    :clear-index (constantly nil))]
    (update db :history-index update-fn)))

(defn update-focused [db f]
  (update-in db [:dom (s/focused db)] f))

(re/register-handler :scroll-to-bottom-of-page
  (fn [db _]
    (js/window.scrollTo 0 (-> js/document .-body .-scrollHeight))
    db))

(def dispatch-map ; if it returns true, it continues with default
  {:repl-line (fn [db e] (condp = (-> e :keys :key)
                           :enter (do (dispatch [:eval (or (s/dom db :repl-line)
                                                           (s/history-value db))])
                                      (-> db (doto-history :clear-index)
                                             (update-focused (constantly nil))))
                           :up    (doto-history db :decrement)
                           :down  (doto-history db :increment)
                           true))})

; ===== USER INTERACTION EVENTS =====

(defn key-down-default
  [db e focused]
  (let [k                   (-> e :keys :key)
        history-index       (s/history-index db)
        [history-value _ _] (s/history-item  db)
        update-fn  (if (or (= k :backspace)
                           (= k :delete   ))
                       #(u/pop-str %)
                       #(str % (-> e :keys :key-str)))  ; append
        update-fn' (if history-index
                       (constantly (update-fn history-value))
                       update-fn)
        update-history-index
          (if history-index #(doto-history % :clear-index) identity)]
    (-> db (update-focused update-fn') update-history-index)))

(re/register-handler :key-down
  (fn [db [_ e]]
    (log/debug "Key down!" (:keys e))
    (let [focused (s/focused db)]
      (if-let [handler (get dispatch-map focused)]
        (let [handled (handler db e)]
          (if (true? handled)
              (key-down-default db e focused)
              handled))
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