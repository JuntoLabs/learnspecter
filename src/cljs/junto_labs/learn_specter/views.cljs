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
        [:pre.evaled {:class (cond (false? success?) "evaled failure"
                                   (true?  success?) "evaled success"
                                   :else             "evaled")}
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

(defn separator [n]
  (fn [] [:div {:style {:margin-bottom n}}]))

(defn challenge-complete []
  (fn []
    [:div.vbox.hcenter
     [:h1 "Challenge complete!"]]))

(defn challenge-ongoing []
  (fn []
    [:div.vbox.hcenter
      [:h2 [:i "Here is your challenge, should you choose to accept it:"]]
      [:div.hbox.subtitle
        [:div "Submit a function which does the following using Specter's"]
        [:div {:style {:width 3}}]
        [:pre "setval"]
        [:div ":"]]
      [:div.vbox [:pre "in  : {:ab [2 3 4]}"]
                 [:pre "out : {:ab [2 3 4 5 6 7 8]}"]]
      [separator 10]
      [:div.vbox.hcenter
        [:div.subtitle "Example:"]
        [:pre "(fn [in] (do-things ... (setval ...) in ... ))"]]
      [separator 10]]))

(defn challenge []
  (let [success? (subscribe [:success?])]
    (fn []
      (if @success?
          [challenge-complete]
          [challenge-ongoing]))))

(defn correct []
  (fn []
    ))

(defn root []
  (dispatch [:focus :repl-line])
  [:div#inner-root.vbox.hstretch
    [:div.hbox.hcenter [:h1.title "Welcome to the REPL!"]]
    [repl]
    [challenge]
  #_[:div.vbox
      [:h2 "Database"] ; TODO hide button
      [pr-database]]])  