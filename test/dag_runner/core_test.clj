(ns dag-runner.core-test
  (:require [clojure.test :refer :all]
            [dag-runner.core :refer :all])
  (:use [clojure.contrib.generic.functor :only [fmap]]))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

;; here's a few very rudimentary tests. ask if we need to add more.

(deftest test-aggregate-arguments
  (testing "test for aggregate-arguments. "
    (let [args-list [:x :y :u]
          input-arg {:x 1 :y 2 :z 3}
          promise-arg {:u (promise) :v (promise)}]
      (do ;; first assign values to the undelivered promise-args.
        (deliver (:u promise-arg) 1)
        (deliver (:v promise-arg) 2)
        (let [temp  (aggregate-arguments args-list input-arg promise-arg)]
          (is (= {:x 1 :y 2 :u 1} temp)))))))



;; we used this part of the test of discover that map is generating
;; lazy-seq which will not immediately update a promise. we need to
;; use "doall" to make the changes happen.

(deftest test-update-results
  (testing "test for update-result capability to delivery results to
  promises."
    (let [result-list {:x 1 :y 2}
          promise-arg {:x (promise) :y (promise)}]
      (do
        (update-results result-list promise-arg)
        (is (= {:x 1 :y 2} (fmap deref promise-arg)))))))

(deftest test-run-and-deliver-results
  (testing "run the process for the corresponding function and deliver
  results to the corresponding place."
    (let [function-info {:function (fn [{:keys [x y]}] {:z (+ x y)})
                         :input [:x :y]
                         :output [:z]}
          input-arg {:x 1 :y 2}
          promise-arg {:z (promise)}]
      (do
        (run-and-deliver-results function-info input-arg promise-arg)
        (is (= 3 (-> promise-arg :z deref)))))))

(deftest test-dagg-runner
  (testing "this will test the core function of dag-runner and make
  sure it works."
    (let [temp [{:input [:x1 :x2]
                 :output [:y1 :y2]
                 :function (fn [{:keys [x1 x2]}]
                             {:y1 (+ x1 x2) :y2 (- x1 x2)})}
                {:input [:y1 :z2]
                 :output [:w1 :w2]
                 :function (fn [{:keys [y1 z2]}]
                             {:w1 (* y1 z2) :w2 (+ y1 z2)})}]]
      (do
        ;; define a thing called runner according to specification
        ;; from temp and have it run.
        (dag-runner runner temp)
        (let [result (runner :x1 1 :x2 2 :z2 3)]
          (is (= result {:y2 -1 :w2 6 :w1 9})))))))
