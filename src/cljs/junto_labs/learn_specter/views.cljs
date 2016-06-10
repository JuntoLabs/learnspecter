(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.utils  :as u ]
            [junto-labs.learn-specter.events :as ev]
            [junto-labs.learn-specter.components :as comp]))

(defn editable-component [tag id & [style v-0]]
  (let [value    (subscribe [:dom id])
        focused? (subscribe [:focused? id])
        _ (dispatch [:dom id v-0])] ; Set initial value
    (fn []
      [tag
        {:id id
         :style style
         :class (if @focused?
                    "focused editable"
                    "editable")
         :on-click #(dispatch [:focus id])}
        @value])))

(defn to-eval [i]
  (let [value (subscribe [:evaled i])]
    (fn []
      (let [[to-eval-str _ _] @value]
        [:pre.to-eval to-eval-str]))))

(defn evaled [i]
  (let [value (subscribe [:evaled i])]
    (fn []
      (let [[_ success? evaled-str] @value]
        [:pre.evaled {:class (if (false? success?) "evaled failure" "evaled")}
          evaled-str]))))

(defn pr-database []
  [:div (u/ppr-str @(subscribe [:db]))]
  #_(comp/pr-table @(subscribe [:db])))



(defn caret []
  [:div.vbox.vcenter.caret [:pre {:style {:padding-top 2}} ">"]])

(defn repl []
  (let [evaled-results (subscribe [:evaled])]
    (fn []
      [:div#container.hbox
        (u/into*
          [:div#repl.vbox {:style {:flex-grow 0 :overflow-x :hidden :min-width "100%"}}]
          (for [i (range 0 (count @evaled-results))]
            [:div.hbox.repl-line
              [caret]
              [to-eval i]
              [evaled i]])
          [[:div.hbox.repl-line
             [:div.vbox.vcenter.caret [:pre {:style {:padding-top 6}} ">"]]
             [editable-component :pre.to-eval :repl-line {:min-height 14}]
             [evaled (-> @evaled-results count)]]])])))

(defn root []
  (dispatch [:focus :repl-line])
  [:div#inner-root.vbox.hstretch
    [:div.hbox.hcenter [:h1.title "Welcome to the REPL!"]]
    [repl]
 #_[:div.vbox
      [:h2 "Database"] ; TODO hide button
      [pr-database]]])  