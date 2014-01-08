(ns codemash.voice
  (:use [overtone.live]))

(def voices [:agnes :albert :alex :bad-news :bahh  :bells :boing :bruce :bubbles :cellos :deranged :fred :good-news :hysterical :junior :kathy :pipe-organ
             :princess :ralph :trinoids :vicki :victoria :whisper :zarvox])

(def waves-b (speech-buffer "waves" :voice (rand-nth voices)))
(def ugens-b (speech-buffer "unit generators" :voice (rand-nth voices)))
(def synthesis-b (speech-buffer "Synthesis" :voice (rand-nth voices)))
(def busses-b (speech-buffer "Busses" :voice (rand-nth voices)))
(def timing-b (speech-buffer "Timing" :voice (rand-nth voices)))
(def buffers-b (speech-buffer "Building Sequencer with buffers" :voice (rand-nth voices)))
(def instruments-b (speech-buffer "Musical Instruments" :voice (rand-nth voices)))
(def samples-b (speech-buffer "Samples" :voice (rand-nth voices)))
(def hardware-b (speech-buffer "Hardware External devices" :voice (rand-nth voices)))
(def end-b (speech-buffer "Go forth and make noise.
 Thank you!
 THE END" :voice :cellos))

(defn waves [] (waves-b :rate 1))
(defn ugens [] (ugens-b))
(defn synthesis [] (synthesis-b))
(defn busses [] (busses-b))
(defn timing [] (timing-b))
(defn buffers [] (buffers-b))
(defn instruments [] (instruments-b))
(defn hardware [] (hardware-b))
(defn samples [] (samples-b))

(defn the-end [] (end-b :rate 0.6 :amp 1.5))

(comment
  (waves)
  (ugens)
  (synthesis))
