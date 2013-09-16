(ns ^{:doc "LogLog Implementation"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
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


(defn- add-elem
  [bytearray k elem]
  (let [elem-hash (qu/murmurhash-32 elem)
        j (>>> elem-hash (- Integer/SIZE k))
        r (+ 1
             (Integer/numberOfLeadingZeros
              (bit-or (bit-shift-left elem-hash k)
                      (bit-shift-left 1 (dec k)))))]
    (when (< (aget bytearray j) r)
      (aset-byte bytearray j r)
      true)))


(defn find-closest
  [x coll]
  (first
   (first
    (sort-by second
             (mapv (fn [val]
                     [val (Math/abs (- val x))])
                   coll)))))


(let [m-in-pow2 (mapv (comp long #(Math/pow 2 %))
                      (range 1 17))]
  (defn get-loglog-counter
    [n expected-error]
    (let [m-given (square (/ 1.30 expected-error))
          m (find-closest m-given m-in-pow2)
          k (int (log2 m))
          max-n (* m (pow2 (pow2 8)))]
      (assert (<= k 24) "K should be smaller than 24")
      (assert (<= n max-n) (str "Can not support given cardinality " n)))))
