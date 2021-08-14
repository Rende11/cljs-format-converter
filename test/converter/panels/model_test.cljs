(ns converter.panels.model-test
  (:require [converter.panels.model :as model]
            [cljs.test :refer-macros [deftest is testing run-tests]]))


(deftest parse
  (testing "json"
    (is (= (model/parse :json "{\"key\": \"value\"}") {:key "value"})))
  (testing "edn"
    (is (= (model/parse :edn "{:key \"value\"}") {:key "value"})))
  (testing "yaml"
    (is (= (model/parse :yaml "key: value") {:key "value"})))
  (testing "object"
    (is (= (model/parse :js "{key: \"value\"}") {:key "value"}))))

(deftest generate
  (testing "edn"
    (is (= (model/generate :edn {:key "value"}) {:key "value"})))
  (testing "json pretty"
    (is (= (model/generate :json {:key "value"}) "{\n  \"key\": \"value\"\n}")))
  (testing "yaml"
    (is (= (model/generate :yaml {:key "value"}) "key: value\n"))))
