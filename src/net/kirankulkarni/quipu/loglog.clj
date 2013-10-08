(ns ^{:doc "LogLog Implementation"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.loglog
  (:require [net.kirankulkarni.quipu.protocols
             :refer [to-byte-array]]
            [net.kirankulkarni.quipu.core
             :refer :all]
            [net.kirankulkarni.quipu.utils :as qu]))


(defn square [x] (* x x))

(defn log2 [x] (/ (Math/log x) (Math/log 2)))

(defn pow2 [x] (Math/pow 2 x))

(defn >>> [v bits] (bit-shift-right (bit-and 0xFFFFFFFF v) bits))

(declare add-elem get-cardinality)

(deftype LogLogCounter
    [^:unsynchronized-mutable bitarray n m k alpham]
    IProbCountingSet
  (add [this elem] (add-elem bitarray k elem))
  (get-card [this] (get-cardinality bitarray m alpham))
  (size [this] m))


(defn- ^:inline add-elem
  [bytearray k elem]
  (let [elem-hash (qu/murmurhash-32 elem)
        j (>>> elem-hash (- Integer/SIZE k))
        r (- (Integer/numberOfLeadingZeros
              (bit-and elem-hash
                       (>>> 0xFFFFFFFF k)))
             (dec k))]
    (when (< (aget bytearray j) r)
      (aset-byte bytearray j r)
      true)))


(defn- ^:inline get-cardinality
  [bytearray m alpham]
  (let [avg (/ (reduce + bytearray) m)]
    (* alpham m (pow2 avg))))

(defn find-closest
  [x coll]
  (first
   (first
    (sort-by second
             (mapv (fn [val]
                     [val (Math/abs (- val x))])
                   coll)))))

;;; alphams calculated using R, expression: ((gamma((-1)/m) * ((1 - (2 ^ (1 /m)) / log(2))
(let [m-in-pow2 (mapv (comp long #(Math/pow 2 %))
                      (range 25))
      alphams [0 0.2228396 0.3120160 0.3548907 0.3760327 0.3865412 0.3917811
               0.3943976 0.3957050 0.3963585 0.3966852 0.3968485 0.3969301 0.3969710
               0.3969914 0.3970016 0.3970066 0.3970084 0.3970086 0.3969941 0.3969818
               0.3969567 0.3980263 0.4001740 0.4090892]]
  (defn get-loglog-counter
    [n expected-error]
    (let [m-given (square (/ 1.30 expected-error))
          m (find-closest m-given m-in-pow2)
          k (int (log2 m))
          max-n (* m (pow2 (pow2 8)))]
      (assert (<= k 24) "K should be smaller than 24")
      (assert (<= n max-n) (str "Can not support given cardinality " n))
      (LogLogCounter. (byte-array m) n m k (alphams k)))))
