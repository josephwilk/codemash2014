(ns codemash.core
  "HELLO and welcome to the woderful world of SOUND.

  WARNING/DANGER:
    Its possible to generate sounds that may cause harm or damage to
    your ears if  you listen for to long.
    Ears are useful, volume down, avoid ear phones if possible.

  Mission:
    * Create instruments
    * Use instruments
    * Manupliate instruments live
    * Maybe Music?
"
  (:use overtone.live))

(use 'overtone.live)
(odoc demo)

;;;;;;;;;;;;;;;;;;;;;;;;;
;;     Waves           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(demo (sin-osc))
(demo (lf-saw))
(demo (pulse))
(demo (lf-pulse:ar))

(demo (square))
(demo (lf-tri))

(graph/plot-harmonics sin-osc "Sin")

;;Synthesis
;; Turning waves into complex sounds.
;; * Additive
;; * Subtractive

;; play{d=Duty;f=d.kr(1/[1,2,4],0,Dseq([0,3,7,12,17]+24,inf));GVerb.ar(Blip.ar(f.midicps*[1,4,8],LFNoise1.kr(1/4,3,4)).sum,200,8)}

(defsynth pop [freq 300]
  (let [sin1 (* 1.1 (sin-osc freq))
        sin2 (sin-osc freq)
        sin3 (* 0.9 (sin-osc freq))
        src (mix [sin1 sin2 sin3])]
    (out [0] (pan2 src))))

(def p (pop))

(ctl p :freq 200)
(ctl p :freq 250)
(kill p)

;;UGENS - Unit generators (Sweet shop)

(defsynth foo [freq 200 dur 0.5]
  (let [src (saw [freq (* freq 1.01) (* 0.99 freq)])
        low (sin-osc (/ freq 2))
        filt (lpf src (line:kr (* 10 freq) freq 10))
        env (env-gen (perc 0.1 dur) :action FREE)]
    (out 0 (pan2 (* 0.8 low env filt)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Busses - Wiring synths         ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce tri-bus (audio-bus)) ;;Audio busses are just ids preallocated at boot.
(defonce sin-bus (audio-bus))

(println sin-bus)

;;Triangle wave
(defsynth tri-synth [out-bus 0 freq 5] (out:kr out-bus (lf-tri:kr freq)))
;;Sin wave
(defsynth sin-synth [out-bus 0 freq 5] (out:kr out-bus (sin-osc:kr freq)))

(defonce main-g  (group "main"))
(defonce early-g (group "early" :head main-g))
(defonce lateroot-g (group "late"  :after early-g))

(def tri-synth-inst (tri-synth [:tail early-g] tri-bus))
(def sin-synth-inst (sin-synth [:tail early-g] sin-bus))

(defsynth modulated-vol-tri [vol-bus 0 freq 220]
  (out 0 (pan2 (* (in:kr vol-bus) (lf-tri freq)))))

(defsynth modulated-freq-tri [freq-bus 0 mid-freq 220 freq-amp 55]
  (let [freq (+ mid-freq (* (in:kr freq-bus) freq-amp))]
    (out 0 (pan2 (lf-tri freq)))))

(comment
  (def mvt (modulated-vol-tri [:tail lateroot-g] sin-bus))
  (def mft (modulated-freq-tri [:tail lateroot-g] sin-bus)))

(comment
  (ctl mft :freq-bus tri-bus))

(comment
  (ctl tri-synth-inst :freq 0.5))

(comment
  (ctl mvt :vol-bus tri-bus))

(comment
  (do
    (kill mft)
    (kill mvt)))

(comment
  (do
    (ctl tri-synth-inst :freq 5)
    (ctl sin-synth-inst :freq 5)
    (def mft-2 (modulated-freq-tri [:tail lateroot-g] sin-bus 220 55))
    (def mft-3 (modulated-freq-tri [:tail lateroot-g] tri-bus 220 55)))
  (ctl sin-synth-inst :freq 4)
  (kill mft-2 mft-3))

;;;;;;;;;;;;;;;
;;Instruments;;
;;;;;;;;;;;;;;;

;;Sampled
(comment (require 'overtone.inst.sampled-piano))
(require '[overtone.inst.piano])

;;Processing GUI
(require '[quil.core :as q :refer [defsketch]])

;;;;;;;;;;
;;Timing;;
;;;;;;;;;;

(require '[overtone.synth.timing :as timing])

(defonce timing-g (group "codemash timing" :tgt (foundation-safe-pre-default-group)))

(defonce root-trigger-bus (control-bus))
(defonce root-count-bus   (control-bus))
(defonce beat-trigger-bus (control-bus))
(defonce beat-count-bus   (control-bus))

(defonce count-trigger-id (trig-id))

(def current-beat 30)

(defsynth trigger [rate 100 out-bus 0]
  (out:kr out-bus (impulse:kr rate)))

(defsynth counter [in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-count:kr (in:kr in-bus))))

(defsynth divider [div 32 in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-divider (in:kr in-bus) div)))

(defonce root-count   (counter [:head timing-g] :in-bus root-trigger-bus :out-bus root-count-bus))
(defonce root-trigger (trigger [:head timing-g] :rate 100 :in-bus root-trigger-bus))
(defonce beat-trigger (divider [:after root-count] :div current-beat :in-bus root-trigger-bus :out-bus beat-trigger-bus))
(defonce beat-count   (counter [:after beat-trigger] :in-bus beat-trigger-bus :out-bus beat-count-bus))
(defsynth get-beat [] (send-trig (in:kr beat-trigger-bus) count-trigger-id (+ (in:kr beat-count-bus) 1)))

(get-beat)

(on-trigger count-trigger-id (fn [x] (println x) (flush)) ::beat-watch)

(ctl beat-trigger :div 30)
(ctl root-trigger :rate 1)

(comment (remove-event-handler ::beat-watch))

(require '[codemash.timing :as time])

(defonce rhythm-g (group "Rhythm" :after time/timing-g))

(defonce music-buffer-1 (buffer 256))
(defonce music-buffer-2 (buffer 256))

(def phasor-b1 (control-bus))
(def phasor-b2 (control-bus))

(defonce saw-x-b1 (control-bus 1 "Timing Saw 1"))
(defonce saw-x-b2 (control-bus 1 "Timing Saw 2"))
(defonce saw-x-b3 (control-bus 1 "Timing Saw 3"))

(defonce saw-s1 (time/saw-x [:head rhythm-g] :out-bus saw-x-b1))
(defonce saw-s2 (time/saw-x [:head rhythm-g] :out-bus saw-x-b2))
(defonce saw-s3 (time/saw-x [:head rhythm-g] :out-bus saw-x-b3))

(defonce phasor-s1 (time/buf-phasor [:after saw-s1] saw-x-b1 :out-bus phasor-b1 :buf music-buffer-1))
(defonce phasor-s2 (time/buf-phasor [:after saw-s2] saw-x-b2 :out-bus phasor-b2 :buf music-buffer-2))

(defsynth foo [attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0]
  (let [freq (/ (in:kr phasor-b2) 2)
        env  (env-gen (env-lin attack sustain release) 1 1 0 1)
        src  (mix (saw [freq (* 1.01 freq)]))
        src  (lpf src (mouse-y 100 20000))
        sin  (sin-osc (* 1 freq))
        sin2 (sin-osc freq)
        src  (mix [src sin sin2])]
    (out out-bus (pan2 (* src amp)))))

(defsynth beepy [amp 1 out-bus 0]
  (let [freq   (* (in:kr phasor-b2) 1)
        ct-saw (+ (lin-lin (in:kr saw-x-b3) 0 1 0.5 1))]
    (out out-bus (* 0.5  ct-saw amp 1.25 (mix (+ (lf-tri [(* 0.5 freq)
                                                          (* 0.25 freq)
                                                          (* 0.5 freq)
                                                          (* 2.01 freq)])))))))

(defsynth foo-bass [lpf-f 1000 lpf-mul 1 amp 0 out-bus 0]
  (let [freq (/ (in:kr phasor-b1) 8)
        ct-saw (in:kr saw-x-b3)]
    (out out-bus (* amp (* 0.5 (* (+ 0.2 ct-saw)
                                  (lpf (sum [(sin-osc (/ freq))
                                             (sin-osc (/ freq 0.25))
                                             (square (* 2 freq))
                                             (saw freq)])
                                       (* lpf-mul ct-saw lpf-f))))))))

(def hi   (beepy    [:head rhythm-g] :amp 0 :out-bus 0))
(def mid  (foo      [:head rhythm-g] :amp 0 :out-bus 0))
(def bass (foo-bass [:head rhythm-g] :amp 0 :out-bus 0))

(def score (map note [:C5, :A3, :B4, :A3, :C5, :E5, :A3, :A4, :C5, :A3, :B4, :A3, :C5, :A4]))

(buffer-write! music-buffer-1 (take 256 (cycle score)))
(buffer-write! music-buffer-2 (take 256 (cycle score)))
(ctl time/root-s :rate 100)

(ctl hi :amp 1)
(ctl mid :amp 1)
(ctl bass :amp 1)

(kill hi)
(kill mid)
(kill bass)
(stop)
;;;;;;;;;;;;;;;;;;;;;;;;
;;Creating a Sequencer;;
;;;;;;;;;;;;;;;;;;;;;;;;

(def b (buffer 256))

;;Samples

;;Hardware (External devices)

(midi-connected-devices)

(on-event
 [:midi :note-on]
 (fn [e] (when-let [drum (drum-kit (:note e))] (drum)))
 ::drumkit)

(comment
  (remove-event-handler ::drumkit))

;;Mouse

(defsynth spacey [out-bus 0 amp 1]
  (out out-bus (* amp (g-verb (blip (mouse-y 24 48) (mouse-x 1 100)) 200 8))))

(spacey)
(kill spacey)
