(ns dag-runner.core)

(def a (future (Thread/sleep 10000)
               (println "done")
               100))

@a

