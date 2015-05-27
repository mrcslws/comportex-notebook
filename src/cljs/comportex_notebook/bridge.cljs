(ns comportex-notebook.bridge
  (:require [cljs.core.async :as async :refer [chan put! <!]]
            [cognitect.transit :as transit]
            [comportexviz.viz-canvas :as viz]
            [org.nfrac.comportex.protocols :as p]
            [org.nfrac.comportex.core :refer [map->RegionNetwork
                                              map->SensoryRegion
                                              map->ExportedSensoriMotorInput]]
            [org.nfrac.comportex.topology :refer [map->OneDTopology
                                                  map->TwoDTopology]]
            [org.nfrac.comportex.cells :refer [map->LayerOfCells
                                               map->LayerActiveState
                                               map->LayerDistalState]]
            [org.nfrac.comportex.synapses :refer [map->CellSegmentsSynapseGraph
                                                  map->SynapseGraph]]
            [reagent.core :as reagent :refer [atom]]))

(def handlers
  {"org.nfrac.comportex.core.RegionNetwork" map->RegionNetwork
   "org.nfrac.comportex.core.SensoryRegion" map->SensoryRegion
   "org.nfrac.comportex.core.ExportedSensoriMotorInput" map->ExportedSensoriMotorInput
   "org.nfrac.comportex.topology.OneDTopology" map->OneDTopology
   "org.nfrac.comportex.topology.TwoDTopology" map->TwoDTopology
   "org.nfrac.comportex.cells.LayerOfCells" map->LayerOfCells
   "org.nfrac.comportex.cells.LayerActiveState" map->LayerActiveState
   "org.nfrac.comportex.cells.LayerDistalState" map->LayerDistalState
   "org.nfrac.comportex.synapses.CellSegmentsSynapseGraph" map->CellSegmentsSynapseGraph
   "org.nfrac.comportex.synapses.SynapseGraph" map->SynapseGraph})

(defn ^:export add-viz [el serialized-model]
  (let [reader (transit/reader :json {:handlers handlers})
        model (transit/read reader serialized-model)
        model-steps (atom [(viz/init-caches model)])
        selection (atom viz/blank-selection)
        viz-options (atom viz/default-viz-options)
        into-viz (chan)
        from-viz nil]
    (set! (.-innerHTML el) "Injected from cljs!")
    (reagent/render [viz/viz-canvas {} model-steps selection viz-options
                     into-viz from-viz]
                    el)))