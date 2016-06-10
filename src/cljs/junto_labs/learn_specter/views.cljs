(ns junto-labs.learn-specter.views
  (:require [re-frame.core :as re
              :refer [subscribe dispatch]]
            [reagent.core  :as rx]
            [junto-labs.learn-specter.utils  :as u ]
            [junto-labs.learn-specter.events :as ev]
            [junto-labs.learn-specter.components :as comp])
  (:require-macros
    [taoensso.timbre    :as log]))

(defn to-eval-editable []
  (let [id       :repl-line
        value    (subscribe [:dom id])
        focused? (subscribe [:focused? id])
        history-index (subscribe [:history-index])
        history-item  (subscribe [:history-item ])]
    (fn []
      (log/debug "history-index" @history-index)
      (let [[history-value _ _] @history-item]
        (log/debug "history-value"  history-value)
        (log/debug "value" "for" id @value)
        (log/debug "What should be looking" (if history-index
              history-value
              @value))
        [:pre.to-eval
          {:id       id
           :style    {:min-height 14}
           :class    (if @focused?
                         "focused editable"
                         "editable")
           :on-click #(dispatch [:focus id])}
          (or history-value @value)]))))

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
  #_[:div (u/ppr-str @(subscribe [:db]))]
  (comp/pr-table @(subscribe [:db])))

(defn caret []
  [:div.vbox.vcenter.caret [:pre {:style {:padding-top 2}} ">"]])

(defn repl []
  (let [evaled-results (subscribe [:evaled])]
    (fn []
      (dispatch [:scroll-to-bottom-of-page])    
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
             [to-eval-editable]
             [evaled (-> @evaled-results count)]]])])))

(defn root []
  (dispatch [:focus :repl-line])
  [:div#inner-root.vbox.hstretch
    [:div.hbox.hcenter [:h1.title "Welcome to the REPL!"]]
    [repl]
  #_[:div.vbox
      [:h2 "Database"] ; TODO hide button
      [pr-database]]])  