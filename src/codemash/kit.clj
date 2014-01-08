(ns codemash.kit
  "HERE LIES DEMONS.

 This is a mash of config and setup taken from:
https://github.com/stars-my-destination/cassiopeia
"
  (:use [overtone.live]
        [overtone.helpers.lib :only [uuid]]
        [codemash.core]
        [codemash.samples]
        [nano-kontrol2.config :only [mixer-init-state basic-mixer-init-state]])
  (:require [launchpad.plugin.metronome :as metronome]
            [launchpad.sequencer :as sequencer]
            [launchpad.plugin.beat :as beat]
            [launchpad.plugin.beat-scroll :as beat-scroll]
            [launchpad.plugin.sample-rows :as sr]

            [nano-kontrol2.core :as nk2]
            [nano-kontrol2.buttons :as btn]
            [launchpad.core :as lp-core]))

(defn nk-bank
  "Returns the nk bank number for the specified bank key"
  [bank-k]
  (case bank-k
    :master btn/record
    :lp64   btn/play
    ;;      btn/stop    ; stop
    :riffs  btn/fast-forward ; fast-forward
    :synths btn/rewind)) ; rewind

(def cfg
  {:synths {:s0 mixer-init-state :s1 mixer-init-state :s2 mixer-init-state :m0 mixer-init-state :m1 mixer-init-state :r0 mixer-init-state :r7 basic-mixer-init-state}
   :riffs  {:s0 mixer-init-state :s1 mixer-init-state :m0 mixer-init-state :m1 mixer-init-state :r7 basic-mixer-init-state}
   :master {:s7 mixer-init-state :m7 mixer-init-state :r7 mixer-init-state}
   :lp64 {
          ;;Beats
          :s0 ["lp64-0" mixer-init-state]
          :m0 ["lp64-1" mixer-init-state]
          :r0 ["lp64-2" mixer-init-state]
          :s1 ["lp64-3" mixer-init-state]
          :m1 ["lp64-4" mixer-init-state]
          :r1 ["lp64-5" mixer-init-state]
          :s2 ["lp64-6" mixer-init-state]

          :s3 ["lp64-triggers" mixer-init-state]
          :r7 ["lp64-master" basic-mixer-init-state]

          ;;Row mapped samples
          :s4 ["lp64-seq-0" mixer-init-state]
          :m4 ["lp64-seq-1" mixer-init-state]
          :r4 ["lp64-seq-2" mixer-init-state]
          :s5 ["lp64-seq-3" mixer-init-state]
          :m5 ["lp64-seq-4" mixer-init-state]
          :r5 ["lp64-seq-5" mixer-init-state]
          :s6 ["lp64-seq-6" mixer-init-state]
          :m6 ["lp64-seq-7" mixer-init-state]}})

(def banks
  {:master btn/record
   :lp64   btn/play
   :riffs  btn/fast-forward
   :synths btn/rewind})

(lp-core/boot!)
(nk2/start! banks cfg)



(defsynth basic-mixer [boost 0 amp 1 mute 1 in-bus 0 out-bus 0 clamp-down-t 0.05]
  (out out-bus (* (+ boost 1) amp (lag mute clamp-down-t) (in:ar in-bus 2))))

;;State to store the currently active nk mixers
(defonce korg-nano-kontrol-mixers (atom {}))

(defcgen wobble
  "wobble an input src"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8  wob)]
     wob)))

(defsynth meta-mix [rev-mix 0
                    delay-decay 0
                    dec-mix 0
                    wobble-mix 0
                    delay-mix 0
                    hpf-mix 0
                    wobble-factor 0
                    amp 1.5
                    rev-damp 0
                    rev-room 0
                    samp-rate 0
                    bit-rate 0
                    delay-rate 0
                    hpf-freq 2060
                    hpf-rq 1
                    pan 0
                    in-bus 10
                    delay-reset-trig [0 :kr]
                    out-bus 0]
  (let [
        ;;scale inputs accordingly
        wobble-factor (* wobble-factor 15)
        amp           (* amp 3)
        samp-rate     (* samp-rate 22000)
        bit-rate      (* bit-rate 32)
        hpf-freq      (mul-add hpf-freq 2000 60)
        pan           (- (* 2 pan) 1)
        num-samps     (* 2 44100)
        delay-buf     (local-buf num-samps)
        src           (in:ar in-bus 1)
        pos           (phasor:ar delay-reset-trig 1 0 (* delay-rate num-samps))
        old           (buf-rd:ar 1 delay-buf pos :loop true)
        delay-sig     (+ src (* delay-decay old))

        src           (+ (* (- 1 delay-mix) src)
                         (* delay-mix delay-sig))

        src           (+ (* (- 1 rev-mix) src)
                         (* rev-mix (free-verb src :mix 1 :room rev-room :damp rev-damp)))

        src           (+ (* (- 1 dec-mix) src)
                         (* dec-mix (decimator src samp-rate bit-rate)))

        src           (+ (* (- 1 hpf-mix) src)
                         (* hpf-mix (normalizer (rhpf src hpf-freq hpf-rq))))

        src           (+ (* (- 1 wobble-mix) src)
                         (* wobble-mix (wobble src wobble-factor)))]

    (buf-wr:ar [src] delay-buf pos :loop true)
    (out out-bus (pan2 (* amp src) pan))))

(defsynth mixer-sin-control
  [out-bus 0

   freq-mul-0 0
   phase-shift-0 0
   mul-0 0
   add-0 1
   amp-0 0

   freq-mul-1 0
   phase-shift-1 0
   mul-1 0
   add-1 1
   amp-1 0

   freq-mul-2 0
   phase-shift-2 0
   mul-2 0
   add-2 1
   amp-2 0

   freq-mul-3 0
   phase-shift-3 0
   mul-3 0
   add-3 1
   amp-3 0

   freq-mul-4 0
   phase-shift-4 0
   mul-4 0
   add-4 1
   amp-4 0

   freq-mul-5 0
   phase-shift-5 0
   mul-5 0
   add-5 1
   amp-5 0

   freq-mul-6 0
   phase-shift-6 0
   mul-6 0
   add-6 1
   amp-6 0

   freq-mul-7 0
   phase-shift-7 0
   mul-7 0
   add-7 1
   amp-7 0.5

   freq-mul-8 0
   phase-shift-8 0
   mul-8 0
   add-8 1
   amp-8 0

   freq-mul-9 0
   phase-shift-9 0
   mul-9 0
   add-9 1
   amp-9 0

   freq-mul-10 0
   phase-shift-10 0
   mul-10 0
   add-10 1
   amp-10 0

   freq-mul-11 0
   phase-shift-11 0
   mul-11 0
   add-11 1
   amp-11 0

   freq-mul-12 0
   phase-shift-12 0
   mul-12 0
   add-12 1
   amp-12 0

   freq-mul-13 0
   phase-shift-13 0
   mul-13 0
   add-13 1
   amp-13 0

   freq-mul-14 0
   phase-shift-14 0
   mul-14 0
   add-14 1
   amp-14 0

   freq-mul-15 0
   phase-shift-15 0
   mul-15 0
   add-15 1
   amp-15 0.5]

  (let [clk (in:kr 0)]
    (out:kr out-bus [(* amp-0 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-0 phase-shift-0))) 2) mul-0 add-0))
                     (* amp-1 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-1 phase-shift-1))) 2) mul-1 add-1))
                     (* amp-2 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-2 phase-shift-2))) 2) mul-2 add-2))
                     (* amp-3 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-3 phase-shift-3))) 2) mul-3 add-3))
                     (* amp-4 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-4 phase-shift-4))) 2) mul-4 add-4))
                     (* amp-5 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-5 phase-shift-5))) 2) mul-5 add-5))
                     (* amp-6 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-6 phase-shift-6))) 2) mul-6 add-6))
                     (* amp-7 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-7 phase-shift-7))) 2) mul-7 add-7))
                     (* amp-8 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-8 phase-shift-8))) 2) mul-8 add-8))
                     (* amp-9 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-9 phase-shift-9))) 2) mul-9 add-9))
                     (* amp-10 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-10 phase-shift-10))) 2) mul-10 add-10))
                     (* amp-11 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-11 phase-shift-11))) 2) mul-11 add-11))
                     (* amp-12 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-12 phase-shift-12))) 2) mul-12 add-12))
                     (* amp-13 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-13 phase-shift-13))) 2) mul-13 add-13))
                     (* amp-14 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-14 phase-shift-14))) 2) mul-14 add-14))
                     (* amp-15 (mul-add:kr (/ (+ 1 (sin  (mul-add clk freq-mul-15 phase-shift-15))) 2) mul-15 add-15))])))

(def nano2-fns {:slider0 (fn [v mixer-g] (ctl mixer-g :rev-mix v))
                :slider1 (fn [v mixer-g] (ctl mixer-g :delay-decay v))
                :slider2 (fn [v mixer-g] (ctl mixer-g :dec-mix v))
                :slider3 (fn [v mixer-g] (ctl mixer-g :wobble-mix v))
                :slider4 (fn [v mixer-g] (ctl mixer-g :delay-mix v))
                :slider5 (fn [v mixer-g] (ctl mixer-g :hpf-mix v))
                :slider6 (fn [v mixer-g] (ctl mixer-g :wobble-factor (scale-range v 0 1 0 15)))
                :slider7 (fn [v mixer-g] (ctl mixer-g :amp (scale-range v 0 1 0 3)))
                :pot0    (fn [v mixer-g] (ctl mixer-g :rev-damp v))
                :pot1    (fn [v mixer-g] (ctl mixer-g :rev-room v))
                :pot2    (fn [v mixer-g] (ctl mixer-g :samp-rate (* 22000 v)))
                :pot3    (fn [v mixer-g] (ctl mixer-g :bit-rate (* 32 v)))
                :pot4    (fn [v mixer-g] (ctl mixer-g :delay-rate v))
                :pot5    (fn [v mixer-g] (ctl mixer-g :hpf-freq (+ 60 (* 2000 v))))
                :pot6    (fn [v mixer-g] (ctl mixer-g :hpf-rq v))
                :pot7    (fn [v mixer-g] (ctl mixer-g :pan (scale-range v 0 1 -1 1)))})

(def nano2-fns {:slider0 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-0 v))
                :slider1 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-1 v))
                :slider2 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-2 v))
                :slider3 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-3 v))
                :slider4 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-4 v))
                :slider5 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-5 v))
                :slider6 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-6 v))
                :slider7 (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-7 v))
                :pot0    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-8 v))
                :pot1    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-9 v))
                :pot2    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-10 v))
                :pot3    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-11 v))
                :pot4    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-12 v))
                :pot5    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-13 v))
                :pot6    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-14  v))
                :pot7    (fn [v sin-ctl-s] (ctl sin-ctl-s :amp-15 v))})

(def nano2-controls (keys nano2-fns))

(defn- mk-mixer
  [event-k mixer-g out-bus]
  (let [in-bus    (audio-bus 2)
        ctl-bus   (control-bus 16)
        live?     (atom true)
        sin-ctl   (mixer-sin-control [:tail mixer-g] :out-bus ctl-bus)
        mixer     (meta-mix [:after sin-ctl] :in-bus in-bus :out-bus out-bus)
        handler-k (uuid)]
    (node-map-n-controls mixer :rev-mix ctl-bus 16)
    (doseq [control-id nano2-controls]
      (println "adding handler for: " (vec (concat event-k [control-id])))
      (on-latest-event (vec (concat event-k [control-id]))
                       (fn [msg]
                         (let [id  (:id msg)
                               val (:val msg)]
                           (when-let [f (get nano2-fns id)]
                             (f val sin-ctl))))
                       (str handler-k control-id)))
    (on-node-destroyed mixer
                       (fn [_]
                         (doseq [control-id nano2-controls]
                           (remove-event-handler (str handler-k control-id)))
                         (swap! korg-nano-kontrol-mixers dissoc event-k)
                         (reset! live? false)))

    (with-meta {:mixer-g   mixer-g
                :mixer     mixer
                :handler-k handler-k
                :in-bus    in-bus
                :event-key event-k
                :live?     live?
                :sin-ctl   sin-ctl}
      {:type ::mixer})))


(defn add-mixer
  ([event-k]
     (add-mixer event-k (foundation-safe-post-default-group)))
  ([event-k tgt-g]
     (add-mixer event-k tgt-g 0))
  ([event-k tgt-g out-bus]
     (let [mixers (swap! korg-nano-kontrol-mixers
                         (fn [mixers]
                           (when (contains? mixers event-k)
                             (throw (Exception.
                                     (str "Korg Nano Kontrol Mixer with event key "
                                          event-k " already exists."))))

                           (assoc mixers event-k (mk-mixer event-k tgt-g out-bus))))]
       (get mixers event-k))))

(defn kill-mixer [mixer]
  (remove-event-handler (:handler-key mixer))
  (swap! korg-nano-kontrol-mixers dissoc (:event-key mixer))
  (with-inactive-modification-error :silent
    (kill (:mixer mixer))))

(defn add-nk-mixer
  ([g k]
     (add-mixer [:v-nanoKON2 g k :control-change]))
  ([g k tgt-g]
     (add-mixer [:v-nanoKON2 g k :control-change] tgt-g))
  ([g k tgt-g out-bus]
     (add-mixer [:v-nanoKON2 g k :control-change] tgt-g out-bus)))

(defn mx
  [k]
  (:mixer-g (get @korg-nano-kontrol-mixers k)))

(defn nkmx
  ([k] (nkmx 16 k)) ;; TODO replace 16 with a more sensible call i.e. nk-bank
  ([bank k]
     (:in-bus (get @korg-nano-kontrol-mixers [:v-nanoKON2 bank k :control-change]))))

(defn nkmx-out
  [k]
  (:out-bus (get @korg-nano-kontrol-mixers [:v-nanoKON2 16 k :control-change])))

(defn nkmx-synth
  [k]
  (:mixer (get @korg-nano-kontrol-mixers [:v-nanoKON2 16 k :control-change])))

(defn nkmx-sctl
  ([k] (nkmx-sctl k 16))
  ([k bank-id]
     (:sin-ctl (get @korg-nano-kontrol-mixers [:v-nanoKON2 bank-id k :control-change]))))


(def lp (first lp-core/launchpad-kons))
(defonce beat-rep-key (uuid))
(def phrase-size 16)
(metronome/start lp :mixer count-trigger-id beat-rep-key)

;;(def samples-set-1 [kick-s snare-s shaker-s hat-s])
(def samples-set-1 [d-kick-s d-shake-s d-tom-s d-shake-2-s d-shake-1-s d-shake-2-deep-s])

(defonce default-mixer-g (group :tail (foundation-safe-post-default-group)))
(defonce drum-g (group))
(defonce drum-trigger-mix-g (group :after drum-g))
(defonce drum-basic-mixer-g (group :after default-mixer-g))

(defonce lp64-b  (audio-bus 2 "lp64 basic-mixer"))
(defonce bas-mix-s64  (basic-mixer [:head drum-basic-mixer-g] :in-bus lp64-b :mute 0))
(defonce trig64-mixer (add-nk-mixer (nk-bank :lp64) "lp64-triggers" drum-trigger-mix-g lp64-b))

(ctl bas-mix-s64 :mute 1)

(defsynth phasor-skipping-sequencer
  "Supports looping and jumping position"
  [buf 0 rate-b 0 out-bus 0 start-point 0 bar-trg [0 :tr] loop? 0 amp 1.0 cb 0]
  (let [rate (in:kr rate-b 1)
        ph (phasor:ar :trig bar-trg
                      :rate (* rate (buf-rate-scale:kr buf))
                      :start 0
                      :end (buf-frames:kr buf)
                      :reset-pos start-point)
        br (buf-rd:ar 1 buf ph loop?)]
    (out:kr cb (a2k ph))
    (out out-bus (* amp br))))

(defsynth rater [out-bus 0 rate 1]  (out out-bus rate))

(defsynth orig-mono-sequencer
  "Plays a single channel audio buffer (with panning)"
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol
                    (pan2
                     (rlpf
                      (scaled-play-buf 1 buf rate bar-trg)
                      (demand bar-trg 0 (dbrown 200 20000 50 INF))
                      (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

(defn- start-synths [samples patterns mixers num-steps tgt-group beat-cnt-bus beat-trg-bus out-bus]
  (let [out-busses (if mixers
                     (map :in-bus mixers)
                     (repeat out-bus))]
    (doall (mapcat (fn [sample pattern out-bus]
                     (map (fn [step-idx]
                            (mono-sequencer [:tail tgt-group]
                                            :buf (to-sc-id sample)
                                            :beat-num step-idx
                                            :pattern (:pattern-buf pattern)
                                            :beat-cnt-bus beat-cnt-bus
                                            :beat-trg-bus beat-trg-bus
                                            :out-bus out-bus))
                          (range num-steps)))
                   samples
                   patterns
                   out-busses))))

(defn- mk-sequence-patterns
  "Setup our buffers"
  [samples num-steps]
  (doall (map (fn [sample]
                (with-meta {:num-steps num-steps
                            :pattern-buf (buffer num-steps)}
                  {:type ::sequence-pattern}))
              samples)))

(defn mk-sequencer [nk-group handle samples num-steps tgt-group beat-cnt-bus beat-trg-bus out-bus]
  (let [patterns (mk-sequence-patterns samples num-steps)
        container-group (group handle :tail tgt-group)
        seq-group       (group "lp-sequencer" :head container-group)
        mixer-group     (group "lp-mixers" :after seq-group)
        mixer-handles   (map #(str handle "-" %) (range (count samples)))
        mixers          (doall (map #(add-nk-mixer nk-group % mixer-group out-bus) mixer-handles))
        synths   (start-synths samples patterns mixers num-steps seq-group beat-cnt-bus beat-trg-bus out-bus)]
    (with-meta {:patterns patterns
                :num-steps num-steps
                :num-samples (count samples)
                :synths (agent synths)

                :seq-group seq-group
                :mixer-group mixer-group
                :mixer-handles mixer-handles
                :mixers mixers}
      {:type ::sequencer})))


(defonce sequencer-64
  (mk-sequencer
   (nk-bank :lp64)
   "lp64"
   samples-set-1
   phrase-size
   drum-g
   beat-count-bus
   beat-trigger-bus
   lp64-b))

(defonce refresh-beat-key (uuid))

(on-trigger count-trigger-id (beat-scroll/grid-refresh lp sequencer-64 phrase-size :up) refresh-beat-key)
(beat/setup-side-controls :up sequencer-64)

;;Adjust bpm
(lp-core/bind :up :7x6 (fn [] (ctl beat-trigger :div (swap! current-beat inc))))
(lp-core/bind :up :7x5 (fn [] (ctl beat-trigger :div (swap! current-beat dec))))

;;Shutdown
(lp-core/bind :up :arm  (fn [lp]
                          (ctl bas-mix-s64 :mute 0)
                          (beat/off lp sequencer-64)))


(ctl bas-mix-s64 :mute 1)

(defonce seq-g (group))
(def seq-mixer-group (group "lp-mixers" :after seq-g))
(defonce seq-trigger-mix-g (group :after seq-g))
(defonce seq-basic-mixer-g (group :after default-mixer-g))
(defonce seq-mix-s64  (basic-mixer [:head seq-basic-mixer-g] :in-bus lp64-b :mute 0))

(def sample-selection [;;arp-s
                       ;;arp-chord-s
                       voice-1-s
                       voice-2-s
                       strings-s
                       drums-s
                       chords-s
                       dub-s

                       bass-1-s
                       bass-2-s
                       ;;                      bass-3-s
                       ;;                      hard-1-s
                       ;;                      hard-2-s
                       ;;                       hard-3-s
                       ;;                       gtr-1-s

                       ;;  gtr-2-s
                       ;;  gtr-3-s
                       ;;  gtr-str-s
                       ])

;;(def seq-mixers  (doall (map-indexed (fn [idx _] (mixers/add-nk-mixer (nk-bank :lp64) (str "lp64-seq-" idx) seq-mixer-group lp64-b)) sample-selection)))
(def seq-mixers [])

(defonce rate-b  (control-bus 1 "Rate"))
(defonce rater-s (rater :out-bus rate-b))

(def all-row-samples
  (doall (map-indexed
          (fn [idx sample] {:row idx
                            :sample sample
                            :sequencer (sequencer/phasor-skipping-sequencer [:tail seq-g]
                                                                            :buf (to-sc-id sample)
                                                                            :loop? true
                                                                            :bar-trg 0
                                                                            :amp 0
                                                                            :beat-b beat-trigger-bus
                                                                            :rate-b rate-b
                                                                            :out-bus (:in-bus (get-in seq-mixers [idx] {:in-bus 0})))})
          sample-selection)))

(use 'launchpad.plugin.sample-rows :reload)
(sample-rows lp :left all-row-samples)
