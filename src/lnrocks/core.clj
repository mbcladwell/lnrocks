(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util])
  (:import [crux.api ICruxAPI])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html

;;(load lnrocks/util.clj)

(defn counter
  ;;entity:  plate, plate-set, sample or project
  ;;need: how many will be created
  ;;returns vector of start and end id
  ;;can only get one per method or transaction aborted
  [ entity need ]
  (let [old (crux/entity (crux/db node) :counters)
        start (+ 1 (entity old))
        end (+ start (- need 1))
        new (assoc old entity end)]
    (crux/submit-tx node [[:crux.tx/cas old new]])
    {:start start :end end}))

;;(counter :sample 368)

(defn get-ps-plt-spl-ids
  ;;args are the integer quantities needed
  ;; :plate-set :plate :sample
  ;;returns vector of start ids for each  
  [ need-ps need-plt need-spl ]
  (let [orig-counters (crux/entity (crux/db node) :counters)
        old-ps (:plate-set orig-counters)
        ps (+ 1 old-ps) ;;this is the start id
        ps-end (+ old-ps need-ps) ;;this goes into the db as the next start id
        old-plt (:plate orig-counters)
        plt (+ 1 old-plt) ;;this is the start id
        plt-end (+ old-plt need-plt) ;;this goes into the db as the next start id
        old-spl (:sample orig-counters)
        spl (+ 1 old-spl) ;;this is the start id
        spl-end (+ old-spl need-spl) ;;this goes into the db as the next start id
        new1 (assoc orig-counters :plate-set ps-end)
        new2 (assoc new1 :plate plt-end)
        new3 (assoc new2 :sample spl-end)
        ]
    (crux/submit-tx node [[:crux.tx/cas orig-counters new3]])
    {:plate-set ps :plate plt :sample spl}))

;;(crux/entity (crux/db node ) :counters)
;;(get-ps-plt-spl-ids 1 2 3)

(defn get-plate-layout
  ;;x is :id e.g.  41
  [x]
  (filter #(= (:id %) x) (:plate-layout  (crux/entity (crux/db node ) :plate-layout))))


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


(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))

(defn fill-wells
  ;;wells: the map of wells
  ;;ids: a map {:start nn :end nn}
  [wells id-start unk-needed]
  (let [      
        wells-vector (case (count wells)
                       96 util/vec96wells
                       384 util/vec384wells
                       1536 util/vec1536wells)  ]
    (loop [id-counter id-start  ;;counts through the ids by id number
           vec-counter 0
           filled-wells wells]
      (if (= id-counter (+ id-start unk-needed))
        filled-wells
        (recur (+ id-counter 1)
               (+ vec-counter 1)
               (assoc filled-wells (get wells-vector vec-counter)  id-counter))))))


;;(fill-wells util/map96wells 3 3)

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
                 :wells (if with-samples (fill-wells wells sample-id-start unk-per-plate-needed) wells)}]
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
                                   (conj plates (new-plate project-id ps-id plate-id-counter plate-format-id plate-type-id plate-layout-name-id with-samples (get spl-ids-start-vec spl-vector-counter) unk-per-plate-needed)))))}]
    (crux/submit-tx node [[:crux.tx/put ps-doc]])
    ps-id))


;;(new-plate-set "my set 1" "desc" 3 96 1 1 1 2 true)
;;(:plates (crux/entity (crux/db node ) :PS-13))
;;(crux/entity (crux/db node ) :counters)
(defn get-plates-in-project [x]
  (crux/q (crux/db node)
          '{:find [id pd pid ptid  ]
            :where [[e :ln-entity pd]
                    [e :project-id pid]
                    [e :plate-type-id ptid]
                    [e :crux.db/id id]]
                    
            }
        ))

;;(get-plates-in-project 2)


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


