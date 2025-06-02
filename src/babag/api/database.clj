(ns babag.api.database
  (:require [mount.core :refer [defstate] :as mount]
            [again.core :as again]
            [babag.api.config :refer [postgres]]
            [babag-common.database :as dbcommon]
            [hikari-cp.core :refer [make-datasource close-datasource]]
            [clojure.java.jdbc :as jdbc]))


(defstate hikari-cp
  :start (make-datasource
           (merge
             postgres
             {:adapter "pgjdbc-ng"}))
  :stop (close-datasource hikari-cp))

(defn put-status [& args]
  (apply dbcommon/put-status (into [hikari-cp] args)))

(defn get-status [& args]
  (apply dbcommon/get-status (into [hikari-cp] args)))

(defn health-check []
  (dbcommon/health-check hikari-cp))
