(ns junto-labs.learn-specter.subs
  (:require [re-frame.core      :as re
              :refer [subscribe]]))

(re/register-sub :db
  (fn [db _] db))