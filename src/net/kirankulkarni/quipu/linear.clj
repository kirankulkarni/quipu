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

(deftype LinearCounter
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


(def ^:private predefind-map-sizes
  "Maps n to m"
  {100 5034
   200 5067
   300 5100
   400 5133
   500 5166
   600 5199
   700 5231
   800 5264
   900 5296
   1000 5329
   2000 5647
   3000 5957
   4000 6260
   5000 6556
   6000 6847
   7000 7132
   8000 7412
   9000 7688
   10000 7960
   20000 10506
   30000 12839
   40000 15036
   50000 17134
   60000 19156
   70000 21117
   80000 23029
   90000 24897
   100000 26729
   200000 43710
   300000 59264
   400000 73999
   500000 88175
   600000 101932
   700000 115359
   800000 128514
   900000 141441
   1000000 154171
   2000000 274328
   3000000 386798
   4000000 494794
   5000000 599692
   6000000 702246
   7000000 802931
   8000000 902069
   9000000 999894
   10000000 1096582
   50000000 4584297
   100000000 8571013
   120000000 10112529})

(def ^:private predefind-n
  (sort (keys predefind-map-sizes)))

(def ^:private predefind-n-intervals
  (mapv (fn [x0 x1]
          [x0 x1])
        predefind-n
        (rest predefind-n)))


(defn calculate-m
  "Get Number of bits required for given n"
  [n]
  (assert (<= n (last predefind-n)))
  (or (predefind-map-sizes n)
      (let [[x0 x1] (first (filter #(and (> n (first %))
                                         (< n (second %)))
                                   predefind-n-intervals))
            y0 (get predefind-map-sizes x0)
            y1 (get predefind-map-sizes x1)]
        (qu/lerp x0 y0 x1 y1 n))))
