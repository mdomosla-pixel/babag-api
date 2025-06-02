(ns babag.api.repl
  (:require [clojure.tools.nrepl.server :as nrepl]
            [mount.core :refer [defstate] :as mount]
            [clojure.tools.logging :as log]
            [babag.api.config :as config]))


(defstate repl-server
  :start (when (not= (:enabled config/nrepl) "false")
           (let [nrepl-port (:port config/nrepl)
                 nrepl-bind-address (:bind-address config/nrepl)]
             (log/info (str "Listening for nREPL connections on " nrepl-bind-address ":" nrepl-port))
             (nrepl/start-server :port nrepl-port :bind nrepl-bind-address)))
  :stop (when repl-server (nrepl/stop-server repl-server)))




