(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
            [lnrocks.db-init :as init]
            [clojure.inspector :as insp]
          [clojure.java.io :as io]
              )
  (:import [crux.api ICruxAPI])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html

(load "util")
(load "db_inserter")
(load "db_retriever")
(load "db_init")


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
   
   ))





  


;;(counter :sample 368)



(defn new-project
  [ name description session-id]
  (let [ prj-id (:start (dbr/counter node :project 1))
        doc  {:crux.db/id (keyword (str "prj-" prj-id))
              :ln-entity "project"
                :prj-name name
                :description description
                :session session-id}]
    (crux/submit-tx node [[:crux.tx/put doc]] )
  prj-id))

;;(new-project "MyNewProj" "a test of function" 1)
;;(crux/entity (crux/db node) :prj-1)




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
  (let [unk-per-plate-needed (:unknown-n (first (dbr/get-plate-layout node plate-layout-name-id)))
        start-ids (dbr/get-ps-plt-spl-ids node 1 num-plates (* num-plates unk-per-plate-needed) )
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
                    
         ;;    :args [{ 'p "PS-12"  }
                   
           ;;        ]}))

;;(count (get-plates-in-project 2))

;;(init/load-eg-projects node)


;;I will load projects with plate-set-names (no plates)
;;also assay run names and data
;;also hit lists
;;Use algorithm to generate plates/wells/samples and copy to plate-set
(defn load-example-data []
  ;; (new-plate-set  plate-set-name description num-plates plate-format-id plate-type-id
  ;;  project-id plate-layout-name-id lnsession-id with-samples)
  (let [all-projects (init/load-eg-projects)
        ps1-dummy (new-plate-set "dummy" "dummy" 2 96 1 1 1 1 true )
        ps2-dummy (new-plate-set "dummy" "dummy" 2 96 1 1 1 1 true )
        ps3-dummy (new-plate-set "dummy" "dummy" 2 96 1 1 1 1 true )
        ps4-dummy (new-plate-set "dummy" "dummy" 2 384 1 1 2 13 true )
        ps5-dummy (new-plate-set "dummy" "dummy" 1 1536 1 1 3 37 true )
        ps6-dummy (new-plate-set "dummy" "dummy" 10 96 1 1 10 1 true )
        ps7-dummy (new-plate-set "dummy" "dummy" 10 96 1 1 10 1 true )
        ps8-dummy (new-plate-set "dummy" "dummy" 3 96 1 1 10 1 true )

        ]
    ))


(defn load-eg-data
  "Loads all example data"
  []
  (def  projects (init/load-eg-projects))
  (loop [counter 1
         the-doc (first (filter #(= (:id %) counter) projects))
         dummy nil]
    (if (> counter 11)
      (println "Example data loaded!")
      (recur
       (+ counter 1)
       (first (filter #(= (:id %) counter) projects))
       (crux/submit-tx node [[:crux.tx/put the-doc]] )))))


(def b {:crux.db/id :b :test "test" :test2 "test2" :myinsert {:a 1 :b 2 :c 3 :d {:embedded 2 :d 3 :e 4 :g {:h 1 :k {:m 1 :n 2}}} :f [1 -2 3 -0.004] }})
;;(crux/submit-tx node [[:crux.tx/put b]] )

;;   (load-eg-data)
;;(:id prjs 1)

;;(def a (first (filter #(= (:id %) 1) projects)))
;;(insp/inspect-tree a)
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(:plates (first(:plate-sets (crux/entity (crux/db node) :plt1))))

{:find '[n]
  :where '[[(re-find #"I" n)]
           [(= l "Ivanov")]]
  :args [{'n "Ivan" 'l "Ivanov"}
         {'n "Petr" 'l "Petrov"}]}

(crux/q (crux/db node)
       
{'[:find ?attr
 :where 
 [?p :person/name]
 [?p ?attr]]})


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


