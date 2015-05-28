(ns comportex-notebook.core
  (:require [cognitect.transit :as transit]
            [gorilla-repl.core :as g]
            [gorilla-renderable.core :as renderable]
            [org.nfrac.comportex.core]
            [org.nfrac.comportex.protocols :as p])
  (:import [java.io ByteArrayOutputStream]))

(def id-count (atom 0))

(defn next-id! []
  (str "ComportexNotebookOutput" (swap! id-count inc)))

;; Elegant.
(defn run-script-on-el [manipulate-el]
  (let [id (next-id!)]
    {:type :html
     :content (str "<div id='" id "'></div>"
                   "<script type='text/javascript'>"
                   "var el = document.getElementById('" id "');"
                   manipulate-el
                   "</script>")}))

(def write-handlers
  (transit/record-write-handlers
   org.nfrac.comportex.core.RegionNetwork
   org.nfrac.comportex.core.SensoryRegion
   org.nfrac.comportex.core.ExportedSensoriMotorInput
   org.nfrac.comportex.topology.OneDTopology
   org.nfrac.comportex.topology.TwoDTopology
   org.nfrac.comportex.cells.LayerOfCells
   org.nfrac.comportex.cells.LayerActiveState
   org.nfrac.comportex.cells.LayerDistalState
   org.nfrac.comportex.synapses.CellSegmentsSynapseGraph
   org.nfrac.comportex.synapses.SynapseGraph))

(extend-type org.nfrac.comportex.core.RegionNetwork
  renderable/Renderable
  (render [self]
    (let [out (ByteArrayOutputStream.)
          writer (transit/writer out :json
                                 {:handlers write-handlers})
          serializable (p/htm-export self)
          _ (transit/write writer serializable)
          serialized (-> out .toString pr-str)]
      (run-script-on-el (format "comportex_notebook.bridge.add_viz(el, %s);"
                                serialized)))))

(defn start-server [& opts]
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
