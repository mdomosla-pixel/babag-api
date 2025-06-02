(ns babag.api.config
  (:require [cprop.core :refer [load-config]]
            [mount.core :refer [defstate]]))

(defstate conf
  :start (load-config))

(defstate log-level
  :start (:log-level conf))

(defstate aws
  :start (:aws conf))

(defstate postgres
  :start (:postgres conf))

(defstate rabbitmq
  :start (:rabbitmq conf))

(defstate api
  :start (:api conf))

(defstate admin
  :start (:admin conf))

(defstate nrepl
  :start (:nrepl conf))

(defstate metrics
  :start (:metrics conf))
