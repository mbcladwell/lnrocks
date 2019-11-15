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



(defn process-layout-data
  "processes that tab delimitted, R generated layouts for import
   order is important; must correlate with SQL statement order of ?'s"
  [x]
(into {} { :id (Integer/parseInt(:id x)) :well (Integer/parseInt(:well x )) :type  (Integer/parseInt(:type x )) :reps (Integer/parseInt(:reps x )) :target (Integer/parseInt(:target x ))}))

(defn process-layout-names
  "processes that tab delimitted, R generated layouts for import
   order is important; must correlate with SQL statement order of ?'s"
  [x]
(into {} { :id (Integer/parseInt(:id x)) :sys-name (:sys-name x ) :name (:name x ) :description (:description x ) :plate-format-id (Integer/parseInt(:plate-format-id x ))  :replicates (Integer/parseInt(:replicates x)) :targets (Integer/parseInt(:targets x)) :use-edge (Integer/parseInt(:use-edge x)) :num-controls (Integer/parseInt(:num-controls x)) :unknown-n (Integer/parseInt(:unknown-n x)) :control-loc (:control-loc x) :source-dest (:source-dest x)  }))

(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  [x coll]
  (map #(dissoc % :id) (filter #(= (:id %) x) coll ) ))


(defn load-plate-layouts []
  ;;add data to layout names using the key :layout
  (let   [table (util/table-to-map "resources/data/plate_layouts_for_import.txt")
          layout-data (into [] (map #(process-layout-data %) table))
          table2 (util/table-to-map "resources/data/plate_layout_name.txt")
          layout-names (into [] (map #(process-layout-names %) table2))
          result (map #(assoc % :layout (extract-data-for-id (:id %)  layout-data)) layout-names)]
         result))


(defn process-well-numbers-data
  "processes that tab delimitted, R generated well_numbers for import
because some are strings, all imported as string
   order is important; must correlate with SQL statement order of ?'s"
  [x]
  (into {} {:format (Integer/parseInt (String. (:format x)))
            :wellname (:wellname x )
            :row (:row x )
            :rownum (Integer/parseInt (String. (:rownum x )))
            :col (Integer/parseInt (String. (:col x )))
            :totcolcount (Integer/parseInt (String. (:totcolcount x)))
            :byrow (Integer/parseInt (String. (:byrow x )))
            :bycol (Integer/parseInt (String. (:bycol x )))
            :quad (Integer/parseInt (String. (:quad x )))
            :parentwell (Integer/parseInt (String. (:parentwell x ))) }))



(defn load-well-numbers []
         (let   [  table (util/table-to-map "resources/data/well_numbers_for_import.txt")
               content (into [] (map #(process-well-numbers-data %) table))]
         content))






;; (defn import-barcode-ids [ plateset-id barcode-file]

;;    " Loads table and make the association
;;       barcodess looks like:

;;       plate 	barcode.id
;;       1     	AMRVK5473H
;;       1      	KMNCX9294W
;;       1      	EHRXZ2102Z
;;       1      	COZHR7852Q
;;       1      	FJVNR6433Q"
    
;;   (let [ col1name (first (util/get-col-names barcode-file))
;;         col2name (first (rest (util/get-col-names barcode-file)))
;;         table (util/table-to-map barcode-file)
;;         sql-statement (str "UPDATE plate SET barcode = ? WHERE plate.ID IN ( SELECT plate.id FROM plate_set, plate_plate_set, plate  WHERE plate_plate_set.plate_set_id=" (str plateset-id) " AND plate_plate_set.plate_id=plate.id AND plate_plate_set.plate_order=? )")
;;         content (into [] (zipmap (map #(:barcode.id %) table) (map #(Integer. (:plate %)) table)))
;;         ]
;;     (if (and (= col1name "plate")(= col2name "barcode.id"))
;;       (with-open [con (j/get-connection cm/conn)
;;                   ps  (j/prepare con [sql-statement])]
;;         (p/execute-batch! ps content))    
;;       (javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", and \"barcode.id\", but found\n" col1name  ", and " col2name  "."  )))))



