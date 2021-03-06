(ns baleen.core
  (:require [baleen.state :as state]
            [baleen.sources.wikimedia :as wikimedia]
            [baleen.sources.gnip :as gnip]
            [baleen.server :as server]
            [baleen.events :as events])
  (:require [crossref.util.config :refer [config]])
  (:require [baleen.util :as util])
  (:require [clojure.tools.logging :refer [error info]]))

(def sources
  {:wikimedia {:vocab {:title "Wikipedia DOI citation live stream"
                       :input-count-label "Wikipedia edits"
                       :citation-count-label "DOI citation events"}
               ; With 20 workers, the size of the backlog tends to hang around the zero mark.
               :num-workers 50

               :input-bucket-time 5000
               :citation-bucket-time 300000

               :num-input-buckets 200
               :num-citation-buckets 200

               :start-f wikimedia/start
               :export-f wikimedia/export
               :process-f wikimedia/process

               :watchdog-time 10000
               :restart-f wikimedia/restart}

   :gnip {:vocab {:title "Tweets mentioning DOIs live stream"
                  :input-count-label "Tweets"
                  :citation-count-label "DOI mentions"}
          :num-workers 50

          :input-bucket-time 5000
          :citation-bucket-time 300000

          :num-input-buckets 200
          :num-citation-buckets 200

          :start-f gnip/start
          :export-f gnip/export
          :process-f gnip/process

          :watchdog-time 10000
          :restart-f gnip/restart}})

(defn -main
  [& args]
  (when-not (sources (:enabled-source config))
    (error "Didn't recognise source" (:enabled-source config)))

  (info "Using " (:enabled-source config))
  (reset! state/source (get sources (:enabled-source config)))
  (info "Starting source")
  (events/boot)
  (info "Starting server")
  (server/start)
  (info "Running"))