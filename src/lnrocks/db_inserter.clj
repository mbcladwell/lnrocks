(ns lnrocks.db-inserter
  (:require [clojure.string :only [split split-lines trim]]
                       [crux.api :as crux]
;;[lnrocks.core :as lnrc]
         ;;    [ln.db-manager :as dbm])
         ;;   [clojure.data.csv :as csv]
            [clojure.java.io :as io])
           )

(defn tokens
  [s]
  (-> s clojure.string/trim (clojure.string/split #"\s+")))

(defn pairs
  [coll1 coll2]
  (map vector coll1 coll2))

(defn parse-table
  [raw-table-data]
  (let [table-data (map tokens (clojure.string/split-lines raw-table-data))
        column-keys (map keyword (first table-data))
        contents  (next table-data)]
    (for [x contents]
  (into (sorted-map)(pairs column-keys x)))))



(defn table-to-map [ file]
  (->
    file
    slurp
    parse-table))

(defn get-col-names [ file ]
 (first (map tokens (clojure.string/split-lines (slurp file)))))

(defn process-layout-data
  "processes that tab delimitted, R generated layouts for import
   order is important; must correlate with SQL statement order of ?'s"
  [x]
(into {} { :id (Integer/parseInt(:id x)) :well (Integer/parseInt(:well x )) :type  (Integer/parseInt(:type x )) :reps (Integer/parseInt(:reps x )) :target (Integer/parseInt(:target x ))}))

(defn load-plate-layouts []
       (let   [  table (table-to-map "resources/data/plate_layouts_for_import.txt")
               content (into [] (map #(process-layout-data %) table))]
         content))

(load-plate-layouts)

(defn load-plate-layoutnames []
(table-to-map "resources/data/plate_layout_name.txt"))




 (def a  [ {:id "41", :reps "1", :target "1", :type "4", :well "1523"} {:id "41", :reps "1", :target "1", :type "4", :well "1524"} {:id "41", :reps "1", :target "1", :type "4", :well "1525"} ])

(apply merge a)


(Integer/parseInt (:id (first a)))




(defn load-well-numbers []
(table-to-map "resources/data/well_numbers_for_import.txt"))






(defn import-barcode-ids [ plateset-id barcode-file]

   " Loads table and make the association
      barcodess looks like:

      plate 	barcode.id
      1     	AMRVK5473H
      1      	KMNCX9294W
      1      	EHRXZ2102Z
      1      	COZHR7852Q
      1      	FJVNR6433Q"
    
  (let [ col1name (first (get-col-names barcode-file))
        col2name (first (rest (get-col-names barcode-file)))
        table (table-to-map barcode-file)
        sql-statement (str "UPDATE plate SET barcode = ? WHERE plate.ID IN ( SELECT plate.id FROM plate_set, plate_plate_set, plate  WHERE plate_plate_set.plate_set_id=" (str plateset-id) " AND plate_plate_set.plate_id=plate.id AND plate_plate_set.plate_order=? )")
        content (into [] (zipmap (map #(:barcode.id %) table) (map #(Integer. (:plate %)) table)))
        ]
    (if (and (= col1name "plate")(= col2name "barcode.id"))
      (with-open [con (j/get-connection cm/conn)
                  ps  (j/prepare con [sql-statement])]
        (p/execute-batch! ps content))    
      (javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", and \"barcode.id\", but found\n" col1name  ", and " col2name  "."  )))))

;; Diagnostic select:  select plate.id, plate.barcode, plate.plate_sys_name from plate, plate_set, plate_plate_set where plate_plate_set.plate_id=plate.id and plate_plate_set.plate_set_id=plate_set.id and plate_set.id=7 order by plate.barcode; 

