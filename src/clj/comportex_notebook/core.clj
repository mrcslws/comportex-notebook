(ns comportex-notebook.core
  (:require [cognitect.transit :as transit]
            [gorilla-renderable.core :as renderable]
            [org.nfrac.comportex.core]
            [org.nfrac.comportex.protocols :as p])
  (:import [java.io ByteArrayOutputStream]))

(def import-if-necessary
  "<script type='text/javascript'>
     if (!window.ComportexNotebookScriptInjected) {
        var scriptEl = document.createElement(\"script\");
        scriptEl.setAttribute(\"type\", \"text/javascript\");
        scriptEl.setAttribute(\"src\", \"cnotebook.js\");
        document.body.appendChild(scriptEl);

        goog.require('comportex_notebook.bridge');

        window.ComportexNotebookScriptInjected = true;
     }
   </script>")

(def id-count (atom 0))

(defn next-id! []
  (str "ComportexNotebookOutput" (swap! id-count inc)))

;; Elegant.
(defn run-script-on-el [manipulate-el]
  (let [id (next-id!)]
    {:type :html
     :content (str import-if-necessary
                   "<div id='" id "'></div>"
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
