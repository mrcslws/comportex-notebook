(ns comportex-notebook.render
  (:require [cognitect.transit :as transit]
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

(defn strip-synapses [model]
  (-> model
      (update-in [:regions :rgn-0 :layer-3 :proximal-sg :int-sg]
                 assoc
                 :syns-by-target nil
                 :targets-by-source nil)
      (update-in [:regions :rgn-0 :layer-3 :distal-sg :int-sg]
                 assoc
                 :syns-by-target nil
                 :targets-by-source nil)))

(defn vector->map [syns-v]
  (loop [syns-m {}
         i 0]
    (if (< i (count syns-v))
      (recur (let [v (nth syns-v i)]
               (if (empty? v)
                 syns-m
                 (assoc syns-m i v)))
             (inc i))
      syns-m)))

;; Switch from fast data structures to smaller data structures
(defn optimize-synapses [model]
  (-> model
      (update-in [:regions :rgn-0 :layer-3 :proximal-sg :int-sg :syns-by-target]
                 vector->map)
      (update-in [:regions :rgn-0 :layer-3 :proximal-sg :int-sg :targets-by-source]
                 vector->map)
      (update-in [:regions :rgn-0 :layer-3 :distal-sg :int-sg :syns-by-target]
                 vector->map)
      (update-in [:regions :rgn-0 :layer-3 :distal-sg :int-sg :targets-by-source]
                 vector->map)))

(defn- models->gorilladata [models include-synapses?]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json
                               {:handlers write-handlers})
        serializable (mapv p/htm-export models)
        serializable (mapv (if include-synapses?
                             optimize-synapses
                             strip-synapses) serializable)
        _ (transit/write writer serializable)
        serialized (-> out .toString pr-str)]
    (run-script-on-el (format "comportex_notebook.bridge.add_viz(el, %s);"
                              serialized))))

(defn viz [models]
  (reify
    renderable/Renderable
    (render [_]
      (models->gorilladata models false))))

(defn viz++ [models]
  (reify
    renderable/Renderable
    (render [_]
      (models->gorilladata models true))))

(extend-type org.nfrac.comportex.core.RegionNetwork
  renderable/Renderable
  (render [self]
    (models->gorilladata [self] false)))
