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
(org.nfrac.comportex.repl/truncate-large-data-structures)
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
(def model (demo/n-region-model 2))
(def the-inputs (iterate demo/input-transform demo/initial-input-val))
(def simulation
  (reductions p/htm-step model the-inputs))
;; @@
;; =>
;;; {"type":"html","content":"<span class='clj-var'>#&#x27;comportex.notebook/simulation</span>","value":"#'comportex.notebook/simulation"}
;; <=

;; @@
(->> simulation
     (take 3)
     r/viz)
;; @@
