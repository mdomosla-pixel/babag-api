(ns babag.api.ctrl
  (:require [ring.util.response :refer [response status]]
            [clojure.string :as string]
            [babag.api.model :as model]
            [babag-common.core :as comm]
            [schema.core :as s]
            [digest]
            [ring.util.http-response :refer [ok service-unavailable not-found]]
            [clj-statsd :as sd]
            [clj-uuid :as uuid]
            [babag.api.metrics :as metrics]
            [clojure.string :as str]))

(s/defn new-sms
  [{:keys [user] :as record} :- (dissoc comm/SmsRequest :id)]
  (sd/with-timing "babag.api.new-sms"
                  (let [id (comm/content-to-id (:content record))]
                    (if (and (sd/with-timing "babag.api.rabbitmq.enqueue" (model/enqueue-sms (assoc record :id id)))
                             (sd/with-timing "babag.api.postgresql.register-id" (model/register-id (assoc record :id id)))
                             (metrics/inc-counter (str "sms-requested." user)))
                      (ok {:id id})
                      (service-unavailable {:msg "Unable to enqueue message"})))))

(defn to-enum [status-str]
  (keyword (str/lower-case status-str)))

(s/defn sms-status
  [id :- s/Str]
  (if-let [status (model/get-status id)]
    (ok {:status (to-enum status)})
    (not-found {:id id})))
