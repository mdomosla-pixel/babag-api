(ns babag.api.mq
  (:require [mount.core :refer [defstate]]
            [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [clojure.tools.logging :as log]
            [less.awful.ssl :as ssl]
            [clojure.java.io :as io]
            [babag.api.config :as config]
            [babag.api.metrics :as metrics]
            [mount.core :as mount]))

(def ^:const default-exchange-name "")

(defn new-channel [conn q-name]
  (let [c (lch/open conn)]
    (lq/declare c q-name {:durable     true
                          :exclusive   false
                          :auto-delete false})
    c))

(defn only-some? [xs]
  (and (some some? xs) (not-every? some? xs)))

(defn rmq-params [config]
  (let [{:keys [host port username password vhost ssl key-pkcs8 cert cacert]} config
        ssl-certs [key-pkcs8 cert cacert]]
    (when (and (only-some? ssl-certs))
      (throw (IllegalArgumentException.
               "Either all or none of rabbitmq :key-pkcs8, :cert, :cacert must be provided")))
    (merge {}
           (when host {:host host})
           (when port {:port port})
           (when username {:username username})
           (when password {:password password})
           (when vhost {:vhost vhost})
           (when ssl {:ssl true})
           (when (every? some? ssl-certs) {:ssl-context (apply ssl/ssl-context ssl-certs)}))))

(defn rmq-connect [opts]
  (rmq/connect opts))

(defstate conn
  :start (rmq-connect (rmq-params config/rabbitmq))
  :stop (rmq/close conn))

(defstate ch
  :start (new-channel conn (:queue-name config/rabbitmq))
  :stop (when (and ch (.isOpen ch))
          rmq/close ch))


(defn restart-ch [ch]
  (let [ch# (var ch)]
    (mount/stop ch#)
    (mount/start ch#)))

(defn enqueue! [body q-meta]
  (lb/publish ch default-exchange-name (:queue-name config/rabbitmq) body q-meta)
  true)

