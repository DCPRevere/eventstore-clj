(ns eventstore-clj.utils-test
  (:require [eventstore-clj.utils :as sut]
            [clojure.test :as t]
            [clojure.spec.test.alpha :as stest]
            [respeced.test :as rt]
            ))

(defn num-tests [n]
  {:clojure.spec.test.check/opts {:num-tests n}})

(t/deftest ->scala-List-test
  (t/is (rt/successful? (stest/check `sut/->scala-List (num-tests 100)))))

(t/deftest scala-List->test
  (t/is (rt/successful? (stest/check `sut/scala-List-> (num-tests 100)))))

(t/deftest get-or-else-test
  (t/is (rt/successful? (stest/check `sut/get-or-else (num-tests 100)))))

