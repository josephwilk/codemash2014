(ns codemash.intro
  "88        88              88  88                                                    88
   88        88              88  88                                                    88
   88        88              88  88                                                    88
   88aaaaaaaa88   ,adPPYba,  88  88   ,adPPYba,      ,adPPYYba,  8b,dPPYba,    ,adPPYb,88
   88''''''''88  a8P_____88  88  88  a8'     '8a     ''     `Y8  88P'   `'8a  a8'    `Y88
   88        88  8PP'''''''  88  88  8b       d8     ,adPPPPP88  88       88  8b       88
   88        88  '8b,   ,aa  88  88  '8a,   ,a8'     88,    ,88  88       88  '8a,   ,d88
   88        88   `'Ybbd8''  88  88   `'YbbdP''      `'8bbdP'Y8  88       88   `'8bbdP'Y8

                                88
                                88
                                88
8b      db      d8   ,adPPYba,  88   ,adPPYba,   ,adPPYba,   88,dPYba,,adPYba,    ,adPPYba,
`8b    d88b    d8'  a8P_____88  88  a8'     ''  a8'     '8a  88P'   '88'    '8a  a8P_____88
 `8b  d8'`8b  d8'   8PP'''''''  88  8b          8b       d8  88      88      88  8PP'''''''
  `8bd8'  `8bd8'    '8b,   ,aa  88  '8a,   ,aa  '8a,   ,a8'  88      88      88  '8b,   ,aa
    YP      YP       `'Ybbd8''  88   `'Ybbd8''   `'YbbdP''   88      88      88   `'Ybbd8''

                                 88                                                                       88           88                    ad88
  ,d                      ,d     88                                                                       88           88                   d8'
  88                      88     88                                                                       88           88                   88
MM88MMM  ,adPPYba,      MM88MMM  88,dPPYba,    ,adPPYba,     8b      db      d8   ,adPPYba,   8b,dPPYba,  88   ,adPPYb,88      ,adPPYba,  MM88MMM
  88    a8'     '8a       88     88P'    '8a  a8P_____88     `8b    d88b    d8'  a8'     '8a  88P'   'Y8  88  a8'    `Y88     a8'     '8a   88
  88    8b       d8       88     88       88  8PP'''''''      `8b  d8'`8b  d8'   8b       d8  88          88  8b       88     8b       d8   88
  88,   '8a,   ,a8'       88,    88       88  '8b,   ,aa       `8bd8'  `8bd8'    '8a,   ,a8'  88          88  '8a,   ,d88     '8a,   ,a8'   88
  'Y888  `'YbbdP''        'Y888  88       88   `'Ybbd8''         YP      YP       `'YbbdP''   88          88   `'8bbdP'Y8      `'YbbdP''    88

 ad88888ba                                                   88
d8'     '8b                                                  88
Y8,                                                          88
`Y8aaaaa,     ,adPPYba,   88       88  8b,dPPYba,    ,adPPYb,88
  `'''''8b,  a8'     '8a  88       88  88P'   `'8a  a8'    `Y88
        `8b  8b       d8  88       88  88       88  8b       88
Y8a     a8P  '8a,   ,a8'  '8a,   ,a88  88       88  '8a,   ,d88
 'Y88888P'    `'YbbdP''    `'YbbdP'Y8  88       88   `'8bbdP'Y8
"
  (:use overtone.live))

(defonce welcome-sample (load-sample "~/Workspace/music/samples/welcome to sound.wav"))
(def welcome (sample-player welcome-sample :rate 1 :amp 1 :loop? 1 :start-pos 0))
(ctl welcome :loop? 1 :rate -1 :start-pos (:size welcome-sample))
(ctl welcome :loop? 1 :rate 0.5 :start-pos 0)
(ctl welcome :loop? 0 :rate 2 :start-pos 0)
(stop)
