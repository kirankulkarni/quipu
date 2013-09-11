(ns ^{:doc "Portocols"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.protocols)


(defprotocol IByteArray
  (to-byte-array [this]))


(extend-protocol IByteArray
  nil
  (to-byte-array [x] (byte-array 0))

  java.lang.String
  (to-byte-array [s] (.getBytes s "utf-8"))

  java.lang.Short
  (to-byte-array [short-int] (.. (java.nio.ByteBuffer/allocate 2)
                                 (putShort short-int)
                                 array))

  java.lang.Integer
  (to-byte-array [integer] (.. (java.nio.ByteBuffer/allocate 4)
                               (putInteger integer)
                               array))

  java.lang.Long
  (to-byte-array [long-int] (.. (java.nio.ByteBuffer/allocate 8)
                                (putLong long-int)
                                array))

  java.nio.ByteBuffer
  (to-byte-array [byte-buffer] (.array byte-buffer))

  clojure.lang.Keyword
  (to-byte-array [k] (.getBytes (name k) "utf-8"))

  clojure.lang.IPersistentList
  (to-byte-array [l] (.getBytes (binding [*print-dup* false]
                                  (pr-str l))
                                "utf-8"))

  clojure.lang.APersistentVector
  (to-byte-array [v] (.getBytes (binding [*print-dup* false]
                                  (pr-str v))
                                "utf-8"))
  clojure.lang.APersistentMap
  (to-byte-array [m] (.getBytes (binding [*print-dup* false]
                                  (pr-str m))
                                "utf-8"))

  clojure.lang.APersistentSet
  (to-byte-array [s] (.getBytes (binding [*print-dup* false]
                                  (pr-str s))
                                "utf-8")))


;; Anyone will wonder why this has been kept seperate?
;; Reason: it is tricky to include this in extend-protocol macro.
;; extend-protocol groups things by splitting them into seqs and symbols.
;; (Class/forName "[B") is a seq, so it's treated as implementation of a protocol
;; function, not a class. However if you keep it as first thing it works
;; e.g. This will work:
;; (extend-protocol IByteArray
;;    (Class/forName "[B")
;;    (to-byte-array [ba] ba)
;;    java.lang.String
;;    (to-byte-array [s] (.getBytes s)))
;;
;; But this won't
;; (extend-protocol IByteArray
;;    java.lang.String
;;    (to-byte-array [s] (.getBytes s))
;;    (Class/forName "[B")
;;    (to-byte-array [ba] ba))
;;
;; To avoid any unnecearry problems write and maintain it seperately
;; Refer: https://groups.google.com/forum/#!topic/clojure/cioMCdArsKw
(extend-type (Class/forName "[B")
  IByteArray
  (to-byte-array [ba]
    ba))


(defprotocol IProbCountingSet
  (add [this elem])
  (get-card [this])
  (clear [this])
  (size [this]))
