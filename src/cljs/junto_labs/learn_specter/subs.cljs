(ns junto-labs.learn-specter.subs
  (:require [re-frame.core      :as re
              :refer [subscribe]])
  (:require-macros
    [reagent.ratom
      :refer [reaction]]))

(re/register-sub :db
  (fn [db _] (reaction @db)))

(defn focused [db] (get db :focused))

(re/register-sub :focused
  (fn [db _] (reaction (focused @db))))

(re/register-sub :focused?
  (fn [db [_ id]] (reaction (= (get @db :focused) id))))

(defn dom [db id] (get-in db [:dom id]))

(re/register-sub :dom
  (fn [db [_ id]]
    (reaction (dom @db id))))

(defn evaled
  ([db] (evaled db nil))
  ([db i]
    (if (nil? i)
        (get    db :evaled)
        (get-in db [:evaled i]))))

(re/register-sub :evaled
  (fn [db [_ i]]
    (reaction (evaled @db i))))

(defn history-index [db] (:history-index db))

(re/register-sub :history-index
  (fn [db _]
    (reaction (history-index @db))))

(defn history-item [db]
  (get-in db [:evaled (history-index db)]))

(re/register-sub :history-item
  (fn [db _]
    (reaction (history-item @db))))

(defn history-value [db]
  (let [[v _ _] (history-item db)] v))