(ns ^{:doc "Linear-Time Probabilistic Counting Algorithm"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.linear
  (:require [net.kirankulkarni.quipu.protocols
             :refer [to-byte-array IProbCountingSet]]
            [net.kirankulkarni.quipu.utils :as qu]))

;;; Implements: Linear-time probabilistic counting algorithm

;;; Refer:
;; Kyu-Young Whang , Brad T. Vander-Zanden , Howard M. Taylor, A linear-time probabilistic counting algorithm for database applications, ACM Transactions on Database Systems (TODS), v.15 n.2, p.208-229, June 1990
;;; http://dl.acm.org/citation.cfm?id=78925&CFID=359353900&CFTOKEN=83197792

(declare calculate-cardinality)


;;; This type implements Linear Counting backed by memory.
;;; It uses `java.util.BitSet` to store state of counter.
;;; Fields:
;;; bitset - Bit Array to store state of the counter
;;; n      - Actual cardinality of the Set
;;;          Since we do not know actual cardinality,
;;;          set n to be total number of elements available in stream
;;;          or set it to max cardinality you expect.
;;; m      - Size of Bit Array that will be used

(deftype LinearCounting
    [^:unsynchronized-mutable ^java.util.BitSet bitset n m]
  IProbCountingSet
  (add [this elem]
    (let [hash-code (qu/unsigned-murmurhash-32 elem)
          bit-pos (int (mod hash-code (long m)))]
      (.set bitset bit-pos)
      true))
  (get-card [this] (calculate-cardinality m
                                          (.cardinality bitset)))
  (clear [this] (.clear bitset))
  (size [this] (long (/ m 8))))


(defn calculate-cardinality
  "Calculate cardinality using Linear counting algorithm
   ń = -m ln Vn
   where Vn = Un/m
         Un = number of Unset Bits
         m  = total number of Bits
   But all the set solutions gives us number of set bits hence arguments are
   m  = total number of Bits
   sn = number of set bits"
  [m sn]
  (let [un (- m sn)
        vn (Math/log (/ un m))
        n-estimated (* (- m) vn)]
    (long (Math/round n-estimated))))
