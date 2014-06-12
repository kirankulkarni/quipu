# Quipu

A Clojure library which implements a bunch of Probabilistic data structures to count cardinality of a set. Most common use of this kind of structure is to find out "unique visitors".

## Installation
For Leiningen project use

```clojure
[net.kirankulkarni/quipu "0.1.0"]
```

## Counting Methods
   Currently this library supports two counting methods Linear
   Counting and LogLog Counting.

### Linear Counting
As the name suggests space required for this algorithm is linearly proportional to actual cardinality. It gives cardinality with 1% error at most.

Please refer the original paper:
[Kyu-Young Whang , Brad T. Vander-Zanden , Howard M. Taylor, A linear-time probabilistic counting algorithm for database applications, ACM Transactions on Database Systems (TODS), v.15 n.2, p.208-229, June 1990](http://dl.acm.org/citation.cfm?id=78925&CFID=359353900&CFTOKEN=83197792)

#### Usage
You can create a linear counter by passing expected cardinality of the set as an argument. If there is no way of knowing expected cardinality then use number of elements (including duplicates) to create a counter.

In following example we will create a linear counter for 1,000 unique elements. We create a loop which runs 10,000 times each time inserting a number from 0-999. At the end we will get cardinality of counter

```clojure
user> (require '[net.kirankulkarni.quipu.core :refer :all])
nil
user> (require '[net.kirankulkarni.quipu.linear :refer [get-linear-counter]])
nil
user> (let [counter (get-linear-counter 1000)
            elements (mapv identity (range 0 1000))]
        (println "byts used" (size counter))
        (doseq [n (range 10000)]
          (add counter (get elements (mod n 1000))))
        (get-card counter))

bytes used 666
999
```

It returned 999 which is estimated cardinality. It used 666 Bytes i.e. 0.6KB for 1,000 elements

### LogLog Counting
Space required for this algorithm is logarithmically proportional to actual cardinality. This algorithm works with buckets of bits. Unlike Linear counter here you can tune expected error (which range from (0.01% to 90%). Generally with loglog counters 4% expected error is used

Please refer the original paper:
[Loglog counting of large cardinalities - Durand, Flajolet - 2003](http://algo.inria.fr/flajolet/Publications/DuFl03-LNCS.pdf)

#### Usage
You can create a loglog counter by passing exepcted cardinality and expected error rate.
We will use similar example we used in Linear Counting

In following example we will create a linear counter for 10,000 unique elements. We create a loop which runs 100,000 times each time inserting a number from 0-9999. At the end we will get cardinality of counter

```clojure
user> (require '[net.kirankulkarni.quipu.core :refer :all])
nil
user> (require '[net.kirankulkarni.quipu.loglog :refer [get-loglog-counter]])
nil
user> (let [counter (get-loglog-counter 10000 0.04)
            elements (mapv identity (range 0 10000))]
        (println "bytes used" (size counter))
        (doseq [n (range 100000)]
          (add counter (get elements (mod n 10000))))
        (int (get-card counter)))
bytes used 640
10009
```

As we see it returned 10009 as estimated cardinality. It used 640 bytes i.e. 0.6KB for 10,000 elements
## Performance
I calculated number of unique words from [The Complete Work of Shakespeare](http://www.gutenberg.org/ebooks/100.txt.utf-8)
It has total `1,410,671` words (including duplicates). I created linear and logical counter using this number, and for loglog counting I am using 4% error. Here are the results of the same

Actual count of unique words is : `59,724`

<table>
    <tr>
    <th>Method</th><th>Estimated Count</th><th>Bytes Used</th>
    </tr>
    <tr>
    <td>Clojure Set</td><td>59,724</td><td>5,421,192 ~ 5MB</td>
    </tr>
    <tr>
    <td>Linear Counting</td><td>59,868</td><td>25,439 ~ 24KB</td>
    </tr>
    <tr>
    <td>Loglog Counting</td><td>59,774</td><td>640 ~ 0.6 KB</td>
    </tr>
</table>

## License

Copyright © 2013 Kiran Kulkarni

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
