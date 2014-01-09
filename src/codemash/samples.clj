(ns codemash.samples
  "Samples lurk here:
   https://github.com/stars-my-destination/samples"
  (:use overtone.live))

(def sample-root "~/Workspace/music/samples/")
;;Samples by devism http://monome.org/community/discussion/736/x&page=1#Item_22
(defonce chords-s  (load-sample (str sample-root "CHORDs.wav")))
(defonce voice-1-s (load-sample (str sample-root "VOICE.wav")))
(defonce voice-2-s (load-sample (str sample-root "VOICE2.wav")))
(defonce strings-s (load-sample (str sample-root "STRINGS.wav")))
(defonce drums-s   (load-sample (str sample-root "DRUMS.wav")))
(defonce dub-s     (load-sample (str sample-root  "DUB.wav")))
(defonce string-s  (load-sample (str sample-root "STRINGS.wav")))
(defonce bass-1-s  (load-sample (str sample-root "BASS1.WAV")))
(defonce bass-2-s  (load-sample (str sample-root "BASS2.WAV")))
(defonce bass-3-s  (load-sample (str sample-root "BASS3.WAV")))
(defonce hard-1-s  (load-sample (str sample-root "HARD1.WAV")))
(defonce hard-2-s  (load-sample (str sample-root "HARD2.WAV")))
(defonce hard-3-s  (load-sample (str sample-root "HARDDUB.WAV")))
(defonce gtr-1-s   (load-sample (str sample-root "GTR1.WAV")))
(defonce gtr-2-s   (load-sample (str sample-root "GTR2.WAV")))
(defonce gtr-3-s   (load-sample (str sample-root "GTR3.WAV")))
(defonce gtr-str-s (load-sample (str sample-root "GTrSTr.wav")))