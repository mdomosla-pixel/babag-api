(ns babag.api.ctrl-test
  (:require [clojure.test :refer :all]
            [babag.api.utils :as utils]
            [babag.api.ctrl :as ctrl]
            [babag.api.metrics :as metrics]
            [babag.api.mq :as mq]))

(defn- args->atom! [atom']
  (fn [& args]
    (reset! atom' args)))

(def VALID-DESTINATION "+11111111111")
(def VALID-SOURCE "test")

(defn fn-called? [atom']
  (not (nil? atom')))

(deftest test-ctrl

  (testing "places message on a queue"
    (let [queue (atom nil)]
      (with-redefs [mq/enqueue! (args->atom! queue)
                    metrics/inc-counter (constantly true)
                    babag.api.database/put-status (fn [& _] true)]
        (let
          [result (ctrl/new-sms {:id      "valid-id"
                                 :user    "testuser"
                                 :from    VALID-SOURCE
                                 :to      VALID-DESTINATION
                                 :content "Hello!"})
           tracking-id (:id (:body result))]
          (let [[body q-meta] @queue
                parsed-body (clojure.edn/read-string body)]
            (is (not (nil? tracking-id)))
            (is (string? tracking-id))
            (is (= (:content parsed-body) "Hello!"))
            (is (= (:type q-meta) "sms"))
            (is (= (:content-type q-meta) "application/edn"))
            (is (= (:message-id q-meta) tracking-id))
            (is (nil? (:correlation-id q-meta))))))))

  (testing "writes status to dynamodb"
    (let [db (atom nil)]
      (with-redefs [mq/enqueue! (fn [& _] true)
                    metrics/inc-counter (constantly true)
                    babag.api.database/put-status (args->atom! db)]
        (let [result (ctrl/new-sms {:id      "valid-id"
                                    :user    "testuser"
                                    :from    VALID-SOURCE
                                    :to      VALID-DESTINATION
                                    :content "Hello!"})
              tracking-id (:id (:body result))]
          (let [[record status] @db
                id (:id record)]
            (is (fn-called? @db))
            (is (not (nil? tracking-id)))
            (is (= id tracking-id))
            (is (= status :enqueued)))))))
  )
