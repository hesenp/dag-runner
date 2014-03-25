(ns dag-runner.experiment-data)

;; this program contains the experimental data structure.

;; say we wanna execute function calls of the following stucuture:

;; :input [x_1 x_2] :function f1 :output #{:x_3 :x_4}
;; :input [y_1 x_3] :function f2 :output #{:y_2 :y_3}

;; we wanna be able to bulid a function that, taking the inputs above,
;; automatically generate a function which takes :x_1, :x_2, y_1 as
;; inputs and return :x_3, :x_4, :y_2, :y_3.

;; for the resulted function, the input should all be specified in a
;; hashmap for clarity.


