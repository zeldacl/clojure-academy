(ns cn.li.academy.config)

(def node-config {
                  :basic {
                          :id 0
                          :name "basic"
                          :max-energy 15000
                          :bandwidth  150
                          :range      9
                          :capacity   5
                          }
                  :standard {
                             :id 1
                             :name "standard"
                             :max-energy 50000
                             :bandwidth  300
                             :range      12
                             :capacity   10
                             }
                  :advanced {
                             :id 2
                             :name "advanced"
                             :max-energy 200000
                             :bandwidth  900
                             :range      19
                             :capacity   20
                             }
                  })
