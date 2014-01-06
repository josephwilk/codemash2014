(ns codemash.graphs
  (:use [incanter core charts]))

(defn simple-plot
  "Creates a graph of `f` in the interval `x1` .. `x2`.
  Accepts an optional title."
  ([f x1 x2] (simple-plot f x1 x2 ""))
  ([f x1 x2 title]
     (view (function-plot f x1 x2 :title title))))

(defn plot-harmonics
  "Creates a graph of summing oscillator fn `f` over `n` octaves,
  in the interval `x1` .. `x2`."
  ([f n title]
     (plot-harmonics f n -10 10 title))
  ([f n x1 x2 title]
     (simple-plot
      (fn [x] (apply + (map-indexed f (repeat n x))))
      x1 x2 title)))
