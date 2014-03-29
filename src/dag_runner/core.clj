(ns dag-runner.core
  (:use [clojure.contrib.generic.functor :only [fmap]]))

(def temp
  [{:input [:x1 :x2]
    :output [:y1 :y2]
    :function (fn [{:keys [x1 x2]}]
                {:y1 (+ x1 x2) :y2 (- x1 x2)})}
   {:input [:y1 :z2]
    :output [:w1 :w2]
    :function (fn [{:keys [y1 z2]}]
                {:w1 (* y1 z2) :w2 (+ y1 z2)})}])

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

(defmacro dag-runner
  "this function will execute functions in a Directed Acyclic Graph as
  specified by their argument and output lists. "
  [fname input-dag]
  `(defn ~fname [& {:as args#}]
     (let [input-args# (mapcat :input ~input-dag)
           output-args# (mapcat :output ~input-dag)
           pure-input-args# (clojure.set/difference (set input-args#)
                                                    (set output-args#))
           pure-output-args# (clojure.set/difference (set output-args#)
                                                     (set input-args))]
       (if (clojure.set/superset? (-> args# keys set) pure-input-args#)
         ;; run the program
         (let [result-value-map (map #(hash-map % (promise)) output-args)]
           (do
             (map tree-mapping ~input-dag)
             (select-keys result-value-map# (vec pure-output-args#))))
         ;; or report error that the inputs are not complete 
         (throw (Exception. "Not sufficient inputs to run."))))))

