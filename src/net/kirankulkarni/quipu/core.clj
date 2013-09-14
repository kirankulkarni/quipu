(ns ^{:doc "Core Protocol"
      :author "Kiran Kulkarni <kk.questworld@gmail.com>"}
  net.kirankulkarni.quipu.core)


(defprotocol IProbCountingSet
  (add [this elem])
  (get-card [this])
  (clear [this])
  (size [this])
  (get-bitset [this]))
