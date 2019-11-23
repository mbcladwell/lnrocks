(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
           [lnrocks.db-init :as init]
           [lnrocks.eg-data :as egd]
            
            [clojure.inspector :as insp]
          [clojure.java.io :as io]
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
   (egd/load-eg-data node)))
   
  
(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  [x coll]
  (map #(dissoc % :id) (filter #(= (:id %) x) coll ) ))


;;(crux/entity (crux/db node) :ps7)
;;(counter :sample 368)
;;(load-assay-run-data node)
;;;;(lnrocks.eg-data/load-assay-run-data node)

;;(init/load-well-numbers node)




;;(load-plate-layouts)

;;(new-project "MyNewProj" "a test of function" 1)
;;(crux/entity (crux/db node) :lyt1)

;;(def barcode-file "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt")
;; ps7 has 10 plates

(defn import-barcode-ids [node plateset-id barcode-file]

   " Loads table and make the association
      barcodess looks like:

      plate 	barcode.id
      1     	AMRVK5473H
      1      	KMNCX9294W
      1      	EHRXZ2102Z
      1      	COZHR7852Q
      1      	FJVNR6433Q"
    
  (let [ col1name (first (util/get-col-names barcode-file))
        col2name (first (rest (util/get-col-names barcode-file)))
        table (util/table-to-map barcode-file)
        old-ps (crux/entity (crux/db node) :ps7)
       ;; content (into [] (zipmap (map #(:barcode.id %) table) (map #(Integer. (:plate %)) table)))
        ]

    ;;(javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", and \"barcode.id\", but found\n" col1name  ", and " col2name  "."  )))))
))


;;(insp/inspect-tree :ps3)

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))
           
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(insp/inspect-tree (crux/entity (crux/db node) :hl1))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


