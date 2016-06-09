(ns junto-labs.learn-specter.subs
  (:require [re-frame.core      :as re
              :refer [subscribe]])
  (:require-macros
    [reagent.ratom
      :refer [reaction]]))

(re/register-sub :db
  (fn [db _] (reaction @db)))

(re/register-sub :focused
  (fn [db _] (reaction (get @db :focused))))

(re/register-sub :dom
  (fn [db [_ id]]
    (reaction (get-in @db [:dom id]))))