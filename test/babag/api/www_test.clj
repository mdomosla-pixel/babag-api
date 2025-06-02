(ns babag.api.www-test
  (:require [clojure.test :refer :all]
            [clojure.data.codec.base64 :as b64]
            [ring.mock.request :as mock]
            [bond.james :as bond :refer [with-stub]]
            [babag.api.www :refer :all]
            [clojure.string :as s]
            [babag.api.utils :as utils]
            [babag.api.mq :as mq]
            [babag.api.database :as database]
            [babag.api.ctrl :as ctrl]
            [cheshire.core :as cheshire]
            [babag.api.metrics :as metrics]
            [babag.api.database :as database]))


(defn hash-part [^String tracking-id]
  (when tracking-id (first (s/split tracking-id #"_"))))

(def VALID-DESTINATION "+11111111111")
(def VALID-SOURCE "test")


(def request-body
  {:from    VALID-SOURCE
   :to      VALID-DESTINATION
   :content "Hello!"})

(deftest test-sending
  (testing "send responds with tracking id"
    (with-redefs [mq/enqueue! (constantly nil)
                  metrics/inc-counter (constantly true)
                  database/put-status (constantly {})]
      (let [content-sha-1 "69342c5c39e5ae5f0077aecc32c0f81811fb8193"
            response (api-routes (-> (mock/request :post (str "/api/" api-version "/sms")
                                                   (cheshire/generate-string request-body))
                                     (mock/content-type "application/json")))
            resp-body (utils/parse-json (slurp (:body response)))
            tracking-id (:id resp-body)]
        (is (= (:status response) 200))
        (is (= (hash-part tracking-id) content-sha-1)))))

  (testing "send is able to use link mobility provider"
    (with-stub [mq/enqueue!
                [metrics/inc-counter (constantly true)]
                [database/put-status (constantly true)]]
      (let [response (api-routes (-> (mock/request :post (str "/api/" api-version "/sms")
                                                   (cheshire/generate-string request-body))
                                     (mock/content-type "application/json")))
            resp-body (utils/parse-json (slurp (:body response)))]
        (is (= (:status response) 200))
        (is (= (count (bond/calls mq/enqueue!)) 1)))))
  )

(deftest test-status-query
  (testing "enqueued message"
    (with-redefs [mq/enqueue! (constantly true)
                  metrics/inc-counter (constantly true)
                  database/get-status (fn [id] "ENQUEUED")]
      (let [response (api-routes (mock/request :get (str "/api/" api-version "/sms/some-id")))
            body (utils/parse-json (slurp (:body response)))]
        (is (= (:status response) 200))
        (is (= (:status body) "enqueued")))))

  (testing "not-found message"
    (with-stub [[metrics/inc-counter (constantly true)]
                [database/get-status (constantly nil)]]
      (let [response (api-routes (mock/request :get (str "/api/" api-version "/sms/some-id")))
            body (utils/parse-json (slurp (:body response)))]
        (is (= (:status response) 404))
        (is (= (:id body) "some-id")))))
  )

(defn- enc-base64-str [^String s]
  (-> s (.getBytes) (b64/encode) (String. "UTF-8")))

(defn- basic-auth [^String user ^String pass]
  (str "Basic " (enc-base64-str (str user ":" pass))))

(deftest test-extract-user
  (testing "Regular auth header"
    (is (= "Aladdin" (extract-user (basic-auth "Aladdin" "OpenSesame")))))

  (testing "User with timeastamp"
    (is (= "Aladdin" (extract-user (basic-auth "Aladdin-2017-01-02" "OpenSesame")))))

  (testing "New-type of user with '--' as separator"
    (is (= "Aladdin" (extract-user (basic-auth "Aladdin--1234" "OpenSesame")))))

  (testing "Unknown auth"
    (is (= "UNKNOWN" (extract-user "UNKNOWN"))))
  )
