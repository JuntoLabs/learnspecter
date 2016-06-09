(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]))

(defn root []
  [:div
      [:div "Hello!"]
      [:div [:div "Database is this:"]
            [:div (pr-str @(subscribe [:db]))]]])

