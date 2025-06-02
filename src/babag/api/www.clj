(ns babag.api.www
  (:require [compojure.api.sweet :refer :all]
            [digest]
            [mount.core :as mount :refer [defstate]]
            [ring.util.http-response :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [schema.core :as s]
            [cheshire.core :as json]
            [manifest.core :as mf]
            [clojure.string :as str]
            [clojure.data.codec.base64 :as b64]
            [babag.api.ctrl :as ctrl]
            [babag.api.model :as model]
            [babag.api.healthchecks :as healthchecks]
            [babag.api.config :as config]
            [babag-common.core :as comm]))

(def api-version "v1")

(defn parse-auth [auth-header]
  (-> (drop (count "Basic ") auth-header)
      (str/join)
      (.getBytes)
      (b64/decode)
      (String. "UTF-8")))

(defn extract-user [auth-header]
  (if (= auth-header "UNKNOWN")
    "UNKNOWN"
    (-> auth-header
        (parse-auth)
        (str/split #":")
        (first)
        (str/split #"-")
        (first))))

(def api-routes
  (api
    (swagger-routes {:data {:info {:version     "0.0.1-alpha"
                                   :title       "SMS Gateway"
                                   :description "REST API for sending Short Messages (SMS) to mobile phones"}
                            :tags [:name "sms" :description "sending sms and checking its status"]}})
    (context (str "/api/" api-version) []
      :tags ["sms"]
      (POST "/sms" []
        :header-params [{authorization :- (describe s/Str "something") "UNKNOWN"}]
        :body [{:keys [provider] :or {provider :linkmobility} :as sms} (dissoc comm/SmsRequest
                                                                               :id :user)]
        :return {:id (comm/SmsRequest :id)}
        :responses {503 {:schema      {(s/optional-key :msg) (describe s/Str
                                                                       "Description of error")}
                         :description "Returned when critical dependency is unavailable (e.g. AMQP server)"}}
        (ctrl/new-sms (-> sms
                          (assoc :provider provider)
                          (assoc :user (extract-user authorization)))))
      (GET "/sms/:id" []
        :path-params [id :- (describe s/Str "ID obtained from POST /sms")]
        :responses {404 {:schema {:id (:id comm/SmsRequest)} :description "Returned when id is not found in the db"}
                    503 {:schema {} :description "Returned when db is down"}}
        :return {:status comm/SmsStatus}
        (ctrl/sms-status id)))))


(defstate api-server
  :start (run-jetty api-routes {:port  (:port config/api)
                                :host  (:bind-address config/api)
                                :join? false})
  :stop (.stop api-server))

;; We're using mount here so we don't get cyclic dep: [main -> www -> main] since
;; retrieving manifest autoloads main class
(defstate jar-manifest :start (mf/manifest "babag.api.main"))

(defroutes admin-routes
  (context "/admin" []
    (GET "/version" [] (json/generate-string (if (seq jar-manifest)
                                               jar-manifest
                                               "Unable read JAR's Manifest.mf")
                                             {:pretty true}))
    (context "/health" []
      (GET "/ping" [] (healthchecks/check-self))
      (GET "/deps" [] (healthchecks/check-dependencies)))))

(defstate admin-server
  :start (run-jetty admin-routes {:port  (:port config/admin)
                                  :host  (:bind-address config/admin)
                                  :join? false})
  :stop (.stop admin-server))

