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
