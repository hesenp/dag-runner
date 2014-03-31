(ns dag-runner.core
  (:use [clojure.contrib.generic.functor :only [fmap]])
  (:require [clojure.set]))

(defn aggregate-arguments
  "this function would aggregate all the inputs from either the
  function itself or a hash-map of promises. "
  [args-list input-arg promise-arg]
  (merge (select-keys input-arg args-list)
         (fmap deref (select-keys promise-arg args-list))))

(defn update-results
  "this function would deliver the results specified in the input to
  the promise-arg set"
  [result-list promise-arg]
  (doall (map #(deliver (get promise-arg (key %)) (val %))
              result-list)))

(defn run-and-deliver-results
  "this function would grab information from its arguments, run/or
  wait to run, and deliver its result to the corresponding places."
  [function-info input-arg promise-arg]
  (let [result ((:function function-info)
                (aggregate-arguments (:input function-info)
                                     input-arg
                                     promise-arg))]
    (update-results result promise-arg)))

;; ok. what's left is to aggregate all things together and make the
;; whole thing happen.

(defmacro dag-runner
  "this function will execute functions in a Directed Acyclic Graph as
  specified by their argument and output lists. "
  [fname input-dag]
  `(defn ~fname [& {:as args#}]
     (let [required-args# (mapcat :input ~input-dag)
           generated-args# (mapcat :output ~input-dag)
           pure-required-args# (clojure.set/difference (set required-args#)
                                                       (set generated-args#))
           pure-generated-args# (clojure.set/difference (set generated-args#)
                                                        (set required-args#))]
       (if (clojure.set/superset? (-> args# keys set) (set pure-required-args#))
         ;; run the program
         (let [result-value-map# (apply merge (map #(hash-map % (promise)) generated-args#))]
           (do
             (doall (map (fn [x#] (run-and-deliver-results x# args# result-value-map#))
                         ~input-dag))
             (fmap deref (select-keys result-value-map# pure-generated-args#))
             ))
         ;; or report error that the inputs are not complete 
         (throw (Exception. "Not sufficient inputs to run."))))))

