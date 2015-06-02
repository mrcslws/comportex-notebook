;; gorilla-repl.fileformat = 1

;; **
;;; # encat decode
;;; 
;; **

;; @@
(ns encat-decode
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
;;; 
;; **

;; **
;;;  
;; **

;; @@
(def timestamp-encoder
  (e/pre-transform (comp repeat (partial f/parse (f/formatter "MM/dd/yy HH:mm")))
                   (e/encat 2
                            (e/pre-transform t/hour
                                             (e/linear-encoder 100 10 [0 24]))
                            (e/pre-transform pr/weekend?
                                             (e/category-encoder 20 [true false])))))

(def consumption-encoder (e/linear-encoder 120 10 [3 92]))

(def hotgym-inputs
  (with-open [in-file (io/reader "data/rec-center-hourly.csv")]
    (doall (->> (csv/read-csv in-file)
                (drop 3)
                (map (fn [[timestamp-str consumption-str]]
                       [timestamp-str
                        (Double/parseDouble consumption-str)]))))))
;; @@

;; @@
(def my-enc (e/encat 2
                     timestamp-encoder
                     consumption-encoder))

(def my-input (nth hotgym-inputs 2))

my-input

(def encoded (p/encode my-enc
                           (nth hotgym-inputs 2)))

encoded

(def my-timestamp (first my-input))

my-timestamp

(def encoded-timestamp (p/encode timestamp-encoder my-timestamp))

encoded-timestamp

(def encoded-consumption (p/encode consumption-encoder (second my-input)))

encoded-consumption

(def bit-votes (zipmap encoded (repeat 5)))

bit-votes

(def decoded-back-to-input (p/decode my-enc bit-votes 1))

decoded-back-to-input
;; @@

;; @@
(def my-enc2 (e/encat 3 consumption-encoder))

(def my-consumption (second (nth hotgym-inputs 2)))

my-consumption

(def my-input2 (repeat 3 my-consumption))

(def my-encoded2 (p/encode my-enc2 my-input2))

my-encoded2

(def my-bit-votes2 (zipmap my-encoded2 (repeat 5)))

my-bit-votes2

(def decoded-back-to-input2 (p/decode my-enc2 my-bit-votes2 1))

decoded-back-to-input2
;; @@
