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



(defn new-plate-set [ node ps-name desc plate-format-id plate-type-id  plate-layout-name-id num-plates project-id user-id]
  (let [ps-id (:start (dbr/counter node :plate-set 1))
        session-id (:session-id (crux/entity (crux/db node) :props))
        doc {:crux.db/id (keyword (str "ps" ps-id))
             :plate-set-sys-name (str "PS-" ps-id)
             :plate-set-name ps-name
             :description desc
             :lnsession-id session-id
             :plate-format-id plate-format-id
             :plate-type-id plate-type-id
             :id ps-id
             :user-id (:user-id (crux/entity (crux/db node) :props))
             :num-plates num-plates
             :project-id project-id
             :plates #{}
             :plate-layout-name-id plate-layout-name-id
             }       ]
    (crux/submit-tx node [[:crux.tx/put doc]] )
    ps-id))

(defn new-plates [ node ps-id  plate-layout-name-id num-plates ]
  (let [layout (crux/entity (crux/db node) plate-layout-name-id)
        plate-format-id (:format-id layout)
        unknown-n (:unknown-n layout)
        plt-ids (dbr/counter node :plate num-plates)
        plt-id-start (:start plt-ids)
        plt-id-end (:end plt-ids)
        sample-ids (dbr/counter node :sample-ids (* unknown-n num-plates))

        ]
    (loop [counter 1

           ])
        
        doc {:crux.db/id (keyword (str "plt" plt-id))
             :plate-sys-name (str "PLT-" plt-id)
             :plate-set-id ps-id
             :id plt-id
             :user-id (:user-id (crux/entity (crux/db node) :props))
             :wells #{}
             :plate-order
             }       ]
    (crux/submit-tx node [[:crux.tx/put doc]] )
    plt-id))


;; (crux/entity (crux/db node) :lyt1)
;;(new-plate-set node "MyNewPs" "a test of function" 96 1 1 2 1 1)
;;(:plates (crux/entity (crux/db node) :ps2))
                  ;;      (crux/entity (crux/db node ) :plt20)

;;(def barcode-file "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt")
;; ps7 has 10 plates

            




;;(crux/entity (crux/db node ) :plt9))

;;(insp/inspect-tree (crux/entity (crux/db node ) :ps1))

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))
           
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


