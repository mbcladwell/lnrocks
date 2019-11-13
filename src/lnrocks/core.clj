(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
            [lnrocks.db-init :as init]
            )
  (:import [crux.api ICruxAPI])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html

(load "util")
(load "db_inserter")
(load "db_retriever")

(defn define-db-var []
 (def ^crux.api.ICruxAPI node
    (crux/start-node
       {:crux.node/topology :crux.standalone/topology
        :crux.node/kv-store "crux.kv.rocksdb/kv"
        :crux.standalone/event-log-dir "data/eventlog-1"
        :crux.kv/db-dir "data/db-dir1"
        :crux.standalone/event-log-kv-store "crux.kv.rocksdb/kv"}))
  )

(if (.exists (io/as-file "data"))
 (define-db-var)
 (do
   (define-db-var)
   (load "db_init"))





  


;;(counter :sample 368)



(defn new-project
  [ name description session-id]
  (let [ prj-id (:start (counter :project 1))
        doc  {:crux.db/id (keyword (str "prj-" prj-id))
              :ln-entity "project"
                :prj-name name
                :description description
                :session session-id}]
    (crux/submit-tx node [[:crux.tx/put doc]] )
  prj-id))

(new-project "MyNewProj" "a test of function" 1)
(crux/entity (crux/db node) :prj-1)




(defn new-plate
  ;;with-samples: boolean
  [ project-id plate-set-id plate-id plate-format-id plate-type-id plate-layout-name-id with-samples sample-id-start unk-per-plate-needed]
  (let [wells (case plate-format-id
                96 util/map96wells
                384 util/map384wells
                1536 util/map1536wells)
        plt-doc { ;; :crux.db/id (keyword (str "plt-" plate-id))
                 :ln-entity "plate"
                 :plate-format-id plate-format-id
                 :plate-type-id plate-type-id
                 :project-id project-id
                 :id plate-id
                 :plate-sys-name (str "PLT-" plate-id)
                 :wells (if with-samples (util/fill-wells wells sample-id-start unk-per-plate-needed) wells)}]
    plt-doc))

;;(new-plate 1 1 2 96  1 1 true 3 3)

(defn new-plate-set
  ;;with-samples: boolean
  [ plate-set-name description num-plates plate-format-id plate-type-id
   project-id plate-layout-name-id lnsession-id with-samples] 
  (let [unk-per-plate-needed (:unknown-n (first (get-plate-layout plate-layout-name-id)))
        start-ids (get-ps-plt-spl-ids 1 num-plates (* num-plates unk-per-plate-needed) )
        ps-id (:plate-set start-ids)
        plate-start (:plate start-ids)
        plate-end (+ plate-start (- num-plates 1) )
        sample-start (:sample start-ids)
        spl-ids-start-vec (into [] (range sample-start (+ sample-start (* unk-per-plate-needed num-plates)) unk-per-plate-needed))
        ps-doc {:crux.db/id (keyword (str "PS-" ps-id))
                :ln-entity "plate-set"
                :ps-name plate-set-name
                :description description
                :num-plates num-plates
                :plate-format-id plate-format-id
                :plate-type-id plate-type-id
                :project-id project-id
                :plate-layout-name-id plate-layout-name-id
                :session lnsession-id
                :plates (loop [plate-id-counter plate-start
                               spl-vector-counter 0
                               plates []]
                          (if (> plate-id-counter plate-end)
                            plates
                            (recur (+ plate-id-counter 1)
                                   (+ spl-vector-counter 1)
                                   (conj plates (new-plate project-id ps-id plate-id-counter plate-format-id
                                                           plate-type-id plate-layout-name-id with-samples
                                                           (get spl-ids-start-vec spl-vector-counter) unk-per-plate-needed)))))}]
    (crux/submit-tx node [[:crux.tx/put ps-doc]])
    ps-id))


;;(new-plate-set "my set 1" "desc" 3 96 1 1 1 2 true)
;;(:plates (crux/entity (crux/db node ) :PS-13))
;;(crux/entity (crux/db node ) :counters)
(defn get-plates-in-project [x]
  (crux/q (crux/db node)
          '{:find [e p  ]
            :where [[e :ps-name p]]}))
                    
             :args [{ 'p "PS-12"  }
                   
                   ]}))

;;(count (get-plates-in-project 2))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


