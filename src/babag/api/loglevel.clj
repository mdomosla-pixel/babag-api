(ns babag.api.loglevel
  (:require [mount.core :refer [defstate]]
            [babag.api.config :as config])
  (:import (org.apache.log4j Level LogManager)))

(def levels {"trace" Level/TRACE
             "debug" Level/DEBUG
             "info"  Level/INFO
             "warn"  Level/WARN
             "error" Level/ERROR
             "off"   Level/OFF})

(defn str->level [s]
  (if-let [level (get levels s)]
    level
    (throw (IllegalArgumentException.
             (str "Unknown log level specified: '" s "'; please choose from: " (keys levels))))))

(defstate log-level
  :start (.setLevel (LogManager/getLogger "babag.api") (str->level config/log-level)))
