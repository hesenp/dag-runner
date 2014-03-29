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

