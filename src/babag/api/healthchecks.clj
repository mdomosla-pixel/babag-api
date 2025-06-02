(ns babag.api.healthchecks
  (:require [babag.api.mq :as mq]
            [ring.util.http-response :refer :all]
            [babag.api.database :as db]
            [cheshire.core :as json]))

(defn- json-content-type [resp]
  (header resp "Content-Type" "application/json"))

(defn check-self []
  (json-content-type
    (ok (json/generate-string {:healthy true}))))

(defn check-dependencies []
  (json-content-type
    (if (and (.isOpen mq/conn) (db/health-check))
      (ok (json/generate-string {:healthy true}))
      (internal-server-error
        (json/generate-string {:healthy  false
                               :rabbitmq {:message "Connection to RabbitMQ is down"}})))))


