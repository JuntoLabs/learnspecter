(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.events :as ev]))

(set! (-> js/document .-body .-onkeydown)
  #(dispatch [:key-down (ev/capture %)]))

(defn editable-component [id v-0]
  (let [editable (subscribe [:dom id])
        focused? (subscribe [:focused? id])
        _ (dispatch [:dom id v-0])] ; Set initial value
    (fn []
      [:div
        {:id id
         :on-click #(dispatch [:focus id])}
        @editable
        [:div (str "- Focused? " @focused?)]])))

(defn root []
  [:div
    [:div "Hello!"]
    [editable-component :div1 "Editable1"]
    [editable-component :div2 "Editable2"]
    [:div [:div "Database is this:"]
          [:div (pr-str @(subscribe [:db]))]]])