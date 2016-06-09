(ns junto-labs.learn-specter.events)

(def ev #(-> % .-target .-value))

(def num->key
  {8  :backspace
   32 :space
   13 :enter
   38 :up
   40 :down
   37 :left
   39 :right})

(defn e->key
  "Gets the key from a key event."
  {:possible-output `{:key :enter, :modifiers #{:alt}}}
  [e]
  (let [n (or (.-keyCode e) (.-which e))]
    {:key (get num->key n n)
     :modifiers (disj (hash-set (when (.-altKey e) :alt)) nil)}))