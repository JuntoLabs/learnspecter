(ns junto-labs.learn-specter.utils
  (:require [clojure.string :as str]
   #?(:clj  [clojure.pprint :refer [pprint]]
      :cljs [cljs.pprint    :refer [pprint]])))

(defn join-once
  "Like /clojure.string/join/ but ensures no double separators."
  {:attribution "taoensso.encore"}
  [separator & coll]
  (reduce
    (fn [s1 s2]
      (let [s1 (str s1) s2 (str s2)]
        (if (str/ends-with? s1 separator)
            (if (str/starts-with? s2 separator)
                (str s1 (.substring s2 1))
                (str s1 s2))
            (if (str/starts-with? s2 separator)
                (str s1 s2)
                (if (or (= s1 "") (= s2 ""))
                    (str s1 s2)
                    (str s1 separator s2))))))
    nil
    coll))

(def separator #?(:clj  (str (java.io.File/separatorChar))
                  :cljs "/"))

(defn ->path [& xs]
  (apply join-once separator xs))

(defn pop-str [s]
  (when (string? s)
    (.substring ^String s 0 (-> s count dec))))

(defn ppr-str [x] (with-out-str (pprint x)))

(defn into*
  ([a b] (into a b))
  ([a b c & args] (reduce into a (apply list b c args))))