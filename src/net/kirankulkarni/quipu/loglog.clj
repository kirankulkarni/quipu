(ns ^{:doc "LogLog Implementation"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.loglog
  (:require [net.kirankulkarni.quipu.protocols
             :refer [to-byte-array]]
            [net.kirankulkarni.quipu.core
             :refer :all]
            [net.kirankulkarni.quipu.utils :as qu]))



(declare add-elem get-cardinality get-size)

(deftype LogLogCounter
    [^:unsynchronized-mutable bitarray n m k alpham]
    IProbCountingSet
  (add [this elem] (add-elem bitarray k elem))
  (get-card [this] (get-cardinality bitarray m alpham))
  (size [this] (get-size m k)))


(defn- ^:inline add-elem
  [bytearray k elem]
  (let [elem-hash (qu/murmurhash-32 elem)
        j (qu/>>> elem-hash (- Integer/SIZE k))
        r (- (Integer/numberOfLeadingZeros
              (bit-and elem-hash
                       (qu/>>> 0xFFFFFFFF k)))
             (dec k))]
    (when (< (aget bytearray j) r)
      (aset-byte bytearray j r)
      true)))


(defn- ^:inline get-cardinality
  [bytearray m alpham]
  (let [avg (/ (reduce + bytearray) m)]
    (* alpham m (qu/pow2 avg))))


(defn- get-size
  [m k]
  (let [max-rank (- Integer/SIZE k)
        string-rep (Integer/toString max-rank 2)]
    (long (/ (* m (count string-rep))
             8))))


(defn find-closest
  [x coll]
  (first
   (first
    (sort-by second
             (mapv (fn [val]
                     [val (Math/abs (- val x))])
                   coll)))))

;;; alphams calculated using R, expression: ((gamma((-1)/m) * ((1 - (2 ^ (1 /m)) / log(2))
(let [m-in-pow2 (mapv (comp long qu/pow2)
                      (range 25))
      alphams [0 0.2228396 0.3120160 0.3548907 0.3760327 0.3865412 0.3917811
               0.3943976 0.3957050 0.3963585 0.3966852 0.3968485 0.3969301 0.3969710
               0.3969914 0.3970016 0.3970066 0.3970084 0.3970086 0.3969941 0.3969818
               0.3969567 0.3980263 0.4001740 0.4090892]]
  (defn get-loglog-counter
    "A function to get a loglog counter,
     which can be later used to count unique items.
     n - Expected number of unique items.
         To be safe you can use total number of elements
         e.g. 10000000
     expected-error - You can fine tune loglog by providing error from
                      0.0001 - 0.9 i.e 0.01% to 90%
                      The bigger error you chose more space you save.
                      Usually 4% i.e. 0.04 is used with loglog counters."
    [n expected-error]
    (let [m-given (qu/square (/ 1.30 expected-error))
          m (find-closest m-given m-in-pow2)
          k (int (qu/log2 m))
          max-n (* m (qu/pow2 (qu/pow2 8)))]
      (assert (<= k 24) "K should be smaller than 24")
      (assert (<= n max-n) (str "Can not support given cardinality " n))
      (LogLogCounter. (byte-array m) n m k (alphams k)))))
