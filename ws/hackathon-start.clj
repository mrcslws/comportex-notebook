;; gorilla-repl.fileformat = 1

;; **
;;; # Comportex Notebook
;;; 
;;; It's:
;;; - HTM
;;; - In a notebook
;;; - With custom rendering
;; **

;; @@
(ns numenta-hackathon
  (:require [gorilla-plot.core :as plot]
            [org.nfrac.comportex.core :as core]
            [org.nfrac.comportex.encoders :as e]
            [org.nfrac.comportex.protocols :as p]
            [org.nfrac.comportex.repl]
            [comportex-notebook.render :as r :refer [viz viz++]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.predicates :as pr]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:use [clojure.pprint]
        [clojure.stacktrace]))

(org.nfrac.comportex.repl/truncate-large-data-structures)
;; @@

;; **
;;; ## Part 1: Let's play.
;; **

;; @@
(def spec
  {:column-dimensions [200]
   :ff-potential-radius 0.2
   :ff-perm-inc 0.05
   :ff-perm-dec 0.01
   :ff-perm-connected 0.20
   :ff-stimulus-threshold 3
   :global-inhibition? false
   :activation-level 0.04
   :boost-active-every 10000
   :depth 2
   :max-segments 5
   :seg-max-synapse-count 18
   :seg-new-synapse-count 12
   :seg-stimulus-threshold 9
   :seg-learn-threshold 7
   :distal-perm-connected 0.20
   :distal-perm-inc 0.05
   :distal-perm-dec 0.01
   :distal-perm-init 0.16})

(def sensory-input (core/sensory-input (e/linear-encoder
                                        200 10 [0 24])))

(def hello-model (core/regions-in-series
                  core/sensory-region
                  sensory-input 1
                  (repeat spec)))

hello-model
;; @@

;; @@
(def hello-inputs (range 0 12))

(def hello-simulation
  (reductions p/htm-step hello-model hello-inputs))
;; @@

;; @@
(viz++ hello-simulation)
;; @@

;; **
;;; ## Part 2: Hot Gym
;; **

;; @@
(def hotgym-spec
  {:column-dimensions [1024]
   :ff-potential-radius 0.2
   :ff-perm-inc 0.05
   :ff-perm-dec 0.01
   :ff-perm-connected 0.20
   :ff-stimulus-threshold 3
   :global-inhibition? false
   :activation-level 0.04
   :boost-active-every 10000
   :depth 4
   :max-segments 5
   :seg-max-synapse-count 18
   :seg-new-synapse-count 12
   :seg-stimulus-threshold 9
   :seg-learn-threshold 7
   :distal-perm-connected 0.20
   :distal-perm-inc 0.05
   :distal-perm-dec 0.01
   :distal-perm-init 0.16})

(def timestamp-encoder
  (e/pre-transform (comp repeat (partial f/parse (f/formatter "MM/dd/yy HH:mm")))
                   (e/encat 2
                            (e/pre-transform t/hour
                                             (e/linear-encoder 100 10 [0 24]))
                            (e/pre-transform pr/weekend?
                                             (e/category-encoder 20 [true false])))))

(def consumption-encoder (e/linear-encoder 120 10 [3 92]))

(def hotgym-sensory-input
  (core/sensory-input (e/encat 2
                               timestamp-encoder
                               consumption-encoder)))

(def model
  (core/regions-in-series core/sensory-region hotgym-sensory-input
                          1 [hotgym-spec]))

model

(def hotgym-inputs
  (with-open [in-file (io/reader "data/rec-center-hourly.csv")]
    (doall (->> (csv/read-csv in-file)
                (drop 3)
                (map (fn [[timestamp-str consumption-str]]
                       [timestamp-str
                        (Double/parseDouble consumption-str)]))))))

(def simulation
  (reductions p/htm-step model hotgym-inputs))

(def actual-consumptions (->> hotgym-inputs
                              (map second)))

(def predicted-consumptions (->> simulation
                                 (map #(core/predictions % 1))
                                 (map (fn [[_ [{:keys [value]}]]]
                                        value))))
;; @@

;; @@
(defn steps* [s l]
  (->> simulation
       (drop s)
       (take l)))

(def steps (comp viz steps*))
(def steps++ (comp viz++ steps*))
;; @@

;; @@
(steps 0 20)
;; @@

;; @@
(defn plot-predictions [range-start length]
  (let [range-start (max 1 range-start) ;; need a previous step for predictions
        r (range range-start (+ range-start length) 1)]
    (plot/compose
     (plot/list-plot (->> r
                          (map (fn [i]
                                 [i (nth actual-consumptions i)])))
                     :plot-size 800
                     :joined true
                     :color "red"
                     :opacity 0.8)
     (plot/list-plot (->> r
                          (map (fn [i]
                                 [i (nth predicted-consumptions (dec i))])))
                     :plot-size 800
                     :joined false
                     :color "blue"
                     :opacity 0.8))))
;; @@

;; @@
(plot-predictions 0 20)
;; @@

;; @@
(plot-predictions 500 20)
;; @@

;; @@
(plot-predictions 1000 20)
;; @@

;; @@
(steps 1000 10)
;; @@

;; @@
(steps++ 1000 3)
;; @@

;; **
;;; 
;; **

;; **
;;; ## Part 3: To the cloud.
;; **

;; **
;;; Time for a change of scene.
;; **

;; **
;;; 
;; **

;; **
;;; 
;; **

;; **
;;; .
;; **

;; **
;;; .
;; **

;; **
;;; .
;; **

;; **
;;; .
;; **

;; **
;;; ## Trivia
;;; 
;;; - Different from ComportexViz. There's a server.
;; **

;; **
;;; ## Takeaways
;; **

;; **
;;; - Seeing is important
;;; - Notebooks are powerful
;;; 
;;; With these powers combined...
;; **

;; **
;;; 
;; **
