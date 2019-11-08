(ns lnrocks.core
  (:require [crux.api :as crux])
  (:import [crux.api ICruxAPI])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html



(defn counter
  ;;entity:  plate, plate-set, sample or project
  ;;need: how many will be created
  ;;returns vector of start and end id
  [ entity need ]
  (let [old (crux/entity (crux/db node) :counters)
        start (+ 1 (entity old))
        end (+ start (- need 1))
        new (assoc old entity end)]
    (crux/submit-tx node [[:crux.tx/cas old new]])
    {:start start :end end}))



(defn new-project
  [ name description session-id]
  (let [ prj-id (:start (counter :project 1))
        doc  {:crux.db/id (keyword (str "prj-" prj-id))
                :name name
                :description description
                :session session-id}]
    (crux/submit-tx node [[:crux.tx/put doc]] )
  prj-id))

(new-project "MyNewProj" "a test of function" 1)
(crux/entity (crux/db node) :prj-1)


(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))



(defn new-plate
  ;;with-samples: boolean
  [ project-id plate-set-id plate-id plate-format-id plate-type-id with-samples] 
  (let [ plt-doc {:crux.db/id (keyword (str "plt-" plate-id))
                  :plate-format-id plate-format-id
                  :plate-type-id plate-type-id
                  :project-id project-id}]
    (crux/submit-tx node [[:crux.tx/put plt-doc]] )
     
    )
  )



(defn new-plate-set
  ;;with-samples: boolean
  [ plate-set-name description num-plates plate-format-id plate-type-id
   project-id plate-layout-name-id lnsession-id with-samples] 
  (let [ps-id (:start (counter :plate-set 1))
        plt-ids (counter :plate-set num-plates)
        start (:start plt-ids)
        end (:end plt-ids)
        ps-doc {:crux.db/id (keyword (str "ps-" ps-id))
                :name plate-set-name
                :description description
                :num-plates num-plates
                :plate-format-id plate-format-id
                :plate-type-id plate-type-id
                :project-id project-id
                :plate-layout-name-id plate-layout-name-id
                :session lnsession-id}
        ]
     (crux/submit-tx node [[:crux.tx/put doc]] )
     
    )
  )



(counter :plate-set 1)



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
   (def ^crux.api.ICruxAPI node
    (crux/start-node
       {:crux.node/topology :crux.standalone/topology
        :crux.node/kv-store "crux.kv.rocksdb/kv"
        :crux.standalone/event-log-dir "data/eventlog-1"
        :crux.kv/db-dir "data/db-dir1"
        :crux.standalone/event-log-kv-store "crux.kv.rocksdb/kv"}))

(println "In main"))


