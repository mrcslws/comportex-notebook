;; gorilla-repl.fileformat = 1

;; **
;;; # Comportex Notebook
;;; 
;;; Exploring the concept
;; **

;; @@
(ns comportex.notebook
  (:require [gorilla-plot.core :as plot]
            [org.nfrac.comportex.demos.directional-steps-1d :as demo]
            [org.nfrac.comportex.core :as core]
            [org.nfrac.comportex.protocols :as p]
            [org.nfrac.comportex.repl]
            [comportex-notebook.render :as r])
  (:use [clojure.pprint]))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
;; For debugging. Reload code.
(require 'comportex-notebook.render :reload)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(org.nfrac.comportex.repl/truncate-large-data-structures)
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-nil'>nil</span>","value":"nil"}
;; <=

;; @@
(def model (demo/n-region-model 2))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;comportex.notebook/model</span>","value":"#'comportex.notebook/model"}
;; <=

;; @@
model
;; @@

;; @@
(def the-inputs (iterate demo/input-transform demo/initial-input-val))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;comportex.notebook/the-inputs</span>","value":"#'comportex.notebook/the-inputs"}
;; <=

;; @@
(def simulation
  (reductions p/htm-step model the-inputs))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;comportex.notebook/simulation</span>","value":"#'comportex.notebook/simulation"}
;; <=

;; @@
(def model-t1 (nth simulation 1))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;comportex.notebook/model-t1</span>","value":"#'comportex.notebook/model-t1"}
;; <=

;; @@
model-t1
;; @@

;; @@
(r/viz [model model-t1])
;; @@
