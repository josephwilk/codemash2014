(defproject codemash "0.1.0-SNAPSHOT"
  :description "Welcome to the world of sound"
  :url "http://github.com/josephwilk"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [overtone "0.9.1"]
                 [incanter "1.5.4"]
                 [polynome "0.3.0-SNAPSHOT"]
                 [korg-nano-kontrol2 "0.1.0-SNAPSHOT"]
                 [launchpad "0.1.0-SNAPSHOT"]]

  :jvm-opts ^:replace ["-Xms512m" "-Xmx1g"
                       "-XX:+UseParNewGC"
                       "-XX:+UseConcMarkSweepGC"
                       "-XX:+CMSConcurrentMTEnabled"
                       "-XX:MaxGCPauseMillis=20"
                       "-XX:+CMSIncrementalMode"
                       "-XX:MaxNewSize=257m"
                       "-XX:NewSize=256m"
                       "-XX:+UseTLAB"
                       "-XX:MaxTenuringThreshold=0"])
