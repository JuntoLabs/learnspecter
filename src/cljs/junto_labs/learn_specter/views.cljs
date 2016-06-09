(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.events :as ev]
            [junto-labs.learn-specter.components :as comp]))

(defn editable-component [id v-0]
  (let [editable (subscribe [:dom id])
        focused? (subscribe [:focused? id])
        _ (dispatch [:dom id v-0])] ; Set initial value
    (fn []
      [:div
        {:id id
         :on-click #(dispatch [:focus id])}
        @editable])))

(defn pr-database []
  (comp/pr-table @(subscribe [:db])))

(defn root []
  [:div#inner-root.vbox
    [:h1 "Welcome to the REPL!"]
    [:div#container.hbox
      [:div#repl.vbox
        [:h2 "REPL"]
        [editable-component :div1 "Editable1"]]
      [:div#evaled.vbox
        [:h2 "Evaled"]
        [editable-component :div2 "Editable2"]]]
    [:div [:h2 "Database"]
          [pr-database]]])