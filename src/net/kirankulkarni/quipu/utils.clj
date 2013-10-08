(ns ^{:doc "Utilities"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.utils
  (:require [net.kirankulkarni.quipu.protocols :refer [to-byte-array]])
  (:import (com.google.common.hash Hashing)))


(defn ^Integer murmurhash-32
  "Use Guava's murmur3 Hash function to get hascode "
  [o & {:keys [seed] :or {seed 0}}]
  (.. (Hashing/murmur3_32 seed)
      newHasher
      (putBytes (to-byte-array o))
      hash
      hashCode))


(defn ^Long  unsigned-murmurhash-32
  "In most places in this project we need unsigned hash-code.
   Note that this returns a `Long` number."
  [o]
  (bit-and (long (murmurhash-32 o))
           (long 0xFFFFFFFF)))


;;; y = y_0 + ((y_1-y_0) * ({x - x_0}/{x_1-x_0}))
(defn lerp
  "Implements linear interpolation"
  [x0 y0 x1 y1 x]
  (long (Math/ceil
         (+ y0 (* (- y1 y0) (/ (- x x0) (- x1 x0)))))))



(defn square [x] (* x x))

(defn log2 [x] (/ (Math/log x) (Math/log 2)))

(defn pow2 [x] (Math/pow 2 x))

(defn >>> [v bits] (bit-shift-right (bit-and 0xFFFFFFFF v) bits))
