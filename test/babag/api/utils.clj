(ns babag.api.utils
  (:require [clojure.data.codec.base64 :as b64]
            [cheshire.core :as cheshire]))

(defn parse-json [json-str]
  (try
    (cheshire/parse-string json-str keyword)
    (catch Exception _ json-str)))
