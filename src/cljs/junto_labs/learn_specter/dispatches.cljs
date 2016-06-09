(ns junto-labs.learn-specter.dispatches
  (:require
    [re-frame.core      :as re
      :refer [dispatch]]
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