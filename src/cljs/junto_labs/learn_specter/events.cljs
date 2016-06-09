(ns junto-labs.learn-specter.events)

(def ev #(-> % .-target .-value))

(def num->key
  {8  :backspace
   16 :shift
   18 :alt
   91 :meta
   46 :delete
   32 :space
   13 :enter
   38 :up
   40 :down
   37 :left
   39 :right})

(def non-printable?
  #{:shift :meta :alt :up :down :left :right})

(defn e->key
  "Gets the key from a key event."
  {:possible-output `{:key :enter, :modifiers #{:alt}}}
  [e]
  (let [n (or (.-keyCode e) (.-which e))
        k (get num->key n n)]
    {:key     k
     :key-str (when-not (non-printable? k)
                (condp = k
                  :enter \newline
                  (.-key e)))
     :modifiers (disj (hash-set (when (.-altKey e) :alt)) nil)}))

(defn capture
  "Events are garbage-collected when they're passed outside of an event handler.
   This gets around that."
  ([e] (capture e (constantly false)))
  ([e prevent-default-pred] ; TODO make this a Record for purposes of speed 
    (let [captured {:keys  (e->key e)
                    :mouse {:button   (.-button  e)
                            :buttons  (.-buttons e)
                            :client   {:x (.-clientX   e)
                                       :y (.-clientY   e)}
                            :movement {:x (.-movementX e)
                                       :y (.-movementY e)}
                            :offset   {:x (.-offsetX   e)
                                       :y (.-offsetY   e)}
                            :page     {:x (.-pageX     e)
                                       :y (.-pageY     e)}
                            :screen   {:x (.-screenX   e)
                                       :y (.-screenY   e)}}}]
    (when (prevent-default-pred captured) (.preventDefault e))
    captured)))