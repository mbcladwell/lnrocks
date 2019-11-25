(ns lnrocks.db-inserter
  (:require [clojure.string :only [split split-lines trim]]
            [crux.api :as crux]
            [clojure.set :as s]
            [lnrocks.util :as util]
         ;;    [ln.db-manager :as dbm])
         ;;   [clojure.data.csv :as csv]
            [clojure.java.io :as io])
           )

;; \copy (Select * From assay_run) To '/home/mbc/projects/lnrocks/resources/data/assay-run.csv' With CSV


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;Required data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))

(defn process-barcode-file
"plate barcode.id;  plate is actually plate order"
 [x]
  (into {} {:id (Integer/parseInt (String. (:plate x)))     
            :barcode (String.(:barcode.id x) )}))



(defn new-project [ node prj-name desc user-id]
  (let [prj-id (:start (dbr/counter node :project 1))
        session-id (:session-id (crux/entity (crux/db node) :props)
        doc {:crux.db/id (keyword (str "prj" prj-id))
             :project-sys-name (str "PRJ-" prj-id)
             :name prj-name
             :description desc
             :lnsession-id session-id
             :id prj-id
             :plate-sets #{}
             :hit-lists #{}
             }       ]
    (crux/submit-tx node [[:crux.tx/put doc]] )
    prj-id))




(defn import-barcode-ids [ node plateset-id barcode-file]
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
        new-ps-plates #{}
        ]
    (loop [counter 1
           a-plate  (first (filter #(= (:plate-order %) counter) old-ps-plates))
           new-plate    (assoc a-plate :barcode (:barcode (first (filter #(= (:id %) counter) processed-table))))
           new-ps-plates (conj new-ps-plates new-plate ) ]          
      (if (> counter (+ 1 (count processed-table)))
        (crux/submit-tx node [[:crux.tx/cas old-ps (assoc old-ps :plates new-ps-plates)]])
        (recur
         (+ counter 1)
         (first (filter #(= (:plate-order %) counter) old-ps-plates))
         ;;(crux/submit-tx node [[:crux.tx/put a-proj]] )
         (assoc a-plate :barcode (:barcode (first (filter #(= (:id %) counter) processed-table))))
         (conj new-ps-plates new-plate ) 
         )))
    ))


    ;;(javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", and \"barcode.id\", but found\n" col1name  ", and " col2name  "."  )))))
;;(import-barcode-ids :ps7 "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt") 







