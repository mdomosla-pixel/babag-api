(ns babag.api.mq-test
  (:require [clojure.test :refer :all]
            [bond.james :as bond :refer [with-stub]]
            [babag.api.utils :as utils]
            [babag.api.mq :as mq]))


(def empty-config {:host      nil
                   :port      nil
                   :vhost     nil
                   :username  nil
                   :password  nil
                   :cacert    nil
                   :cert      nil
                   :key-pkcs8 nil})

(deftest test-ctrl
  (testing "returns empty when no params provided"
    (is (= (mq/rmq-params empty-config) {})))

  (testing "sets host and port"
    (is (= (mq/rmq-params (assoc empty-config :host "localhost"
                                              :port 5671))
           {:host "localhost"
            :port 5671})))

  (testing "sets credentials"
    (is (= (mq/rmq-params (assoc empty-config :username "guest"
                                              :password "sekret"))
           {:username "guest"
            :password "sekret"})))

  (testing "throws when only some ssl cert vars are provided"
    (is (thrown? IllegalArgumentException
                 (mq/rmq-params (assoc empty-config :cacert "/some/path"))))
    (is (thrown? IllegalArgumentException
                 (mq/rmq-params (-> empty-config
                                    (assoc :cacert "/some/path")
                                    (assoc :key-pkcs8 "/some/other/path"))))))

  (testing "creates ssl context"
    (with-stub [[less.awful.ssl/ssl-context (constantly :returned-ssl-context)]]
      (let [ret (mq/rmq-params (assoc empty-config :ssl true
                                                   :cacert "/some/cacert"
                                                   :key-pkcs8 "/some/key"
                                                   :cert "/some/cert"))]
        (is (= (:ssl-context ret) :returned-ssl-context))
        (is (= (:ssl ret) true))

        (let [calls-to-ssl-context (bond/calls less.awful.ssl/ssl-context)
              [key-path cert-path cacert-path] (:args (first calls-to-ssl-context))]
          (is (= 1 (count calls-to-ssl-context)))
          (is (= cacert-path "/some/cacert"))
          (is (= key-path "/some/key"))
          (is (= cert-path "/some/cert"))))))

  )

