(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.events :as ev]))

(set! (-> js/document .-body .-onkeydown)
  #(dispatch [:key-down (ev/capture %)]))

(defn editable-component []
  (let [editable (subscribe [:dom :editable])
        _ (dispatch [:dom :editable "Editable"])]
    (fn []
      [:div#editable
        {:on-click #(dispatch [:focus :editable])}
        @editable])))

(defn root []
  [:div
    [:div "Hello!"]
    [editable-component]
    [:div [:div "Database is this:"]
          [:div (pr-str @(subscribe [:db]))]]])