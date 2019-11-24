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


 ;; (def  ps1 (crux/entity (crux/db node ) :ps1))
 ;;     (def   new-ps1  (update ps1 :plates (comp set conj)
 ;;                        (crux/entity (crux/db node ) :plt1)
 ;;                        (crux/entity (crux/db node ) :plt2)))       
 ;;   (insp/inspect-tree  new-ps1))  




;;(crux/entity (crux/db node) :ps7)
;;(counter :sample 368)
;;(load-assay-run-data node)
;;;;(lnrocks.eg-data/load-assay-run-data node)

;;(init/load-well-numbers node)


;;(load-eg-plate-sets)
;;(egd/assoc-ar-with-ps node)

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
            

(defn import-barcode-ids [ plateset-id barcode-file]

   " Loads table and make the association
      barcodess looks like:

      plate 	barcode.id
      1     	AMRVK5473H
      1      	KMNCX9294W
      1      	EHRXZ2102Z
      1      	COZHR7852Q
      1      	FJVNR6433Q"
    
  (let [col1name (first (util/get-col-names barcode-file))
        col2name (first (rest (util/get-col-names barcode-file)))
        table (util/table-to-map barcode-file)
        processed-table (into [] (map #(process-barcode-file %) table))
        old-ps (crux/entity (crux/db node) plateset-id)
        old-ps-plates (:plates old-ps)
        new-ps-plates #{}]
    (loop [counter 1
           a-plate  (first (filter #(= (:plate-order %) counter) old-ps-plates))
           new-plate    (assoc a-plate :barcode (:barcode (first (filter #(= (:id %) counter) processed-table))))
           new-ps-plates (conj new-ps-plates new-plate ) ]          
      (if (> counter (+ 1 (count processed-table)))
        (println "Finished!")
        (recur
         (+ counter 1)
         (first (filter #(= (:plate-order %) counter) old-ps-plates))
         ;;(crux/submit-tx node [[:crux.tx/put a-proj]] )
         (assoc a-plate :barcode (:barcode (first (filter #(= (:id %) counter) processed-table))))
         (conj new-ps-plates new-plate ) 
         )))
     
    (crux/submit-tx node [[:crux.tx/cas old-ps (assoc old-ps :plates new-ps-plates)]])
    ))
    
;;(def  old-ps (:plates (crux/entity (crux/db node) :ps7)) )
 ;;(first (filter #(= (:plate-order %) 3) old-ps))
        

    ;;(javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", and \"barcode.id\", but found\n" col1name  ", and " col2name  "."  )))))


;;(import-barcode-ids :ps7 "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt") 

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


