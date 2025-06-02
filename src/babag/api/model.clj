(ns babag.api.model
  (:require
    [clj-uuid :as uuid]
    [cheshire.core :as cheshire]
    [schema.core :as s]
    [babag.api.mq :refer [ch] :as mq]
    [babag.api.database :as database]
    [babag.api.config :as config]
    [babag-common.core :as comm]))

(defn register-id
  [record]
  (database/put-status record :enqueued))

(s/defn enqueue-sms :- s/Str
  ([record]
    (enqueue-sms record nil))
  ([record correlation-id :- s/Str]
    (comm/verify-sms record)
    (mq/enqueue!
      (pr-str record)
      {:content-type   "application/edn"
       :type           "sms"
       :correlation-id correlation-id
       :persistent     true
       :message-id     (:id record)})
    (:id record)))

(s/defn get-status :- comm/SmsStatus
  [id :- s/Str]
  (database/get-status id))
