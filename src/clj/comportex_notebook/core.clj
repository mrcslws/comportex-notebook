(ns comportex-notebook.core
  (:require [gorilla-repl.core :as g]))

(defn run [& opts]
  (let [{:keys [port ip nrepl-port gorilla-options]} (apply hash-map opts)
        port (or port 0)
        ip (or ip "127.0.0.1")
        nrepl-port (or nrepl-port 0)]
    (g/run-gorilla-server {:port port
                           :ip ip
                           :nrepl-port nrepl-port
                           :version "0.3.5-MARCUS"
                           :project "comportex-notebook"
                           :gorilla-options gorilla-options})))
