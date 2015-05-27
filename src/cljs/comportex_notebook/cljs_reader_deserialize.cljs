(ns comportex-notebook.cljs-reader-deserialize
  (:require [cljs.reader :as rdr]
            [org.nfrac.comportex.core :refer [map->RegionNetwork
                                              map->SensoryRegion
                                              map->ExportedSensoriMotorInput]]
            [org.nfrac.comportex.topology :refer [map->OneDTopology
                                                  map->TwoDTopology]]
            [org.nfrac.comportex.cells :refer [map->LayerOfCells
                                               map->LayerActiveState
                                               map->LayerDistalState]]
            [org.nfrac.comportex.synapses :refer [map->CellSegmentsSynapseGraph
                                                  map->SynapseGraph]]))

;; Dumping ground.
;; This was an experiment to use Clojure's built-in serialization / reading.
;; The problem is that when serializing from Clojure it puts in strings like
;; "clojure.lang.PersistentArrayMap/create". So this won't work for tossing
;; between Clojure and ClojureScript.

(doseq [[t p] {"org.nfrac.comportex.core.RegionNetwork" map->RegionNetwork
               "org.nfrac.comportex.core.SensoryRegion" map->SensoryRegion
               "org.nfrac.comportex.core.ExportedSensoriMotorInput" map->ExportedSensoriMotorInput
               "org.nfrac.comportex.topology.OneDTopology" map->OneDTopology
               "org.nfrac.comportex.topology.TwoDTopology" map->TwoDTopology
               "org.nfrac.comportex.cells.LayerOfCells" map->LayerOfCells
               "org.nfrac.comportex.cells.LayerActiveState" map->LayerActiveState
               "org.nfrac.comportex.cells.LayerDistalState" map->LayerDistalState
               "org.nfrac.comportex.synapses.CellSegmentsSynapseGraph" map->CellSegmentsSynapseGraph
               "org.nfrac.comportex.synapses.SynapseGraph" map->SynapseGraph}]
  (rdr/register-tag-parser! t p))

(defn ^:export add-viz [el serialized-model]
  (let [model (rdr/read-string serialized-model)]
    (set! (.-innerHTML el) "Injected from cljs!")
    (js/console.log model)))
