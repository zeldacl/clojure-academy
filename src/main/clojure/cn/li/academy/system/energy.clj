(ns cn.li.academy.system.energy)

(def node-type [{
                 :name "basic",
                 :max-energy 10000,
                 :latency 20,
                 :range 9,
                 :capacity 5
                 },
                {
                 :name "standard",
                 :max-energy 50000,
                 :latency 40,
                 :range 12,
                 :capacity 10
                 },
                {
                 :name "advanced",
                 :max-energy 300000,
                 :latency 100,
                 :range 19,
                 :capacity 20
                 }])


