(ns junto-labs.learn-specter.styles)

(def styles
  (let [horient (fn [k v] (condp = k
                            :h {:justify-content v}
                            :v {:align-items     v}))
        vorient (fn [k v] (condp = k
                            :h {:align-items     v}
                            :v {:justify-content v}))
        vbox-base {:display         :flex
                   :flex-direction  :column}
        vbox (merge vbox-base
               #_(vorient :h :center))
        hbox-base {:display         :flex
                   :flex-direction  :row}
        hbox (merge hbox-base
               #_(vorient :v :center))]
    [[:* {:font-family "Helvetica Neue"}]
     [:.vbox         vbox]
     [:.hbox         hbox]
     [:.hbox.hcenter (merge hbox (horient :h :center))]
     [:.hbox.vcenter (merge hbox (vorient :h :center))]
     [:.node         {:background-color :gray}]
     [:.node-label   {:width 35}]]))

(defn replace-css-at! [id css-str]
  "Replaces CSS at a style node."
  (let [elem (or (.getElementById js/document id) ; was failing until this
                 (doto (.createElement js/document "style")
                       (-> .-id (set! id))
                       (->> (.appendChild (.-head js/document)))))
        _ (assert (-> elem .-tagName (= "STYLE")))
        text (.createTextNode js/document css-str)]
    (while (.-firstChild elem)
      (.removeChild elem (.-firstChild elem)))

    (.appendChild elem text)
    elem))