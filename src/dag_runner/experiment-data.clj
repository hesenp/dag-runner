(ns dag-runner.experiment-data)

;; this program contains the experimental data structure.

;; say we wanna execute function calls of the following stucuture:

;; :input [x_1 x_2] :function f1 :output #{:x_3 :x_4}
;; :input [y_1 x_3] :function f2 :output #{:y_3 :y_4}

;; we wanna be able to bulid a function that, taking the inputs above,
;; automatically generate a function which takes :x_1, :x_2, y_1 as
;; inputs and return :x_3, :x_4, :y_2, :y_3.

;; for the resulted function, the input should all be specified in a
;; hashmap for clarity.

(defn f1 [{a :x1, b :x2}]
  {:x3 (+ a b) :x4 (* a b)})

(defn f2 [{c :y1, d :y2}]
  {:y3 (+ (* c c) (* d d)) :y4 (- c d)})

;; ha, so here's the thing: each argument will only appear in the
;; output once. but they might recur many times as input. there needs
;; to be at least one input whose input is available upon the starting
;; point of the program.

;; so there are two ways to deal with the problem: 1) use clojure
;; "future" and "promise" to have every program run on itself and
;; automatically aggregate results when the computation is done. 2).
;; identify the input and intermediate arguments, and iteratively
;; execute throughout the chain.

;; so, regarding the above two functions:

(def f1-out (promise))

(def f2-out (promise))

(deliver f1-out (f1 {:x1 1 :x2 2}))

(deliver f2-out (f2 {:y1 (:x3 @f1-out) :y2 4}))

;; however, if we run the (deliver f2-out ... )first and the result
;; won't be able to obtained since the evaluation will be suspended
;; forever.

(do 
  (deliver f2-out (f2 {:y1 (:x3 @f1-out) :y2 4}))
  (deliver f1-out (f1 {:x1 1 :x2 2}))
  (deref f2-out)
  )

;; ok, this trick will do the work. nice :) 

(def a {:name "haha" :content (promise)})

(deliver (:content a) "haha")

(defmacro postfix-notation
  [expression]
  (conj (butlast expression) (last expression)))

(postfix-notation (1 2 +))

(defmacro critic
  [{:keys [good bad]}]
  (do (println "Great!"
               (quote ~good))
      (println "bad~"
               (quote ~bad))))

(defmacro run-add
  [add other]
  `(when ~add
     1
     ~other))

(macroexpand (run-add true (+ 12)))

(defmacro add-laughter
  [execute]
  (do (println "haha")
      execute))

(add-laughter (+ 1 2))

(defn add-another
  [execute]
  (do (println "haha")
      execute))

(defmacro empty-func [fname & body]
  `(defn ~fname []
     ~@body))

(defmacro empty-func [fname & body]
  `(defn ~fname []
     ~(if (= (first body) true)
        ~@body
        2
        )))

(macroexpand-1 '(empty-func haha (true)))

(empty-func haha (true))

(haha)

(defmacro loud-function
  [fname args & body]
  (do `(defn ~fname [~args]
         ~@body
         )
      (println "haha")
      ))

(macroexpand  (loud-function addone x (+ 1 x)))

(defmacro cute-function
  [fname]
  `(let [temp# (rand)]
     (defn ~fname [x#] (+ temp# x#))))

(cute-function haha)
