(ns codemash.core
  "HELLO and welcome to the woderful world of SOUND.

  WARNING/DANGER:
    Its possible to generate sounds that may cause harm or damage to
    your ears if  you listen for to long.
    Ears are useful, volume down, avoid ear phones if possible.

  Musical Mission:
    * Create instruments new heard before.
    * Use instruments for greater good.
    * Bend/Manupliate instruments live.
    * Maybe Music...
"
  (:use overtone.live)
  (:require [codemash.graphs :as graph]))

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

;;Synthesis
;; Turning waves into complex sounds.
;; * Additive
;; * Subtractive

;; play{d=Duty;f=d.kr(1/[1,2,4],0,Dseq([0,3,7,12,17]+24,inf));GVerb.ar(Blip.ar(f.midicps*[1,4,8],LFNoise1.kr(1/4,3,4)).sum,200,8)}

(defsynth pop [freq 300]
  (let [src (sin-osc freq)]
    (out 0 src)))

(show-graphviz-synth pop)

(def p (pop))

(ctl p :freq 200)
(ctl p :freq 250)
(kill p)

;;UGENS - Unit generators (Sweet shop)

(defsynth ding [freq 880 dur 0.2 level 0.25 pan 0.0 out-bus 0]
  (let [amp  (env-gen:ar (env-perc) :action FREE :time-scale dur)
        snd (* (sin-osc:ar freq ) amp level)]
    (out out-bus (pan2:ar snd pan))))

(defsynth tick [freq 880 dur 0.1 level 0.25 pan 0.0 out-bus 0]
  (let [amp (env-gen (env-perc) :action FREE :time-scale dur)
        snd (lpf:ar (white-noise:ar) freq)]
    (out out-bus (pan2:ar (* snd amp level) pan))))

(defsynth woody-beep [freq 300 out-bus 0 dur 0.4]
  (let [tri (* 0.5 (lf-tri:ar freq))
        sin (sin-osc:ar (* 1 freq))
        sin2 (sin-osc:ar (* 1.01 freq))
        wood (bpf:ar (* (white-noise:ar) (line:kr 5 0 0.02)) freq 0.02)
        src (mix [sin sin2 tri wood])
        src (free-verb src)
        env (env-gen:ar (env-perc :release 0.25) :action FREE :time-scale dur)]
    (out out-bus (pan2 (* env src)))))

(woody-beep :freq 400)

(kill woody-beep)
(show-graphviz-synth ding)

(ding)
(tick)

;;Samples

(def subby-s (freesound-sample))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Busses - Wiring synths ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;Audio busses are just ids preallocated at boot.
(defonce tri-bus (audio-bus))
(defonce sin-bus (audio-bus))

(println sin-bus)

;;Triangle wave
(defsynth tri-synth [out-bus 0 freq 5] (out:kr out-bus (lf-tri:kr freq)))
;;Sin wave
(defsynth sin-synth [out-bus 0 freq 5] (out:kr out-bus (sin-osc:kr freq)))

(defonce main-g  (group "main"))
(defonce early-g (group "early" :head main-g))
(defonce late-g  (group "late"  :after early-g))

(def tri-synth-inst (tri-synth [:tail early-g] :out-bus tri-bus))
(def sin-synth-inst (sin-synth [:tail early-g] :out-bus sin-bus))

(defsynth modulated-freq-tri [freq-bus 0 mid-freq 220 freq-amp 55]
  (let [freq (+ mid-freq (* (in:kr freq-bus) freq-amp))]
    (out 0 (pan2 (lf-tri freq)))))

(comment
  (def mft (modulated-freq-tri [:tail late-g] sin-bus)))

(comment
  (ctl mft :freq-bus sin-bus))

(comment
  (ctl tri-synth-inst :freq 0.5))

(comment
  (do
    (kill mft))
  )

;;;;;;;;;;;;;;;
;;Instruments;;
;;;;;;;;;;;;;;;

;;Sampled
(comment (require 'overtone.inst.sampled-piano))
(require '[overtone.inst.piano :as piano])

(require '[overtone.inst.sampled-piano :as s-piano])

(piano/piano :note 50)
(s-piano/sampled-piano :note 50)

;;Processing GUI
(require '[quil.core :as q :refer [defsketch]])

;;;;;;;;;;
;;Timing;;
;;;;;;;;;;

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

(defonce root-trigger (trigger [:head timing-g] :rate 100 :in-bus root-trigger-bus))
(defonce root-count   (counter [:after root-trigger] :in-bus root-trigger-bus :out-bus root-count-bus))
(defonce beat-trigger (divider [:after root-trigger] :div current-beat :in-bus root-trigger-bus :out-bus beat-trigger-bus))
(defonce beat-count   (counter [:after beat-trigger] :in-bus beat-trigger-bus :out-bus beat-count-bus))
(defsynth get-beat [] (send-trig (in:kr beat-trigger-bus) count-trigger-id (+ (in:kr beat-count-bus) 1)))

(defonce beat (get-beat [:after beat-count]))

(require '[overtone.inst.drum :as drum])
(on-trigger count-trigger-id (fn [x] (drum/kick)) ::beat-watch)

(ctl beat-trigger :div 10)
(ctl root-trigger :rate 100)

(comment (remove-event-handler ::beat-watch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Building Sequencer with buffers;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def beats-g (group "beats"))

(def kick-s (load-sample "~/Workspace/music/samples/sliced-p5/kick.aif"))

(defonce kick-sequencer-buffer (buffer 8))

(sample-player kick-s)

(defsynth mono-sequencer
  [buf 0 rate 1 out-bus 0 beat-num 0 sequencer 0 numsteps 8 amp 1]
  (let [cnt      (in:kr beat-count-bus)
        beat-trg (in:kr beat-trigger-bus)
        bar-trg  (and (buf-rd:kr 1 sequencer cnt)
                      (= beat-num (mod cnt numsteps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out
     out-bus (* vol
                amp
                (pan2
                 (rlpf
                  (scaled-play-buf 1 buf rate bar-trg)
                  (demand bar-trg 0 (dbrown 200 20000 50 INF))
                  (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))


(def kicks
  (doall
   (for [x (range 8)]
     (mono-sequencer [:tail beats-g] :buf kick-s :beat-num x
                     :sequencer kick-sequencer-buffer))))

(buffer-write! kick-sequencer-buffer [1 0 0 0 0 1 0 0])

(defonce tom-sequencer-buffer (buffer 8))
(defonce tom-s (load-sample "~/Workspace/music/samples/sliced-p5/tom.aif"))

(def toms
  (doall
   (for [x (range 8)]
     (mono-sequencer [:tail beats-g] :buf tom-s :beat-num x
                     :sequencer tom-sequencer-buffer))))

(buffer-write! tom-sequencer-buffer [0 0 1 0 0 0 1 0])

(defonce shake-sequencer-buffer (buffer 8))
(defonce shake-s (load-sample "~/Workspace/music/samples/sliced-p5/shaker.aif"))

(def shakes
  (doall (for [x (range 8)]
           (mono-sequencer [:tail beats-g] :buf shake-s :beat-num x
                           :sequencer shake-sequencer-buffer))))

(buffer-write! shake-sequencer-buffer [0 1 0 0 0 0 0 0])

(defonce shake2-s (load-sample "~/Workspace/music/samples/sliced-p5/double-shake.aif"))
(defonce shake2-sequencer-buffer (buffer 8))

(def shakes2
  (doall
   (for [x (range 8)] (mono-sequencer [:tail beats-g] :buf shake2-s :beat-num x
                                      :sequencer shake2-sequencer-buffer))))

(buffer-write! shake2-sequencer-buffer [0 0 0 1 0 0 0 0])

(defonce shake2d-s (load-sample "~/Workspace/music/samples/sliced-p5/double-shake-deep.aif"))
(defonce shake2d-sequencer-buffer (buffer 8))

(def shakes2d
  (doall
   (for [x (range 8)] (mono-sequencer [:tail beats-g] :buf shake2d-s :beat-num x
                     :sequencer shake2d-sequencer-buffer))))

(buffer-write! shake2d-sequencer-buffer [0 0 0 0 0 0 0 1])


(defonce shake1-s      (load-sample "~/Workspace/music/samples/sliced-p5/single-shake.aif"))

(defonce shake1-sequencer-buffer (buffer 8))

(def shakes1
  (doall
   (for [x (range 8)] (mono-sequencer [:tail beats-g] :buf shake1-s :beat-num x
                     :sequencer shake1-sequencer-buffer))))

(buffer-write! shake1-sequencer-buffer [0 0 0 0 1 0 0 0])


(stop)

;;;;;;;;;;;;;;;;;;;;;;;
;; Timing and Buffers;;
;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;
;; Timing AND Buffers;;
;;;;;;;;;;;;;;;;;;;;;;;

(def score-b         (buffer 128))
(def duration-b      (buffer 128))
(def bass-duration-b (buffer 128))
(def bass-notes-b    (buffer 128))

(def score [:F4 :F4 :F4 :F4 :F4 :F4 :F4
            :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4 :G4
            :BB4 :BB4 :BB4 :BB4 :BB4 :BB4
            :D#4 :D#4 :D#4])

(def duration     [1/7])

(def score (concat
            (map #(+ -5  (note %)) score)
            (map #(+ -5  (note %)) score)
            (map #(+ -10 (note %)) score)
            (map #(+ -5  (note %)) score)
            (map #(+ -1  (note %)) score)))

(defsynth woody-beep [duration-bus 0 beat-count-bus 0 offset-bus 0 amp 1 out-bus 0]
  (let [cnt    (in:kr beat-count-bus)
        offset (buf-rd:kr 1 offset-bus cnt)
        durs   (buf-rd:kr 1 duration-bus cnt)
        trig (t-duty:kr (dseq durs INFINITE))
        freq (demand:kr trig 0 (drand offset INFINITE))
        freq (midicps freq)

        env (env-gen:ar (env-asr :release 0.25 :sustain 0.8) trig)
        tri (* 0.5 (lf-tri:ar freq))
        sin (sin-osc:ar (* 1 freq))
        sin2 (sin-osc:ar (* 1.01 freq))
        wood (bpf:ar (* (white-noise:ar) (line:kr 5 0 0.02)) freq 0.02)
        src (mix [sin sin2 tri wood])
        src (free-verb src)]
    (out:ar out-bus (* amp env (pan2 src)))))

    (def w  (woody-beep :duration-bus duration-b :beat-count-bus beat-count-bus :offset-bus score-b :amp 4))

(defsynth deep-saw [freq 100 beat-count-bus 0 offset-bus 0 duration-bus 0 out-bus 0 amp 1 pan 0]
  (let [cnt    (in:kr beat-count-bus)
        offset (buf-rd:kr 1 offset-bus cnt)
        durs   (buf-rd:kr 1 duration-bus cnt)
        trig (t-duty:kr (dseq durs INFINITE))
        freq (demand:kr trig 0 (drand offset INFINITE))
        freq (midicps freq)

        saw1 (lf-saw:ar (* 0.5 freq))
        saw2 (lf-saw:ar (* 0.25 freq))
        sin1 (sin-osc freq)
        sin2 (sin-osc (* 1.01 freq))
        src (mix [saw1 saw2 sin1 sin2])
        env (env-gen:ar (env-asr) trig)
        src (lpf:ar src)
        src (free-verb src 0.33 1 1)]
    (out out-bus (* amp [src src]))))

(def ps (deep-saw 100 :duration-bus bass-duration-b :beat-count-bus beat-count-bus :offset-bus bass-notes-b :amp 0.9))

(ctl ps :damp 1)
(ctl ps :room 1)

(kill ps)

(buffer-write! bass-duration-b (take 32 (cycle [(/ 1 3.5)])))
(buffer-write! bass-notes-b (take 32 (cycle (map note [:F2 :F2 :G3 :G2 :G3 :BB2 :BB2 :G2 :G2]))))

(buffer-write! bass-notes-b (take 32 (cycle (map #(+ 0 (note %)) [:F2 :F2 :G3 :G2 :G3 :BB2 :BB2 :G2 :G2]))))

(buffer-write! score-b (take 128 (cycle (map note score))))
(buffer-write! duration-b (take 128 (cycle duration)))

(ctl root-trigger :rate 100)
(ctl beat-trigger :div 30)

;;Hardware (External devices)

(midi-connected-devices)

(on-event [:midi :note-on]
          (fn [m]
            (buffer-write! music-buffer (map midi->hz
                                             (map (fn [midi-note] (+ -12 midi-note))
                                                  (repeat 256 (:note m))))))
          ::phat-bass-keyboard)

(comment
  (remove-event-handler ::drumkit))

;;Mouse

(defsynth spacey [out-bus 0 amp 1]
  (out out-bus (* amp (g-verb (blip (mouse-y 24 48) (mouse-x 1 100)) 200 8))))

(spacey)
(kill spacey)
