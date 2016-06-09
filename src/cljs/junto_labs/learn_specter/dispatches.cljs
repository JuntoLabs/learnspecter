(ns junto-labs.learn-specter.dispatches
  (:require
    [re-frame.core      :as re
      :refer [dispatch subscribe]]
    [taoensso.timbre])
  (:require-macros
    [taoensso.timbre    :as log]))

(re/register-handler :reset
  (fn [db [_ v]]
    (log/debug "Resetting state to" v)
    v))

(re/register-handler :receive-message
  (fn [db [_ e]]
    (update db :messages conj e)))

(re/register-handler :test-ws-connectivity
  (fn [db [_ send-fn]]
    (send-fn [:event/name "Message"] 200 
      (fn [e] (dispatch [:receive-message e])))
    db))

(re/register-handler :key-down
  (fn [db [_ e]]
    (log/debug "Key down!" (:keys e))
    (update-in db [:dom @(subscribe [:focused])] #(str % (-> e :keys :key-str)))))

(re/register-handler :click
  (fn [db [_ e]]
    (log/debug "Click!" e)
    db))

(re/register-handler :focus
  (fn [db [_ id]]
    (log/debug "Focusing" id)
    (assoc db :focused id)))

(re/register-handler :dom
  (fn [db [_ id v]]
    (assoc-in db [:dom id] v)))