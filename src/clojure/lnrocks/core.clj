(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
           [lnrocks.db-init :as init]
           [lnrocks.eg-data :as egd]
            
            [clojure.inspector :as insp]
            [clojure.java.io :as io]
            [clojure.set :as s]
              )
  (:import [crux.api ICruxAPI ])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html


(defn define-db-var []
  
   (def ^crux.api.ICruxAPI node
     (crux/start-node
      {:crux.node/topology :crux.standalone/topology
       :crux.node/kv-store "crux.kv.rocksdb/kv"
       :crux.standalone/event-log-dir "data/eventlog-1"
       :crux.kv/db-dir "data/db-dir1"
       :crux.standalone/event-log-kv-store "crux.kv.rocksdb/kv"})))


(if (.exists (io/as-file "data"))
  (do
    (define-db-var)
    (println "db already exists"))
 (do
   (println "initializing database at startup.")
   (define-db-var)

   (init/initialize-db node)
   (egd/load-eg-data node)
   (init/diag-init node)
   (egd/diag-eg-data node)
   ))

;;(egd/load-eg-plate-sets node)
 ;;get assay runs   (println ":ps3 --  " (first (:wells (crux/entity (crux/db node) :ps1)) )
;;(insp/inspect-tree new-ps5)


(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  [x coll]
  (map #(dissoc % :id) (filter #(= (:id %) x) coll ) ))



(defn new-project [ prj-name desc user-id]
  (let [prj-id (counters :project 1)
        session-id (counters :session-id 1)
        doc {:crux.db/id (str "prj" prj-id)
             :project-sys-name (str "PRJ-" prj-id)
             :name prj-name
             :description desc
             :lnsession-id session-id
             :id prj-id
             :plate-sets #{}
             :hit-lists #{}
             }       ]

    )

  )

;; (crux/entity (crux/db node) :prj1)
;;(new-project "MyNewProj" "a test of function" 1)
;;(:plates (crux/entity (crux/db node) :ps2))
                  ;;      (crux/entity (crux/db node ) :plt20)

;;(def barcode-file "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt")
;; ps7 has 10 plates

(defn process-barcode-file
"plate barcode.id;  plate is actually plate order"
 [x]
  (into {} {:id (Integer/parseInt (String. (:plate x)))     
            :barcode (String.(:barcode.id x) )}))
            




;;(crux/entity (crux/db node ) :plt9))

;;(insp/inspect-tree (crux/entity (crux/db node ) :ps7))

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))
           
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


