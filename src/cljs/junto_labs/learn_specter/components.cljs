(ns junto-labs.learn-specter.components
  (:require [junto-labs.learn-specter.utils  :as u ]))

(defn pr-table [x]
  (cond (map?    x) (into [:table]
                      (for [[k v] x]
                        [:tr [:td (pr-table k)] [:td (pr-table v)]]))
        (vector? x) (into [:table]
                      (for [v x]
                        [:tr [:td (pr-table v)]]))
        :else       (u/ppr-str x)))