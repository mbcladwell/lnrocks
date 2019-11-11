(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util])
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

;;(counter :sample 368)


(defn get-plate-layout
  ;;x is :id e.g.  41
  [x]
  (filter #(= (:id %) x) (:plate-layout  (crux/entity (crux/db node ) :plate-layout))))


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

(defn fill-wells
  ;;wells: the map of wells
  ;;ids: a map {:start nn :end nn}
  [wells ids]
  (let [start (:start ids)
        end  (:end ids)
        wells-vector (case (count wells)
                       96 util/vec96well
                       384 util/vec384well
                       1536 util/vec1536well)  ]
    (loop [id-counter start  ;;counts through the ids by id number
           vec-counter 0     ;;index of the id vector
           filled-wells wells]
      (if (> id-counter end)
        filled-wells
        (recur (+ id-counter 1)
               (+ vec-counter 1)
               (assoc filled-wells (get wells-vector vec-counter)  id-counter))))))


;;(fill-wells util/map96wells (counter :sample 10))

(defn new-plate
  ;;with-samples: boolean
  [ project-id plate-set-id plate-id plate-format-id plate-type-id plate-layout-name-id with-samples] 
  (let [wells (case plate-format-id
                96 util/map96wells
                384 util/map384wells
                1536 util/map1536wells)
        unk-needed (:unknown-n (first (get-plate-layout plate-layout-name-id)))
        sample-ids (counter :sample unk-needed)
        plt-doc { ;; :crux.db/id (keyword (str "plt-" plate-id))
                 :plate-format-id plate-format-id
                 :plate-type-id plate-type-id
                 :project-id project-id
                 :id plate-id
                 :wells (if with-samples (fill-wells wells sample-ids) wells)}]
    ;;(crux/submit-tx node [[:crux.tx/put plt-doc]] )
    plate-id))

(:unknown-n (first (get-plate-layout 1)))
;;(new-plate 1 1 2 96  1 1 true)

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
                :session lnsession-id
                :plates (loop [id-counter start         
                               plates []]
                          (if (> id-counter end)
                            plates
                            (recur (+ id-counter 1)           
                                   (conj plates (new-plate project-id ps-id id-counter plate-format-id plate-type-id plate-layout-name-id with-samples)))))}]
;;     (crux/submit-tx node [[:crux.tx/put doc]] )
     (println ps-doc)
    )
  )

;;(new-plate-set "my set 1" "desc" 3 96 1 1 1 1 true)


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


