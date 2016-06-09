(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.utils  :as u ]
            [junto-labs.learn-specter.events :as ev]
            [junto-labs.learn-specter.components :as comp]))

(defn editable-component [tag id v-0]
  (let [editable (subscribe [:dom id])
        focused? (subscribe [:focused? id])
        _ (dispatch [:dom id v-0])] ; Set initial value
    (fn []
      [tag
        {:id id
         :on-click #(dispatch [:focus id])}
        @editable])))

(defn pr-database []
  [:div (u/ppr-str @(subscribe [:db]))]
  #_(comp/pr-table @(subscribe [:db])))

(defn root []
  [:div#inner-root.vbox
    [:h1 "Welcome to the REPL!"]
    [:div#container.hbox
      [:div#repl.vbox
        [:table
          [:tr [:th] [:th [:h2 "REPL"]] [:th [:h2 "Evaled"]]]
          [:tr [:td [:pre ">"]]
               [:td [editable-component :pre :repl.line ""]]
               [:td [editable-component :pre :evaled    ""]]]]]]
   #_[:div [:div [:h2 "Database"]] ; TODO hide button
          [pr-database]]])