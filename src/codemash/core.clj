(ns codemash.core
  "Welcome to the world of Sound.
  Part 1"
  (:use overtone.live))

;;Sound

(demo (sin-osc))
(demo (lf-saw))

(demo (pulse))
(demo (lf-pulse:ar))

(demo (square))
(demo (lf-tri))

;;Synthesis

(defsynth pop [freq 300]
  (let [sin1 (* 1.1 (sin-osc freq))
        sin2 (sin-osc freq)
        sin3 (* 0.9 (sin-osc freq))
        src (mix [sin1 sin2 sin3])]
    (out [0] (pan2 src))))

(def p (pop))
(kill pop)

;;Instruments

;;Perfoming live: Buffers

(def b (buffer 256))

;;Samples

;;Hardware (External devices)
