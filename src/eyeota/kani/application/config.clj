(ns eyeota.kani.application.config)

(defn read-config
  [config-file]
  (let [config (clojure.edn/read-string (slurp config-file))
        default-config {:port        9042
                        :null-value  "<null>"
                        :fetch-size  5000
                        :consistency :quorum}]
    (merge default-config config)))
