(ns cn.academy.tech-system.energy-system.visualization
  (:require [clojure.string :as str]))

(defn- normalize-values [values height]
  (let [min-val (apply min values)
        max-val (apply max values)
        range (- max-val min-val)]
    (if (zero? range)
      (repeat (count values) (quot height 2))
      (map #(int (* height (/ (- % min-val) range))) values))))

(defn- create-sparkline [values width height]
  (let [step (max 1 (quot (count values) width))
        sampled (take width (take-nth step values))
        normalized (normalize-values sampled height)
        chars ["█" "▇" "▆" "▅" "▄" "▃" "▂" "▁" " "]
        lines (for [y (range height -1 -1)]
               (str/join (for [v normalized]
                          (if (>= v y) "█" " "))))]
    (str/join "\n" lines)))

(defn create-line-chart [title values width height & {:keys [labels]}]
  (let [chart (create-sparkline values width height)
        title-line (str "┌" (str/join (repeat (- width 2) "─")) "┐")
        title-text (if (> (count title) (- width 4))
                    (str (subs title 0 (- width 7)) "...")
                    title)
        centered-title (str "│ " 
                          (str/join (repeat (quot (- width (+ 3 (count title-text))) 2) " "))
                          title-text
                          (str/join (repeat (- width 4 
                                             (quot (- width (+ 3 (count title-text))) 2)
                                             (count title-text)) " "))
                          " │")
        bottom-line (str "└" (str/join (repeat (- width 2) "─")) "┘")
        chart-lines (map #(str "│" % "│") (str/split-lines chart))
        label-line (when labels
                    (let [label-width (quot width (count labels))
                          formatted (map #(format (str "%-" label-width "s") 
                                               (if (> (count %) (dec label-width))
                                                 (str (subs % 0 (- label-width 2)) "..")
                                                 %))
                                      labels)]
                      (str "│" (str/join formatted) (str/join (repeat (- width 2 (* label-width (count labels))) " ")) "│")))]
    (str/join "\n"
              (concat [title-line
                      centered-title]
                     chart-lines
                     [(or label-line bottom-line)
                      (when label-line bottom-line)]))))

(defn create-bar-chart [title categories values width height]
  (let [max-val (apply max values)
        bar-width (quot (- width 4) (count values))
        scale (/ (dec height) (or max-val 1))
        bar-heights (map #(int (* % scale)) values)
        bars (for [h bar-heights]
               (str/join "\n"
                        (concat
                          (repeat (- height h) (str/join (repeat bar-width " ")))
                          (repeat h (str/join (repeat bar-width "█"))))))
        chart (->> bars
                  (apply map str)
                  (map #(str "│ " % " │"))
                  (str/join "\n"))
        title-line (str "┌" (str/join (repeat (- width 2) "─")) "┐")
        title-text (if (> (count title) (- width 4))
                    (str (subs title 0 (- width 7)) "...")
                    title)
        centered-title (str "│ " 
                          (str/join (repeat (quot (- width (+ 3 (count title-text))) 2) " "))
                          title-text
                          (str/join (repeat (- width 4 
                                             (quot (- width (+ 3 (count title-text))) 2)
                                             (count title-text)) " "))
                          " │")
        bottom-line (str "└" (str/join (repeat (- width 2) "─")) "┘")
        labels (for [cat categories]
                (let [cat-text (if (> (count cat) bar-width)
                               (str (subs cat 0 (- bar-width 2)) "..")
                               cat)]
                  (format (str "%-" bar-width "s") cat-text)))
        label-line (str "│ " (str/join labels) " │")]
    (str/join "\n"
              [title-line
               centered-title
               chart
               label-line
               bottom-line])))

(defn format-legend [items width]
  (let [item-width (quot width (count items))
        formatted (map (fn [[label value]]
                        (format (str "%-" item-width "s") 
                               (str label ": " value)))
                      items)]
    (str/join (take width (str/join formatted)))))