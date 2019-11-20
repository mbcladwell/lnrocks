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



