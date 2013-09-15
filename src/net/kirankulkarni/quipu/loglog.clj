(ns ^{:doc "LogLog Implementation"
      :author "Kiran Kulkarni <kk@helpshift.com>"}
  net.kirankulkarni.quipu.loglog
  )


(defn square [x] (* x x))

(defn log2 [x] (/ (Math/log x) (Math/log 2)))

(defn pow-2 [x] (Math/pow 2 x))

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
          max-n (* m (pow-2 (pow-2 8)))]
      (assert (<= n max-n)))))
