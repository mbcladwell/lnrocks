(ns ln.db-inserter
  (:require [next.jdbc :as j]
            [next.jdbc.prepare :as p]
            [next.jdbc.result-set :as rs]
            [next.jdbc.protocols :as proto]
            [clojure.set :as s]
           ;; [honeysql.core :as hsql]
            [incanter.stats :as is]
           ;; [honeysql.helpers :refer :all :as helpers]
            [clojure.string :only [split split-lines trim]] 
            [ln.codax-manager :as cm])
          
  (:import java.sql.DriverManager javax.swing.JOptionPane ln.DialogReformatPlateSet))

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


(defn import-barcode-ids [ plateset-id barcode-file]
   " Loads table and make the association
      barcodes looks like:

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



(defn process-accs-map
;;order is important; must correlate with SQL statement order of ?'s
  [x]
(into [] [(:accs.id x ) (Integer/parseInt(:plate x)) (Integer/parseInt(:well x ))]))


(defn import-accession-ids 
  " Loads table and make the association accessions looks like:
 plate	well	accs.id
1	1	AMRVK5473H
1	2	KMNCX9294W
1	3	EHRXZ2102Z
1	4	COZHR7852Q
1	5	FJVNR6433Q
1	6	WTCKQ4682U"
  
[ plateset-id accession-file]
  (let [ col1name (first (get-col-names accession-file))
        col2name (second (get-col-names accession-file))
        col3name (nth (get-col-names accession-file) 2)        
        table (table-to-map accession-file)
        content (into [] (map #(process-accs-map %) table))
        sql-statement (str "UPDATE sample SET accs_id = ? WHERE sample.ID IN ( SELECT sample.id FROM plate_set, plate_plate_set, plate, well, well_sample, sample WHERE plate_plate_set.plate_set_id=" (str plateset-id)   " AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID AND plate_plate_set.plate_order=? AND well.by_col=?)")
        ]
    (if (and (= col3name "accs.id")(= col1name "plate")(= col2name "well"))
      (with-open [con (j/get-connection cm/conn)
                  ps  (j/prepare con [sql-statement])]
        (p/execute-batch! ps content))    
      (javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", \"well\", and \"accs.id\", but found\n" col1name ", " col2name  ", and " col3name  "."  )))))



(defn assoc-plate-ids-with-plate-set-id
  "plate-ids: integer array of plate ids  int[]
  plate-set-id integer"
  [ plate-ids plate-set-id ]
  (let [
        sorted-plate-ids (sort plate-ids)
        plate-order (range 1 (+ 1 (count sorted-plate-ids)))
        content (pairs sorted-plate-ids plate-order)
        sql-statement (str "INSERT INTO plate_plate_set (plate_set_id, plate_id, plate_order) VALUES (" (str plate-set-id)", ?,?)")
        ]
      (with-open [con (j/get-connection cm/conn)
                  ps  (j/prepare con [sql-statement])]
        (p/execute-batch! ps content))    
      ))


(defn new-user
  ;;tags are any keyword
  ;; group-id is int
  [ user-name tags password group-id ]
  (let [ sql-statement (str "INSERT INTO lnuser(usergroup, lnuser_name, tags, password) VALUES (?, ?, ?, ?)")
        ]
  (j/execute-one! cm/conn [sql-statement group-id user-name tags password])))

(defn new-project-original
  ;;tags are any keyword
  ;; group-id is int
  [ project-name description lnuser-id ]
  (let [ sql-statement (str "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)")
        new-project-id-pre (j/execute-one! cm/conn [sql-statement description project-name lnuser-id]{:return-keys true})
        new-project-id (:project/id new-project-id-pre)
        ]
  (j/execute-one! cm/conn [(str "UPDATE project SET project_sys_name = " (str "'PRJ-" new-project-id "'") " WHERE id=?") new-project-id])))

(defn new-project
  ;;tags are any keyword
  ;; group-id is int
  [ project-name description lnuser-id ]
  (let [ sql-statement1 "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)"
        sql-statement2 "UPDATE project SET project_sys_name = (SELECT CONCAT('PRJ-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"]
    (j/with-transaction [tx cm/conn]
      (j/execute! tx [sql-statement1 description project-name lnuser-id])
      (j/execute! tx [sql-statement2])
      (j/execute! tx [sql-statement3]))))



;;https://github.com/seancorfield/next-jdbc/blob/master/test/next/jdbc_test.clj#L53-L105

(defn get-ids-for-sys-names
  "sys_names vector of system_names
   table table to be queried
   column name of the sys_name column e.g. plate_sys_name, plate_set_sys_name
  execute-multi! not returning the result so this is a hack"
  [sys-names table column-name]
  (into [] (map :plate_set/id
       (flatten
        (let [ sql-statement (str "SELECT id FROM " table  "  WHERE " column-name  " = ?")
              ;;content (into [](map vec (partition 1  sys-names)))
              con (j/get-connection  cm/conn)
              ;;ps  (j/prepare con [sql-statement ])
              results nil]
           (for [x sys-names]  (concat results (j/execute! con  [sql-statement x])))
          )))))


;;(get-ids-for-sys-names ["PS-1" "PS-2" "PS-3" "PS-4" ] "plate_set" "plate_set_sys_name" )
;;(get-ids-for-sys-names ["PS-10"] "plate_set" "plate_set_sys_name" )



(defn get-all-plate-ids-for-plate-set-id [ plate-set-id]
  (let [ sql-statement "SELECT plate_id  FROM  plate_plate_set WHERE plate_plate_set.plate_set_id = ?;"
         plate-ids-pre (doall (j/execute! cm/conn [sql-statement plate-set-id]{:return-keys true}))
        ]
    (into [] (map :plate_plate_set/plate_id (flatten plate-ids-pre)))))


;;must get rid of import file in Utilities.java

(defn create-assay-run
  " String _assayName,
      String _descr,
      int _assay_type_id,
      int _plate_set_id,
      int _plate_layout_name_id
  "
  [ assay-run-name description assay-type-id plate-set-id plate-layout-name-id ]
  (let [ session-id (cm/get-session-id)
        sql-statement1 (str "INSERT INTO assay_run(assay_run_name , descr, assay_type_id, plate_set_id, plate_layout_name_id, lnsession_id) VALUES (?, ?, ?, ?, ?, " session-id ")")
        sql-statement2 "UPDATE assay_run SET assay_run_sys_name = (SELECT CONCAT('AR-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"      
        new-assay-run-id-pre (j/with-transaction [tx cm/conn]
                               (j/execute! tx [sql-statement1 assay-run-name description assay-type-id plate-set-id plate-layout-name-id])
                               (j/execute! tx [sql-statement2])
                               (j/execute! tx [sql-statement3]))]
    (first (vals (first new-assay-run-id-pre))))) 

;;(create-assay-run "n1" "d1" 1 1 1)


;; used to process and  load manipulated maps
;; defn process-assay-results-to-load
;; "order is important; must correlate with SQL statement order of ?'s"
;;   [x]
;;  (into [] [(Integer/parseInt(:plate x )) (Integer/parseInt(:well x)) (Double/parseDouble(:response x ))  (Double/parseDouble(:bkgrnd_sub x )) (Double/parseDouble(:norm x )) (Double/parseDouble(:norm_pos x )) (Double/parseDouble(:p_enhance x ))]))


;; (defn load-assay-results
;;   [ assay-run-id data-table]
;;   (let [ sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response ) VALUES ( " assay-run-id ", ?, ?, ?)")
;;         content (into [] (map #(process-assay-results-map %) data-table))
;;         ]
;;       (with-open [con (j/get-connection cm/conn)
;;                   ps  (j/prepare con [sql-statement])]
;;         (p/execute-batch! ps content))))


;; (defn new-project-demo
;;   ;;tags are any keyword
;;   ;; group-id is int
;;   [ project-name description lnuser-id ]
;;   (let [
;;         sql-statement1 "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)"
;;         sql-statement2 "UPDATE project SET project_sys_name = (SELECT CONCAT('PRJ-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
;;         sql-statement3 "SELECT LAST_INSERT_ID()"]
;;     new-hit-list-id-pre (j/with-transaction [tx cm/conn]
;;                           (j/execute! tx [sql-statement1 description project-name lnuser-id])
;;                           (j/execute! tx [sql-statement2])
;;                           (j/execute! tx [sql-statement3])))) 
;; (first (vals (first new-plate-set-id-pre)))

(defn new-hit-list
"hit-list is a vector of integers"
  [ hit-list-name description number-of-hits assay-run-id hit-list]
  (let [
        lnsession-id (cm/get-session-id)
        
        sql-statement1 "INSERT INTO hit_list(hitlist_name, descr, n, assay_run_id, lnsession_id) VALUES (?,?,?,?,?)"
        sql-statement2 "UPDATE hit_list SET hitlist_sys_name = (SELECT CONCAT('HL-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"
        new-hit-list-id-pre (j/with-transaction [tx cm/conn]
                              (j/execute! tx [sql-statement1 hit-list-name  description  number-of-hits  assay-run-id lnsession-id])
                              (j/execute! tx [sql-statement2])
                              (j/execute! tx [sql-statement3])) 

        new-hit-list-id (first (vals (first new-hit-list-id-pre)))
        
        sql-statement4 (str "INSERT INTO hit_sample(hitlist_id, sample_id) VALUES(" (str new-hit-list-id) ", ?)")
        content (into [](map vector hit-list))
        ]  
    (with-open [con (j/get-connection cm/conn)
                ps  (j/prepare con [sql-statement4])]
      (p/execute-batch! ps content))
     ))




(defn new-hit-list-old
"hit-list is a vector of integers"
  [ hit-list-name description number-of-hits assay-run-id hit-list]
  (let [
        lnsession-id (cm/get-session-id)
        sql-statement (str "INSERT INTO hit_list(hitlist_name, descr, n, assay_run_id, lnsession_id) VALUES ('" hit-list-name "', '" description "', " (str number-of-hits) ", " (str assay-run-id) ", " (str lnsession-id) ")")
        new-hit-list-id-pre (j/execute-one! cm/conn [sql-statement]{:return-keys true})
        new-hit-list-id (:hit_list/id new-hit-list-id-pre)
        sql-statement2 (str "UPDATE hit_list SET hitlist_sys_name = 'HL-" (str new-hit-list-id) "' WHERE id=" (str new-hit-list-id))
        dummy (j/execute-one! cm/conn [sql-statement2])
        sql-statement3 (str "INSERT INTO hit_sample(hitlist_id, sample_id) VALUES(" (str new-hit-list-id) ", ?)")
        content (into [](map vector hit-list))
        ]  
     (with-open [con (j/get-connection cm/conn)
                 ps  (j/prepare con [sql-statement3])]
      (p/execute-batch! ps content))
     ))



(defn process-rearray-map-to-load
"order is important; must correlate with SQL statement order of ?'s"
  [x]
 (into [] [ (:id x ) (:source_plate_sys_name x) (:source_by_col x ) (:plate_sys_name x ) (:by_col x )]))


(defn rearray-transfer-samples 
  "used during rearray process
first selection: select get in plate, well order, not necessarily sample order "
  [ source-plate-set-id  dest-plate-set-id  hit-list-id]
  (let [ sql-statement1 "SELECT  sample.id FROM plate_set, plate_plate_set, plate, well, well_sample, sample WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ? AND sample.ID  IN  (SELECT hit_sample.sample_id FROM hit_sample WHERE hit_sample.hitlist_id = ?) ORDER BY plate.ID, well.ID"
        all-hit-sample-ids  (first (sorted-set (proto/-execute-all cm/conn [ sql-statement1 source-plate-set-id hit-list-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )))
        num-hits (count all-hit-sample-ids)
        sql-statement2 "SELECT well.ID FROM plate_set, plate_plate_set, plate, well, plate_layout WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND plate_set.plate_layout_name_id=plate_layout.plate_layout_name_id AND plate_layout.well_by_col= well.by_col AND plate_set.id= ? AND plate_layout.well_type_id=1 ORDER BY well.ID"
        dest-wells (take num-hits (first (sorted-set (proto/-execute-all cm/conn [ sql-statement2 dest-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))))
        hit-well (pairs  (map :id dest-wells) (map :id all-hit-sample-ids))
        sql-statement3 " INSERT INTO well_sample (well_id, sample_id) VALUES (?,?)"
        a      (with-open [con (j/get-connection cm/conn)
                           ps  (j/prepare con [sql-statement3])]
                 (p/execute-batch! ps hit-well))
        sql-statement4 "INSERT INTO rearray_pairs (src, dest) VALUES (?,?)"
        rearray-pairs-id-pre (j/execute-one! cm/conn [sql-statement4 source-plate-set-id dest-plate-set-id]{:return-keys true}) 
        rearray-pairs-id (:rearray_pairs/id rearray-pairs-id-pre)
        sql-statement5 "SELECT  plate.plate_sys_name AS \"source_plate_sys_name\", well.by_col AS \"source_by_col\", sample.ID   FROM plate_set, plate_plate_set, plate, well, well_sample, sample  WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ?  AND sample.ID IN  (SELECT hit_sample.sample_id FROM hit_sample WHERE hit_sample.hitlist_id = ? ORDER BY sample.ID)"
        orig-plates-with-hits (set (proto/-execute-all cm/conn [ sql-statement5 source-plate-set-id hit-list-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
        sql-statement6 "SELECT plate.plate_sys_name, well.by_col, sample.ID  FROM plate_set, plate_plate_set, plate, well, well_sample, sample  WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ?  ORDER BY sample.ID"
        new-plates-of-hits (set (proto/-execute-all cm/conn [ sql-statement6 dest-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
        joined-data (s/join orig-plates-with-hits new-plates-of-hits{:id :id})     
        sql-statement7 (str "INSERT INTO worklists ( rearray_pairs_id, sample_id, source_plate, source_well, dest_plate, dest_well) VALUES (" (str rearray-pairs-id) ", ?, ?, ?, ?, ? )")
        content (into [] (map #(process-rearray-map-to-load %) joined-data))
        ]
      (with-open [con (j/get-connection cm/conn)
                 ps  (j/prepare con [sql-statement7])]
      (p/execute-batch! ps content))))


(defn new-sample
  "not using this during plate creation.  Batching instead."
  [ project-id  ]
  (let [
        sql-statement1 "INSERT INTO sample(project_id) VALUES (?)"
        sql-statement2 "UPDATE sample SET sample_sys_name = (SELECT CONCAT('SPL-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"
        ]
    (j/with-transaction [tx cm/conn]
      (j/execute! tx [sql-statement1 project-id])
      (j/execute! tx [sql-statement2])
      (j/execute! tx [sql-statement3])))) 
  
;;(vals (first (:LAST_INSERT_ID() (new-sample 1) )))

;;(first (vals (first (new-sample 1) )))




(defn new-plate
  "only add samples if include-samples is true"
  [plate-type-id plate-set-id plate-format-id plate-layout-name-id include-samples]
  (let [sql-statement1 "INSERT INTO plate(plate_type_id, plate_format_id, plate_layout_name_id) VALUES (?, ?, ?)"
        sql-statement2 "UPDATE plate SET plate_sys_name = (SELECT CONCAT('PLT-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"
        new-plate-id-pre (j/with-transaction [tx cm/conn]
                           (j/execute! tx [sql-statement1 plate-type-id plate-format-id plate-layout-name-id])
                           (j/execute! tx [sql-statement2])
                           (j/execute! tx [sql-statement3]))
        new-plate-id (first (vals (first new-plate-id-pre)))

        sql-statement4 (str "INSERT INTO well(by_col, plate_id) VALUES(?, " (str new-plate-id) ")")
        content (into [] (map vector (range 1 (+ 1 plate-format-id))))
        b  (with-open [con (j/get-connection cm/conn)
                       ps  (j/prepare con [sql-statement4])]
             (p/execute-batch! ps content))      
        ]
    (if (= include-samples true)
      (let [  sql-statement5  (str "SELECT well.id  FROM plate_layout, plate_layout_name, plate, well  WHERE plate_layout.plate_layout_name_id = plate_layout_name.id AND plate_layout.well_type_id = 1 AND well.plate_id=plate.id AND plate_layout.plate_layout_name_id = ? AND plate_layout.well_by_col=well.by_col AND plate.id= ?")
            wells-need-samples (into [] (map vector (map :id (first (sorted-set (proto/-execute-all cm/conn [ sql-statement5 plate-layout-name-id new-plate-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))))))
            project-id (cm/get-project-id)
            sql-statement6  "INSERT INTO sample( project_id, plate_id) VALUES(?, ?)"
            prj-plt (into []  (repeat (count wells-need-samples) [(cm/get-project-id) new-plate-id] ))
            c  (with-open [con (j/get-connection cm/conn)
                           ps  (j/prepare con [sql-statement6])]
                 (p/execute-batch! ps prj-plt))
            sql-statement7 "SELECT id FROM  sample WHERE  plate_id=?"
            new-sample-ids-pre (set (proto/-execute-all cm/conn [ sql-statement7 new-plate-id ]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
            new-sample-ids  (map :id new-sample-ids-pre)
            sql-statement8 "UPDATE sample SET sample_sys_name = CONCAT('SPL-', ?) WHERE id=?"
            content (into [] (pairs  (sort  new-sample-ids)  (sort  new-sample-ids)))
            d  (with-open [con (j/get-connection cm/conn)
                           ps  (j/prepare con [sql-statement8])]
                 (p/execute-batch! ps content)) 
            sql-statement9 "INSERT INTO well_sample(well_id, sample_id)VALUES(?,?)"
            well-sample-pairs (into [] (pairs  (flatten wells-need-samples)  (sort  new-sample-ids)))
            ]
        (with-open [con (j/get-connection cm/conn)
                    ps  (j/prepare con [sql-statement9])]
          (p/execute-batch! ps well-sample-pairs)) 

              ))new-plate-id))




;;(new-project 1)

;;(new-plate 1 50 96 1 true)


(defn new-plate-set [ description, plate-set-name, num-plates, plate-format-id, plate-type-id, project-id, plate-layout-name-id, with-samples ]
  (let [ lnsession-id (cm/get-session-id)
        sql-statement1 "INSERT INTO plate_set(descr, plate_set_name, num_plates, plate_format_id, plate_type_id, project_id, plate_layout_name_id, lnsession_id) VALUES (?, ?, ?, ?, ?, ?, ?, ? )"
        sql-statement2 "UPDATE plate_set SET plate_set_sys_name = (SELECT CONCAT('PS-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"       
    new-plate-set-id-pre  (j/with-transaction [tx cm/conn]
                            (j/execute! tx [sql-statement1 description plate-set-name num-plates plate-format-id plate-type-id project-id plate-layout-name-id lnsession-id])
                            (j/execute! tx [sql-statement2])
                            (j/execute! tx [sql-statement3]))
        new-plate-set-id (first (vals (first new-plate-set-id-pre)))
        ]
        (loop [
               new-plate-ids #{}
               plate-counter 1]
          (if (< plate-counter (+ 1 num-plates))
            (recur (s/union  new-plate-ids #{ (new-plate plate-type-id new-plate-set-id plate-format-id plate-layout-name-id with-samples)}) (inc plate-counter))
            (let [   sql-statement6 (str "INSERT INTO plate_plate_set(plate_set_id, plate_id, plate_order) VALUES(" (str new-plate-set-id) ",?,?)")
                  plate-id-order (into [] (pairs  (flatten (sort (map vector new-plate-ids))) (range 1 (+ 1 num-plates))))
                  ]
               (with-open [con (j/get-connection cm/conn)
                        ps  (j/prepare con [sql-statement6])]
              (p/execute-batch! ps plate-id-order)
              ))))   ;;remove 3
  new-plate-set-id))

;;(new-plate-set "des" "ps name" 3 96 1 1 1  false)




;; (defn reformat-plate-set-old
;;   "Called from DialogReformatPlateSet OK action listener"
;;   [source-plate-set-id  source-num-plates  n-reps-source  dest-descr  dest-plate-set-name  dest-num-plates  dest-plate-format-id  dest-plate-type-id  dest-plate-layout-name-id ]
;;   (let [
;;         project-id (cm/get-project-id)
;;         dest-plate-set-id (new-plate-set dest-descr, dest-plate-set-name, dest-num-plates, dest-plate-format-id, dest-plate-type-id, project-id, dest-plate-layout-name-id, false )
;;         sql-statement1 "select well.plate_id, plate_plate_set.plate_order, well.by_col, well.id AS source_well_id FROM plate_plate_set, well  WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = well.plate_id ORDER BY well.plate_id, well.ID"
;;         source-plates (proto/-execute-all cm/conn [ sql-statement1 source-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
;;         rep-source-plates (loop [  counter 1 temp ()]
;;                             (if (> counter n-reps-source)  temp
;;                                 (recur   (+ 1 counter)
;;                                          (concat (map #(assoc % :rep counter) source-plates) temp))))

       
;;         sorted-source-pre    (sort-by (juxt :plate_id :rep :source_well_id)  rep-source-plates)
;;         num  (count sorted-source-pre)
;;          sorted-source (into [] (loop [  counter 0
;;                                        new-set #{}
;;                                        remaining sorted-source-pre]
;;                                   (if (> counter  (- num 1 ))  new-set
;;                                       (recur   (+ 1 counter)
;;                                                (s/union new-set #{(assoc (first remaining) :sort-order counter)})
;;                                                (rest remaining)))))
;;         sql-statement2 "SELECT plate_plate_set.plate_ID, well.by_col,  well.id, well_numbers.well_name, well_numbers.quad  FROM well, plate_plate_set, well_numbers, plate_layout  WHERE plate_plate_set.plate_set_id = ?  AND plate_plate_set.plate_id = well.plate_id AND well_numbers.plate_format= ? AND well.by_col = well_numbers.by_col AND plate_layout.plate_layout_name_id = ? AND well.by_col=plate_layout.well_by_col AND plate_layout.well_type_id = 1 order by plate_id, quad, well_numbers.by_col"
;;         dest-plates-unk-wells (proto/-execute-all cm/conn [ sql-statement2 dest-plate-set-id dest-plate-format-id dest-plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
;;         sorted-dest-pre    (sort-by (juxt :plate_id :quad :by_col)  dest-plates-unk-wells)
;;         num-dest  (count sorted-dest-pre)
;;         sorted-dest (into [] (loop [  counter 0
;;                                     new-set #{}
;;                                     remaining sorted-dest-pre]
;;                                (if (> counter  (- num-dest 1 ))  new-set
;;                                    (recur   (+ 1 counter)
;;                                             (s/union new-set #{(assoc (first remaining) :sort-order counter)})
;;                                             (rest remaining)))))
;;         joined-data  (s/join sorted-source sorted-dest {:sort-order :sort-order})
;;         dwell-swell (map #(process-dwell-swell-to-load %) joined-data)
;;         sql-statement3 "INSERT INTO well_sample (well_id, sample_id) VALUES ( ?, (SELECT sample.id FROM sample, well, well_sample WHERE well_sample.well_id=well.id AND well_sample.sample_id=sample.id AND well.id= ?))" 
;;         ]
;;     (with-open [con (j/get-connection cm/conn)
;;                 ps  (j/prepare con [sql-statement3])]
;;       (p/execute-batch! ps dwell-swell))
;;     dest-plate-set-id))
 (defn process-dwell-sid-to-load
 "order is important; must correlate with SQL statement order of ?'s"
   [x]
  (into [] [ (:id x) (:sample_id x) ]))



(defn reformat-plate-set
  "Called from DialogReformatPlateSet OK action listener"
  [source-plate-set-id  source-num-plates  n-reps-source  dest-descr  dest-plate-set-name  dest-num-plates  dest-plate-format-id  dest-plate-type-id  dest-plate-layout-name-id ]
  (let [
        project-id (cm/get-project-id)
        dest-plate-set-id (new-plate-set dest-descr, dest-plate-set-name, dest-num-plates, dest-plate-format-id, dest-plate-type-id, project-id, dest-plate-layout-name-id, false )
        sql-statement1 "select well.plate_id, plate_plate_set.plate_order, well.by_col, well.id AS source_well_id, sample.id AS sample_id  FROM plate_plate_set, well, sample, well_sample  WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = well.plate_id AND well_sample.well_id=well.id AND well_sample.sample_id=sample.id  ORDER BY well.plate_id, well.ID"
        source-plates (proto/-execute-all cm/conn [ sql-statement1 source-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
        rep-source-plates (loop [  counter 1 temp ()]
                            (if (> counter n-reps-source)  temp
                                (recur   (+ 1 counter)
                                         (concat (map #(assoc % :rep counter) source-plates) temp))))

       
        sorted-source-pre    (sort-by (juxt :plate_id :rep :source_well_id)  rep-source-plates)
        num  (count sorted-source-pre)
         sorted-source (into [] (loop [  counter 0
                                       new-set #{}
                                       remaining sorted-source-pre]
                                  (if (> counter  (- num 1 ))  new-set
                                      (recur   (+ 1 counter)
                                               (s/union new-set #{(assoc (first remaining) :sort-order counter)})
                                               (rest remaining)))))
        sql-statement2 "SELECT plate_plate_set.plate_ID, well.by_col,  well.id, well_numbers.well_name, well_numbers.quad  FROM well, plate_plate_set, well_numbers, plate_layout  WHERE plate_plate_set.plate_set_id = ?  AND plate_plate_set.plate_id = well.plate_id AND well_numbers.plate_format= ? AND well.by_col = well_numbers.by_col AND plate_layout.plate_layout_name_id = ? AND well.by_col=plate_layout.well_by_col AND plate_layout.well_type_id = 1 order by plate_id, quad, well_numbers.by_col"
        dest-plates-unk-wells (proto/-execute-all cm/conn [ sql-statement2 dest-plate-set-id dest-plate-format-id dest-plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
        sorted-dest-pre    (sort-by (juxt :plate_id :quad :by_col)  dest-plates-unk-wells)
        num-dest  (count sorted-dest-pre)
        sorted-dest (into [] (loop [  counter 0
                                    new-set #{}
                                    remaining sorted-dest-pre]
                               (if (> counter  (- num-dest 1 ))  new-set
                                   (recur   (+ 1 counter)
                                            (s/union new-set #{(assoc (first remaining) :sort-order counter)})
                                            (rest remaining)))))
        joined-data  (s/join sorted-source sorted-dest {:sort-order :sort-order})
        ;;id is dest well id and :source_well_id is source well id
        dwell-sid (map #(process-dwell-sid-to-load %) joined-data)
        sql-statement3 "INSERT INTO well_sample (well_id, sample_id) VALUES ( ?, ?)" 
        ]
   ;; (println dwell-sid)
    (with-open [con (j/get-connection cm/conn)
                ps  (j/prepare con [sql-statement3])]
      (p/execute-batch! ps dwell-sid))
    dest-plate-set-id))


;;(reformat-plate-set 3 2 1 "descr1" "reformatted PS3" 1 384 1 19)


(defn process-assay-results-to-load
"order is important; must correlate with SQL statement order of ?'s"
  [x]
 (into [] [(:plate x ) (:well x) (:response x ) (:bkgrnd_sub x ) (:norm x ) (:norm_pos x ) (:p_enhance x )]))



(defn process-plate-of-data
  ;;plate-data:
  ;;id: 
  [plate-data id]
        (let [ 
                ;;plate-data (s/select #(= (:plate %) individual-plate) joined-data)
                positives (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 2) plate-data))))
                negatives (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 3) plate-data))))
                background (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 4) plate-data))))
                unk-max (last(sort (map #(get % :response)(into [](s/select #(= (:well_type_id %) 1) plate-data)))))
              ]
           (loop [
                 processed-results-set #{}
                 well-number 1]
            (if (> well-number  (count plate-data));;once all wells processed
              processed-results-set
              (let [
                    response (:response (first (into [] (s/select #(= (:well %) well-number) plate-data))))
                    bkgrnd_sub (- response background)
                    norm  (/ response unk-max)
                    norm_pos (/ response positives)
                    p_enhance (* 100(- (/ (- response negatives) (- positives negatives)) 1))
                    ]
                   (recur 
                   (s/union  processed-results-set #{{:plate id :well well-number :response response :bkgrnd_sub bkgrnd_sub :norm norm :norm_pos norm_pos :p_enhance p_enhance}})
                   (+ 1 well-number)))))))




(defn process-assay-results-map
;;used when manipulating maps before loading postgres
  [x]
(into {} { :plate (Integer/parseInt(:plate x )) :well (Integer/parseInt(:well x)) :response (Double/parseDouble(:response x ))}))

  
(defn associate-data-with-plate-set
  "  String _plate_set_sys_name,  a vector of sys-name; 
      int _top_n_number"
  [assay-run-name description plate-set-sys-names format-id assay-type-id plate-layout-name-id input-file-name auto-select-hits hit-selection-algorithm top-n-number]
 (let [ plate-set-ids (get-ids-for-sys-names plate-set-sys-names "plate_set" "plate_set_sys_name");;should only be one though method handles array 
       num-of-plate-ids (count (get-all-plate-ids-for-plate-set-id (first plate-set-ids))) ;;should be only one plate-set-id
       expected-rows-in-table  (* num-of-plate-ids format-id)
       table-map (table-to-map input-file-name)
       ]
   (if (= expected-rows-in-table (count table-map))
     ;;^^ first let determines if file is valid
     ;;plate-set-ids could be a vector with many but for this workflow only expecting one; get it out with (first)
     ;;vv second let does processing
     (let [
           sql-statement "SELECT well_by_col, well_type_id, replicates, target FROM plate_layout WHERE plate_layout_name_id =?"
           layout  (set (proto/-execute-all cm/conn [ sql-statement plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
           data (set (map #(process-assay-results-map %) (table-to-map input-file-name)))
           joined-data (s/join data layout {:well :well_by_col})
           num-plates   (count (distinct (map :plate  joined-data)))
           processed-plates  (loop [new-set #{}
                                    plate-counter 1]
                               (if (> plate-counter  num-plates) new-set
                                   (recur                                          
                                    (s/union new-set (process-plate-of-data
                                                      (s/select #(= (:plate %) plate-counter) joined-data)  plate-counter ))
                                    (+ 1 plate-counter))))        
           a (s/project processed-plates [:plate :well :response :bkgrnd_sub :norm :norm_pos :p_enhance])
           b (into [] a)
           content (into [] (map #(process-assay-results-to-load %) b))
           new-assay-run-id (create-assay-run  assay-run-name description assay-type-id (first plate-set-ids) plate-layout-name-id )
           sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response, bkgrnd_sub, norm, norm_pos, p_enhance ) VALUES ( "  new-assay-run-id ", ?, ?, ?, ?, ?, ?, ?)")
           ]                                  
           (with-open [con (j/get-connection cm/conn)
                              ps  (j/prepare con [sql-statement])]
             (p/execute-batch! ps content))
;;          (println joined-data)
           new-assay-run-id  ;;must return new id for auto-select-hits when true
       ;;(clojure.pprint/pprint processed-plates)
       )      ;;end of second let
 (javax.swing.JOptionPane/showMessageDialog nil (str "Expecting " expected-rows-in-table " rows but found " (count table-map) " rows in data file." )) )));;row count is not correct

;;(associate-data-with-plate-set "run1test" "test-desc" ["PS-2"] 96 1 1 "/home/mbc/sample96controls4lowp1.txt" true 1 10)


(defn process-source-layout
  "replication and target are 1"
  [x]
(let [ a (rest x)
      num (count a) ]
  (loop [ new-set #{}
         first-item (first a)
         remaining (rest a)
         counter 1]
    (if(= counter num) new-set   
       (recur     
        (s/union new-set #{[(Integer/parseInt (first first-item)) (Integer/parseInt (first(rest first-item))) 1 1]})
        (first remaining)
        (rest remaining)
        (+ 1 counter)
        )))) )




(defn new-plate-layout
"data is an array"
  [ data  source-name  source-description control-loc n-controls n-unk source-format-id  n-edge ]
  (let [printer (.println (System/out) "in clojure new-plate-layout")
        edge (if (> 0 n-edge) 0 1)
        dest-format (if (= 96 source-format-id) 384 1536)
        sql-statement1 (str "INSERT INTO plate_layout_name(name, descr, plate_format_id, replicates, targets, use_edge, num_controls, unknown_n, control_loc, source_dest) VALUES (?,?,?,?,?,?,?,?,?, 'source')")
        sql-statement2 "UPDATE plate_layout_name SET sys_name = (SELECT CONCAT('LYT-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
        sql-statement3 "SELECT LAST_INSERT_ID()"
    source-plate-layout-name-id-pre (j/with-transaction [tx cm/conn]
                                      (j/execute! tx [sql-statement1 source-name source-description source-format-id 1 1 edge n-controls n-unk control-loc])
                                      (j/execute! tx [sql-statement2])
                                      (j/execute! tx [sql-statement3]))         
       source-plate-layout-name-id  (first (vals (first source-plate-layout-name-id-pre)))
        ;;insert the source layout
       
        sql-statement4 (str "INSERT INTO plate_layout( plate_layout_name_id, well_by_col, well_type_id, replicates, target ) VALUES (" (str source-plate-layout-name-id )",?,?,?,?)")
        source-data (process-source-layout data)
        b            (with-open [con (j/get-connection cm/conn)
                              ps  (j/prepare con [sql-statement4])]
                       (p/execute-batch! ps source-data))
        dest-layout-descr [["1S4T"]["2S2T"]["2S4T"]["4S1T"]["4S2T"]]
        dest-template-layout-ids (if (= 96 source-format-id) [2 3 4 5 6] [14 15 16 17 18]) ;;if not 96 then 384 only options        
        sql-statement-name  "INSERT INTO plate_layout_name ( descr, plate_format_id, replicates, targets, use_edge, num_controls, unknown_n, control_loc, source_dest) VALUES ( ?, ?, 1, 1, ?, ?, ?, ?, 'dest')"
        sql-statement-name-update  "UPDATE plate_layout_name SET sys_name = (SELECT CONCAT('LYT-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"

        sql-statement-dplnid "SELECT @dest_plate_layout_name_id := LAST_INSERT_ID()"
        sql-statement-layout (str "INSERT INTO plate_layout (SELECT @dest_plate_layout_name_id AS \"plate_layout_name_id\", well_numbers.by_col AS \"well_by_col\", import_plate_layout.well_type_id, plate_layout.replicates, plate_layout.target FROM well_numbers, import_plate_layout, plate_layout WHERE well_numbers.plate_format = ? AND import_plate_layout.well_by_col=well_numbers.parent_well AND plate_layout.plate_layout_name_id= ?  AND plate_layout.well_by_col=well_numbers.by_col)")
        sql-statement-src-dest "INSERT INTO layout_source_dest (src, dest) VALUES (?, @dest_plate_layout_name_id)"
        ]
      (loop [
                 a nil
                 counter 0]
            (if (> counter  4);;once all wells processed
              nil
              (recur
               (j/with-transaction [tx cm/conn]
                 (j/execute! tx [sql-statement-name (first (get dest-layout-descr counter)) dest-format edge n-controls n-unk control-loc ])
                 (j/execute! tx [sql-statement-name-update])
                 (j/execute! tx [sql-statement-dplnid])               
                 (j/execute! tx [sql-statement-layout dest-format (get dest-template-layout-ids counter) ])
                 (j/execute! tx [sql-statement-src-dest source-plate-layout-name-id ])) 
                   (+ 1 counter))))
       source-plate-layout-name-id ;;must return
    )) 

;;(new-plate-layout a "MyLayoutName" "1S1T" "scattered" 8 300 384 76 )

  (def dest-layout-descr [["1S4T"]["2S2T"]["2S4T"]["4S1T"]["4S2T"]] )
  (first (get dest-layout-descr 0))
      


  
(defn new-plate-layout-old
"data is an array"
  [ data  source-name  source-description control-loc n-controls n-unk source-format-id  n-edge ]
  (let [printer (.println (System/out) "in clojure new-plate-layout")
        edge (if (> 0 n-edge) 0 1)
        dest-template-layout-ids (if (= 96 source-format-id) [2 3 4 5 6] [14 15 16 17 18]) ;;if not 96 then 384 only options
        dest-format (if (= 96 source-format-id) 384 1536)
        sql-statement1 (str "INSERT INTO plate_layout_name(name, descr, plate_format_id, replicates, targets, use_edge, num_controls, unknown_n, control_loc, source_dest) VALUES (?,?,?,?,?,?,?,?,?, 'source')")
        source-plate-layout-name-id-pre (j/execute-one! cm/conn [sql-statement1 source-name source-description source-format-id 1 1 edge n-controls n-unk control-loc ]{:return-keys true})
       source-plate-layout-name-id (:plate_layout_name/id source-plate-layout-name-id-pre)
        sql-statement2  "UPDATE plate_layout_name SET sys_name = CONCAT('LYT-', ?) WHERE id=?" 
        a (j/execute-one! cm/conn [sql-statement2 source-plate-layout-name-id source-plate-layout-name-id])
        ;;insert the source layout
       
        sql-statement3 (str "INSERT INTO plate_layout( plate_layout_name_id, well_by_col, well_type_id, replicates, target ) VALUES (" (str source-plate-layout-name-id )",?,?,?,?)")
        source-data (process-source-layout data)
        b            (with-open [con (j/get-connection cm/conn)
                              ps  (j/prepare con [sql-statement3])]
                       (p/execute-batch! ps source-data))
        sql-statement-name  "INSERT INTO plate_layout_name ( descr, plate_format_id, replicates, targets, use_edge, num_controls, unknown_n, control_loc, source_dest) VALUES ( ?, ?, 1, 1, ?, ?, ?, ?, 'dest')"
        sql-statement-name-update "UPDATE plate_layout_name SET sys_name = CONCAT('LYT-',?) WHERE id=?"
        sql-statement-layout (str "INSERT INTO plate_layout (SELECT ? AS \"plate_layout_name_id\", well_numbers.by_col AS \"well_by_col\", import_plate_layout.well_type_id, plate_layout.replicates, plate_layout.target FROM well_numbers, import_plate_layout, plate_layout WHERE well_numbers.plate_format = ? AND import_plate_layout.well_by_col=well_numbers.parent_well AND plate_layout.plate_layout_name_id= ?  AND plate_layout.well_by_col=well_numbers.by_col)")
        sql-statement-src-dest "INSERT INTO layout_source_dest (src, dest) VALUES (?,?)"
        ;;tried to do this with loop recur but id always lags
        dest-layout-descr [["1S4T"]["2S2T"]["2S4T"]["4S1T"]["4S2T"]]
        dl-descr-first (first dest-layout-descr);;this is a vector so another first to get the string
        dest-id1 (:plate_layout_name/id (j/execute-one! cm/conn [sql-statement-name "1S4T" dest-format edge n-controls n-unk control-loc ]{:return-keys true}))
        c (j/execute-one! cm/conn [sql-statement-name-update dest-id1 dest-id1])
        d (j/execute-one! cm/conn [sql-statement-layout dest-id1 dest-format  (first dest-template-layout-ids) ])

          dest-id2 (:plate_layout_name/id (j/execute-one! cm/conn [sql-statement-name "2S2T" dest-format edge n-controls n-unk control-loc ]{:return-keys true}))
        e (j/execute-one! cm/conn [sql-statement-name-update dest-id2 dest-id2])
        f (j/execute-one! cm/conn [sql-statement-layout dest-id2 dest-format  (first (rest dest-template-layout-ids)) ])

          dest-id3 (:plate_layout_name/id (j/execute-one! cm/conn [sql-statement-name "2S4T" dest-format edge n-controls n-unk control-loc ]{:return-keys true}))
        g (j/execute-one! cm/conn [sql-statement-name-update dest-id3 dest-id3])
        h (j/execute-one! cm/conn [sql-statement-layout dest-id3 dest-format  (first (rest (rest dest-template-layout-ids))) ])

          dest-id4 (:plate_layout_name/id (j/execute-one! cm/conn [sql-statement-name "4S1T" dest-format edge n-controls n-unk control-loc ]{:return-keys true}))
        i (j/execute-one! cm/conn [sql-statement-name-update dest-id4 dest-id4])
        j (j/execute-one! cm/conn [sql-statement-layout dest-id4 dest-format  (first (rest (rest (rest dest-template-layout-ids)))) ])

          dest-id5 (:plate_layout_name/id (j/execute-one! cm/conn [sql-statement-name "4S2T" dest-format edge n-controls n-unk control-loc ]{:return-keys true}))
        k (j/execute-one! cm/conn [sql-statement-name-update dest-id5 dest-id5])
        l (j/execute-one! cm/conn [sql-statement-layout dest-id5 dest-format  (first (rest (rest (rest (rest dest-template-layout-ids))))) ])
        content [[source-plate-layout-name-id dest-id1 ][source-plate-layout-name-id dest-id2][source-plate-layout-name-id dest-id3 ][source-plate-layout-name-id  dest-id4 ][source-plate-layout-name-id dest-id5]]
        ]
        (with-open [con (j/get-connection cm/conn)
                              ps  (j/prepare con [sql-statement-src-dest])]
          (p/execute-batch! ps content))
        source-plate-layout-name-id
    )) 

;;(new-plate-layout a "MyLayoutName" "1S1T" "scattered" 8 300 384 76 )

(defn associate-data-with-plate-set2
  "  String _plate_set_sys_name,  a vector of sys-name; 
      int _top_n_number"
  [assay-run-name description plate-set-sys-names format-id assay-type-id plate-layout-name-id input-file-name auto-select-hits hit-selection-algorithm top-n-number]
 (let [ plate-set-ids (get-ids-for-sys-names plate-set-sys-names "plate_set" "plate_set_sys_name");;should only be one though method handles array 
       num-of-plate-ids (count (get-all-plate-ids-for-plate-set-id (first plate-set-ids))) ;;should be only one plate-set-id
       expected-rows-in-table  (* num-of-plate-ids format-id)
       table-map (table-to-map input-file-name)
       ]
   (if (= expected-rows-in-table (count table-map))
     ;;^^ first let determines if file is valid
     ;;plate-set-ids could be a vector with many but for this workflow only expecting one; get it out with (first)
     ;;vv second let does processing
     (let [
           sql-statement "SELECT well_by_col, well_type_id, replicates, target FROM plate_layout WHERE plate_layout_name_id =?"
           layout  (set (proto/-execute-all cm/conn [ sql-statement plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
           data (set (map #(process-assay-results-map %) (table-to-map input-file-name)))
           joined-data (s/join data layout {:well :well_by_col})
           num-plates   (count (distinct (map :plate  joined-data)))
           processed-plates  (loop [new-set #{}
                                    plate-counter 1]
                               (if (> plate-counter  num-plates) new-set
                                   (recur                                          
                                    (s/union new-set (process-plate-of-data
                                                      (s/select #(= (:plate %) plate-counter) joined-data)  plate-counter ))
                                    (+ 1 plate-counter))))        
           a (s/project processed-plates [:plate :well :response :bkgrnd_sub :norm :norm_pos :p_enhance])
           b (into [] a)
           content (into [] (map #(process-assay-results-to-load %) b))
           new-assay-run-id (create-assay-run  assay-run-name description assay-type-id (first plate-set-ids) plate-layout-name-id )
           sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response, bkgrnd_sub, norm, norm_pos, p_enhance ) VALUES ( "  new-assay-run-id ", ?, ?, ?, ?, ?, ?, ?)")
           ]
       (clojure.pprint/pprint layout)
        ;;    (with-open [con (j/get-connection cm/conn)
;;                               ps  (j/prepare con [sql-statement])]
;;              (p/execute-batch! ps content))
;; ;;          (println joined-data)
;;            new-assay-run-id  ;;must return new id for auto-select-hits when true
       ;;(clojure.pprint/pprint processed-plates)
       )      ;;end of second let
 (javax.swing.JOptionPane/showMessageDialog nil (str "Expecting " expected-rows-in-table " rows but found " (count table-map) " rows in data file." )) )));;row count is not correct

;;(associate-data-with-plate-set2 "run1test" "test-desc" ["PS-2"] 96 1 1 "/home/mbc/sample96controls4lowp1.txt" true 1 10)
;;(associate-data-with-plate-set2 "assay_run4", "PS-4 LYT-13;384;8in24", ["PS-4"] 384, 1, 13, "/home/mbc/projects/ln/resources/raw_plate_data/ar4raw.txt" false nil nil)


;; (defn new-project2
;;   ;;tags are any keyword
;;   ;; group-id is int
;;   [ project-name description lnuser-id ]
;;   (let [ sql-statement (str "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)")
;;         a (j/execute-one! cm/conn [sql-statement description project-name lnuser-id])
        
;;         ]
;;   (j/execute-one! cm/conn [(str  ])))

;;   (let [ sql-statement1 "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)"
;;         sql-statement2 "UPDATE project SET project_sys_name = (SELECT CONCAT('PRJ-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
       
;;         ]
;;   (j/with-transaction [tx cm/conn]
;;   (j/execute! tx [sql-statement1 "p-desc" "p-name" 1])
;;   (j/execute! tx [sql-statement2]))) 

(defn get-plate-layout-name-id-for-plate-set-id [plate-set-id]
  (let [
        sql-statement "SELECT plate_layout_name_id FROM plate_set WHERE id = ?;"
        result  (doall (j/execute! cm/conn [ sql-statement plate-set-id]{:return-keys true} ))
        ]
    (first(vals (first result)))))

;;(get-plate-layout-name-id-for-plate-set-id 3)


(defn get-num-samples-for-plate-set-id [ plate-set-id ]
  (let [
        sql-statement  (str "SELECT sample.id FROM plate, plate_plate_set, well, sample, well_sample WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = plate.id AND well.plate_id = plate.id AND well_sample.well_id = well.id AND well_sample.sample_id = sample.id ORDER BY plate_plate_set.plate_id, plate_plate_set.plate_order, well.id")
        result (doall (j/execute! cm/conn [ sql-statement plate-set-id]{:return-keys true} ))
        ]
    (count  result)))


;; (defn prep-for-dialog-reformat-plate-set
;;   "prepare data for the intermediate dialog DialogReformatPlateSet befor the reformat operation"
;;   [ plate-set-sys-name descr num-plates plate-type formatv ]

;;   (let [
;;         plate-set-id (get-ids-for-sys-names plate-set-sys-name "plate_set" "plate_set_sys_name")
;;         num-samples (get-num-samples-for-plate-set-id (first plate-set-id))
;;         plate-layout-name-id (get-plate-layout-name-id-for-plate-set-id (first plate-set-id))
;;         ]
;;     (println (first plate-set-id))
;;     (println (first plate-set-sys-name))
;;     (println num-samples)
;;     (println plate-layout-name-id)
;;     (println formatv)
    
;;    (case (str  formatv)
;;      "96" (DialogReformatPlateSet.
;;            nil
;;            1
;;            "PS-2"
;;            "des" 2
;;            184
;;            "1"
;;            "96"
;;            1)
;;     ;  "384" (ln.DialogReformatPlateSet. nil (first plate-set-id) (first plate-set-sys-name) descr num-plates  num-samples  plate-type  formatv  plate-layout-name-id)
;;       "1536" (JOptionPane/showMessageDialog  "1536 well plates can not be reformatted." "Error"))

;;     )

;;   )

;;(prep-for-dialog-reformat-plate-set ["PS-1"] "des" 2 1 "96")

;;(ln.DialogReformatPlateSet.
  ;;   nil
     ;      1
      ;     "PS-1"
       ;    "des"
        ;   2
         ;  184
          ; "1"
           ;"96"
          ; 1)
