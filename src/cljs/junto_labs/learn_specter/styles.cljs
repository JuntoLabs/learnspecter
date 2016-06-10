(ns junto-labs.learn-specter.styles)

(def reset-defaults
  [:html :body :span :div
   :applet :object :iframe
   :h1 :h2 :h3 :h4 :h5 :h6
   :b :i :strong :strike
   :ol :ul :li
   :blockquote :pre :abbr :acronym
   :address
   :cite :code :del :dfn :em :img 
   :ins :kbd :q :samp :small
   :sub :sup :var :dl :dt :dd :tt
   :fieldset :form :label :big
   :legend :table :caption 
   :tbody :tfoot :thead :tr :th :td
   :a :p :font :s
   :article :aside :figure :footer :header
   :hgroup :menu :nav :section :time :mark
   :audio :video
    {:margin         0
     :padding        0
     :border         0
     :outline        0
     :font-weight    :inherit
     :font-style     :inherit
     :font-family    :inherit
     :font-size      "100%"
     :vertical-align :baseline
     :background     :transparent}] )

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
               #_(vorient :v :center))
        box-stretch-child
          {:flex-grow   1
           :flex-shrink 1
           :flex-basis  :auto
           :display     :flex}
        repl-box (fn [percent]
                   {:padding       "0px"
                    :padding-top   "2px"
                    :padding-left  "5px"
                    :padding-right "5px"
                    :min-width     (str "calc(" percent "% - 6px - 10px)")
                    :max-width     (str "calc(" percent "% - 6px - 10px)")
                    :width         (str "calc(" percent "% - 6px - 10px)")
                    :min-height    "20px"
                    :max-height    "250px"
                    :overflow-x    :scroll
                    :overflow-y    :scroll})]
    [reset-defaults
     [:html :body     {:height  "100%"
                       :width   "100%"
                       :-webkit-font-smoothing  :antialiased
                       :-moz-osx-font-smoothing :grayscale}]  ; disables subpixel rendering
     [:div :h1        {:font-family "Helvetica Neue"}]
     [:h1             {:font-size   "30px"
                       :font-weight :bold}]
     [:h1.title       {:padding-top   "10px" 
                       :padding-bottom "10px"}]
     [:pre            {:font-family "Courier"
                       :font-size   "12px"}]
     [:.vbox           vbox]
     [:.vbox.hcenter   (merge vbox (horient :v :center ))]
     [:.vbox.vcenter   (merge hbox (vorient :v :center ))]
     [".vstretch > *"  box-stretch-child]
     [:.hbox           hbox]
     [:.hbox.hcenter   (merge hbox (horient :h :center ))]
     [:.hbox.vcenter   (merge hbox (vorient :h :center ))]
     [".hstretch > *"  box-stretch-child]
     [:.editable       {:padding "5px"}]
     [:.focused        {:border-style :solid
                        :border-color :gray
                        :border-width "1px"}]
     [:.to-eval        (repl-box 30)]
     [:.evaled         (merge (repl-box 70)
                         {:background-color "#e4e4e4"
                          :color            "#3E3E3E"})]
     [:.evaled.failure {:background-color "#F64744"
                        :color            :white}]
     [:.repl-line      {:padding-top  "2px"}]
     [:.caret          {:padding-left "5px"
                        :min-width    "15px"
                        :max-width    "15px"
                        :width        "15px" }]]))

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