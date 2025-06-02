(ns babag.api.metrics
  (:require [mount.core :refer [defstate]]
            [babag.api.config :as config]
            [clj-statsd :as sd]))

(defstate clj-statsd-setup
  :start (when (:enabled config/metrics)
           (let [{:keys [host port]} config/metrics]
             (sd/setup host port))))


(defn inc-counter [cnt-name]
  (sd/increment (str "babag.api."))
  true)
