(ns codemash.samples
  "Samples lurk here:
   https://github.com/stars-my-destination/samples"
  (:use overtone.live))

;;Samples by devism http://monome.org/community/discussion/736/x&page=1#Item_22
(defonce arp-s (load-sample "~/Workspace/music/samples/P5mlr/ARP.wav"))
(defonce arp-chord-s (load-sample "~/Workspace/music/samples/P5mlr/ARPCHORD.wav"))
(defonce chords-s (load-sample "~/Workspace/music/samples/P5mlr/CHORDs.wav"))
(defonce voice-1-s (load-sample "~/Workspace/music/samples/P5mlr/VOICE.wav"))
(defonce voice-2-s (load-sample "~/Workspace/music/samples/P5mlr/VOICE2.wav"))
(defonce strings-s (load-sample "~/Workspace/music/samples/P5mlr/STRINGS.wav"))
(defonce drums-s (load-sample "~/Workspace/music/samples/P5mlr/DRUMS.wav"))
(defonce dub-s (load-sample "~/Workspace/music/samples/P5mlr/DUB.wav"))
(defonce string-s (load-sample "~/Workspace/music/samples/P5mlr/STRINGS.wav"))
(defonce bass-1-s (load-sample "~/Workspace/music/samples/P5mlr/BASS1.WAV"))
(defonce bass-2-s (load-sample "~/Workspace/music/samples/P5mlr/BASS2.WAV"))
(defonce bass-3-s (load-sample "~/Workspace/music/samples/P5mlr/BASS3.WAV"))
(defonce hard-1-s (load-sample "~/Workspace/music/samples/P5mlr/HARD1.WAV"))
(defonce hard-2-s (load-sample "~/Workspace/music/samples/P5mlr/HARD2.WAV"))
(defonce hard-3-s (load-sample "~/Workspace/music/samples/P5mlr/HARDDUB.WAV"))
(defonce gtr-1-s (load-sample "~/Workspace/music/samples/P5mlr/GTR1.WAV"))
(defonce gtr-2-s (load-sample "~/Workspace/music/samples/P5mlr/GTR2.WAV"))
(defonce gtr-3-s (load-sample "~/Workspace/music/samples/P5mlr/GTR3.WAV"))
(defonce gtr-str-s (load-sample "~/Workspace/music/samples/P5mlr/GTrSTr.wav"))

(defonce d-kick-s         (load-sample "~/Workspace/music/samples/sliced-p5/kick.aif"))
(defonce d-shake-s        (load-sample "~/Workspace/music/samples/sliced-p5/shaker.aif"))
(defonce d-tom-s          (load-sample "~/Workspace/music/samples/sliced-p5/tom.aif"))
(defonce d-shake-2-s      (load-sample "~/Workspace/music/samples/sliced-p5/double-shake.aif"))
(defonce d-shake-1-s      (load-sample "~/Workspace/music/samples/sliced-p5/single-shake.aif"))
(defonce d-shake-2-deep-s (load-sample "~/Workspace/music/samples/sliced-p5/double-shake-deep.aif"))
