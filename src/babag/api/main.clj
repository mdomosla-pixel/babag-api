(ns babag.api.main
  (:require [mount.core :as mount]
            [babag.api.config :refer [conf rabbitmq api admin postgres]]
            [babag.api.repl :refer [repl-server]]
            [babag.api.www :refer [api-server admin-server]]
            [babag.api.loglevel :refer [log-level]]
            [babag.api.metrics :refer [clj-statsd-setup]]
            [babag.api.mq])
  (:gen-class))


(defn -main []
  (mount/start #'babag.api.config/conf)
  (mount/start #'babag.api.config/rabbitmq)
  (mount/start #'babag.api.config/api)
  (mount/start #'babag.api.config/admin)
  (mount/start #'babag.api.config/postgres)
  (mount/start))

