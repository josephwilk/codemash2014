(ns codemash.score
  "Score taken from Meta-ex: https://github.com/meta-ex/ignite"
  (:use [overtone.live]))

(def repetition-sub-a (map note [:C5, :A3, :B4, :A3, :C5, :E5, :A3, :A4, :C5, :A3, :B4, :A3, :C5, :A4]))
(def repetition-a (concat (map note [:A4, :A3]) repetition-sub-a (map note [:A3, :A4]) repetition-sub-a))

(def repetition-b  (map note [:F4, :F4, :A4, :F4, :G4, :F4, :A4, :C5, :F4, :F4, :A4, :F4, :G4, :F4, :A4, :F4]))

;; slight variation of the above with different distances between the 2nd and 3rd note
(def repetition-b3 (map note [:E4, :E4, :G4, :E4, :F#3, :E4, :G4, :B4, :E4, :E4, :G4, :E4, :F#3, :E4, :G4, :E4]))

(defn transpose [updown notes]
  (map #(+ updown %1) notes))

(def theme  (concat
             repetition-a
             (transpose -5 repetition-a)
             repetition-a
             (transpose -5 repetition-a)
             repetition-b
             (transpose 2 repetition-b)
             (transpose -2 repetition-b3)
             repetition-b3
             repetition-b
             (transpose 2 repetition-b)
             repetition-b3
             repetition-b3))

(def score (concat
            (concat (drop-last theme) [(note :A4)])
            theme
            (concat (drop-last theme) [(note :A4)])
            (concat (drop-last theme) [(note :A4)])))



(require '[codemash.timing :as time])

(defonce rhythm-g (group "Rhythm" :after time/timing-g))
(defonce music-buffer (buffer 256))

(def phasor-b (control-bus))
(def saw-x-b1 (control-bus))
(def saw-x-b2 (control-bus))

(defonce saw-s1 (time/saw-x [:head rhythm-g] :out-bus saw-x-b1))
(defonce saw-s2 (time/saw-x [:head rhythm-g] :out-bus saw-x-b2))

(def phasor-s (time/buf-phasor [:after saw-s1] saw-x-b1 :out-bus phasor-b :buf music-buffer))

(defsynth fuzzy-beep [attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0]
  (let [freq (/ (in:kr phasor-b) 2)
        env  (env-gen (env-lin attack sustain release) 1 1 0 1)
        src  (mix (saw [freq (* 1.01 freq)]))
        src  (lpf src (mouse-y 100 20000))
        sin  (sin-osc (* 1 freq))
        sin2 (sin-osc freq)
        src  (mix [src sin sin2])]
    (out out-bus (pan2 (* src amp)))))

(defsynth beepy [amp 1 out-bus 0]
  (let [freq   (* (in:kr phasor-b) 1)
        ct-saw (+ (lin-lin (in:kr saw-x-b2) 0 1 0.5 1))]
    (out out-bus (* 0.5  ct-saw amp 1.25 (mix (+ (lf-tri [(* 0.5 freq)
                                                          (* 0.25 freq)
                                                          (* 0.5 freq)
                                                          (* 2.01 freq)])))))))

(def hi   (beepy      [:head rhythm-g] :amp 0 :out-bus 0))
(def mid  (fuzzy-beep [:head rhythm-g] :amp 0 :out-bus 0))

(def score (map note [:C5 :A3 :B4 :A3 :C5 :E5 :A3 :A4 :C5 :A3 :B4 :A3 :C5 :A4]))

(buffer-write! music-buffer (take 256 (cycle (map midi->hz codemash.score/score))))

(ctl time/root-s :rate 1/32)

(ctl hi :amp 1)
(ctl mid :amp 1)

(kill hi)
(kill mid)
(stop)
