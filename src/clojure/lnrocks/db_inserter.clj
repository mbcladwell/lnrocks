(ns lnrocks.db-inserter
  (:require [clojure.string :only [split split-lines trim]]
            [crux.api :as crux]
            [clojure.set :as s]
          ;;  [incanter.stats :as is]
           
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
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



(defn new-project [ node prj-name desc ]
  (let [prj-id (:start (dbr/counter node :project 1))
        session-id (:session-id (crux/entity (crux/db node) :props))
        doc {:crux.db/id (keyword (str "prj" prj-id))
             :project-sys-name (str "PRJ-" prj-id)
             :name prj-name
             :description desc
             :lnsession-id session-id
             :id prj-id
             :user-id (:user-id (crux/entity (crux/db node) :props))
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
(defn persist-all-samples [ node unknown-n sample-start-id]
  (loop [ counter 1
         spl-id sample-start-id
         dummy (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                    :sample-sys-name (str "SPL-"  spl-id )
                                                    :id  spl-id
                                                    :accession nil}]])]
    (if (> counter  unknown-n )
      nil
      (recur
       (+ counter 1)
       (+ spl-id 1)
       (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                          :sample-sys-name (str "SPL-"  spl-id )
                                                          :id  spl-id
                                                          :accession nil}]])))))

(defn new-wells
  "format: 96, 384, 1535"
  [node format unknown-n with-samples sample-start-id]   
    (let [ 
          empty-wells (case format
                        96 util/map96wells
                        384 util/map384wells
                        1536 util/map1536wells)
          well-vec (case format
                     96 util/vec96wells
                     384 util/vec384wells
                     1536 util/vec1536wells)  ]
      (if with-samples
        (do
          (persist-all-samples node unknown-n sample-start-id )
        (loop [ counter 1
               spl-id sample-start-id
               filled-wells (assoc empty-wells (get well-vec (- counter 1)) (crux/entity (crux/db node) (keyword (str "spl"  spl-id))  ))]
           (if (> counter unknown-n )
            filled-wells
            (recur
             (+ counter 1)
             (+ spl-id 1)
             (assoc filled-wells (get well-vec (- counter 1)) (crux/entity (crux/db node) (keyword (str "spl"  spl-id))  ) )
               ))))
        (let  [empty-wells (case format
                      96 util/map96wells
                      384 util/map384wells
                      1536 util/map1536wells)]
      empty-wells))))


(defn new-plates
"return a set of the new plate ids
here layout-id is the crux id e.g. :lyt1
When creating the vector of start sample ids, need an extra because loop recur is going to create
the extra doc before failing at the if;  will fail due to null pointer if it can't make an extra plate"
  [ node all-ids layout-id num-plates with-samples]
  (let [
        layout (crux/entity (crux/db node) layout-id)
        unknown-n (:unknown-n layout)
        format (:format layout)
        ps-id  (keyword (str "ps" (:plate-set all-ids)))
        plt-id-start (:plate all-ids)
        spl-id-start (:sample all-ids)
        spl-start-vec (loop [counter 1
                             spl-id spl-id-start
                             myvec []]
                        (if (> counter (+  num-plates 1)) ;;need an extra element for the loop recur
                          myvec
                          (recur
                           (+ 1 counter)
                           (+ spl-id unknown-n)
                           (conj myvec spl-id))))
        user-id (:user-id (crux/entity (crux/db node) :props))
        ]
    (loop [
           counter 1
           plt-id plt-id-start
           doc {:crux.db/id (keyword (str "plt" plt-id))
                  :plate-sys-name (str "PLT-" plt-id)
                  :plate-set-id  ps-id
                  :id plt-id
                  :user-id user-id
                  :wells (new-wells node format unknown-n with-samples (get spl-start-vec (- counter 1)))
                  :plate-order counter
                }
           new-plates #{doc}
           dummy nil
      ]
      (if (> counter   (+ num-plates 1)   )
         new-plates
        (recur        
         (+ counter 1)
         (+ plt-id 1)
         {:crux.db/id (keyword (str "plt" plt-id))
                :plate-sys-name (str "PLT-" plt-id)
                :plate-set-id  ps-id
                :id plt-id
                :user-id user-id
                :wells (new-wells node format unknown-n with-samples (get spl-start-vec (- counter 1)))
                :plate-order counter
          }
         (conj new-plates doc)
         (crux/submit-tx node [[:crux.tx/put doc]])
         )))))


(defn new-plate-set [ node ps-name desc plate-format plate-type  plate-layout-name-id num-plates project-id  with-samples ]
  (let [
        layout (crux/entity (crux/db node) plate-layout-name-id)
        unknown-n (:unknown-n layout)    
        all-ids (dbr/get-ps-plt-spl-ids node  1 num-plates (* num-plates unknown-n) )
        id (:plate-set all-ids)
        ps-id (keyword (str "ps" id))
        session-id (:session-id (crux/entity (crux/db node) :props))
        doc {:crux.db/id ps-id
             :plate-set-sys-name (str "PS-" id)
             :plate-set-name ps-name
             :description desc
             :lnsession-id session-id
             :plate-format plate-format
             :plate-type plate-type
             :id id
             :user-id (:user-id (crux/entity (crux/db node) :props))
             :num-plates num-plates
             :project-id project-id
             :plates  (new-plates node all-ids plate-layout-name-id num-plates with-samples)
             :plate-layout-name-id plate-layout-name-id
             :worklist nil
             :assay-runs #{}
             }
        dummy (crux/submit-tx node [[:crux.tx/put doc]])
        old-prj (crux/entity (crux/db node)  project-id)
        new-prj (assoc old-prj :plate-sets   (conj (:plate-sets old-prj) doc  ))
        ]        
    (crux/submit-tx node [[:crux.tx/cas old-prj new-prj]])
    ps-id))


(defn process-layout-import-table
  ""
  [x]
  (into {} { 
            :well (Integer/parseInt(:well x ))
            :type  (Integer/parseInt(:type x ))
            :reps 1
            :target 1}))

;;(new-plate-layout nil "MyLayoutName" "1S1T" "scattered" 8 300 384 76 )
;;(new-plate-layout nil "MyLayoutName" "1S1T" "scattered" 4 92 96 0 )

(defn new-plate-layout
  ;;dest-layout-descr [["1S4T"]["2S2T"]["2S4T"]["4S1T"]["4S2T"]]
  ;;dest-template-layout-ids (if (= 96 source-format-id) [2 3 4 5 6] [14 15 16 17 18])
  [ node file-name  source-name  source-description control-loc n-controls n-unk source-format-id  n-edge ]
  (let [;;printer (.println (System/out) "in clojure new-plate-layout")
        ids (dbr/counter node :layout 6)
        table (util/table-to-map file-name) 
        src-doc {:id (:start ids)
             :name source-name
             :description source-description
             :control-loc control-loc
             :num-controls n-controls
             :format source-format-id
             :replicates 1
             :targets 1
             :layout (map #(process-layout-import-table %) table)
             :use-edge (if (> 0 n-edge) 0 1)
             :unknown-n n-unk
             :source-dest "source"
             :sys-name (str "LYT-" (:start ids))
             :crux.db/id (keyword (str "lyt"  (:start ids)))
             :dest #{(keyword (str "lyt"  (+ 1 (:start ids)))) ;;["1S4T"] 2  14
                     (keyword (str "lyt"  (+ 2 (:start ids))));; ["2S2T"] 3  15
                     (keyword (str "lyt"  (+ 3 (:start ids))));;["2S4T"] 4  16
                     (keyword (str "lyt"  (+ 4 (:start ids))));;["4S1T"] 5  17
                     (keyword (str "lyt"  (+ 5 (:start ids))))};;["4S2T"] 6 18
                     
                 }
        dummy (crux/submit-tx node [[:crux.tx/put src-doc]])
        dest-format (if (= 96 source-format-id) 384 1536)
        layout-template (case dest-format
                          384 (:layout (crux/entity (crux/db node) :lyt2 ))
                          1536 (:layout (crux/entity (crux/db node) :lyt14 )))
        dest-id (+ 1 (:start ids)) ;;["1S4T"]
        dest1-doc {:id dest-id
                   :name source-name
                   :description source-description
                   :control-loc control-loc
                   :num-controls n-controls
                   :format dest-format
                   :replicates 1
                   :targets 4
                   :layout layout-template
                   :use-edge (if (> 0 n-edge) 0 1)
                   :unknown-n n-unk
                   :source-dest "dest"
                   :sys-name (str "LYT-" dest-id)
                   :crux.db/id (keyword (str "lyt" dest-id))
                   :source (keyword (str "lyt"  (:start ids)))                     
                 }
        dummy (crux/submit-tx node [[:crux.tx/put dest1-doc]])
     
        layout-template (case dest-format
                          384 (:layout (crux/entity (crux/db node) :lyt3 ))
                          1536 (:layout (crux/entity (crux/db node) :lyt15 )))
        dest-id (+ 2 (:start ids)) ;;["2S2T"]
        dest2-doc {:id dest-id
                   :name source-name
                   :description source-description
                   :control-loc control-loc
                   :num-controls n-controls
                   :format dest-format
                   :replicates 2
                   :targets 2
                   :layout layout-template
                   :use-edge (if (> 0 n-edge) 0 1)
                   :unknown-n n-unk
                   :source-dest "dest"
                   :sys-name (str "LYT-" dest-id)
                   :crux.db/id (keyword (str "lyt" dest-id))
                   :source (keyword (str "lyt"  (:start ids)))                     
                 }
        dummy (crux/submit-tx node [[:crux.tx/put dest2-doc]])
   
        layout-template (case dest-format
                          384 (:layout (crux/entity (crux/db node) :lyt4 ))
                          1536 (:layout (crux/entity (crux/db node) :lyt16 )))
        dest-id (+ 3 (:start ids)) ;;["2S4T"]
        dest3-doc {:id dest-id
                   :name source-name
                   :description source-description
                   :control-loc control-loc
                   :num-controls n-controls
                   :format dest-format
                   :replicates 2
                   :targets 4
                   :layout layout-template
                   :use-edge (if (> 0 n-edge) 0 1)
                   :unknown-n n-unk
                   :source-dest "dest"
                   :sys-name (str "LYT-" dest-id)
                   :crux.db/id (keyword (str "lyt" dest-id))
                   :source (keyword (str "lyt"  (:start ids)))                     
                 }
        dummy (crux/submit-tx node [[:crux.tx/put dest3-doc]])

        layout-template (case dest-format
                          384 (:layout (crux/entity (crux/db node) :lyt5 ))
                          1536 (:layout (crux/entity (crux/db node) :lyt17 )))
        dest-id (+ 4 (:start ids)) ;;["4S1T"]
        dest4-doc {:id dest-id
                   :name source-name
                   :description source-description
                   :control-loc control-loc
                   :num-controls n-controls
                   :format dest-format
                   :replicates 4
                   :targets 1
                   :layout layout-template
                   :use-edge (if (> 0 n-edge) 0 1)
                   :unknown-n n-unk
                   :source-dest "dest"
                   :sys-name (str "LYT-" dest-id)
                   :crux.db/id (keyword (str "lyt" dest-id))
                   :source (keyword (str "lyt"  (:start ids)))                     
                 }
   dummy (crux/submit-tx node [[:crux.tx/put dest4-doc]])

               layout-template (case dest-format
                          384 (:layout (crux/entity (crux/db node) :lyt6 ))
                          1536 (:layout (crux/entity (crux/db node) :lyt18 )))
        dest-id (+ 5 (:start ids)) ;;["4S2T"]
        dest5-doc {:id dest-id
                   :name source-name
                   :description source-description
                   :control-loc control-loc
                   :num-controls n-controls
                   :format dest-format
                   :replicates 4
                   :targets 2
                   :layout layout-template
                   :use-edge (if (> 0 n-edge) 0 1)
                   :unknown-n n-unk
                   :source-dest "dest"
                   :sys-name (str "LYT-" dest-id)
                   :crux.db/id (keyword (str "lyt" dest-id))
                   :source (keyword (str "lyt"  (:start ids)))                     
                 }
        dummy (crux/submit-tx node [[:crux.tx/put dest5-doc]])

        ]
    (println (str (keyword (str "lyt"  (:start ids)))))
    ))
       


 

;; (defn get-all-plate-ids-for-plate-set-id [ plate-set-id]
;;   (let [ sql-statement "SELECT plate_id  FROM  plate_plate_set WHERE plate_plate_set.plate_set_id = ?;"
;;          plate-ids-pre (doall (j/execute! cm/conn [sql-statement plate-set-id]{:return-keys true}))
;;         ]
;;     (into [] (map :plate_plate_set/plate_id (flatten plate-ids-pre)))))

   
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;; (defn process-accs-map
;; ;;order is important; must correlate with SQL statement order of ?'s
;;   [x]
;; (into [] [(:accs.id x ) (Integer/parseInt(:plate x)) (Integer/parseInt(:well x ))]))


;; (defn import-accession-ids 
;;   " Loads table and make the association accessions looks like:
;;  plate	well	accs.id
;; 1	1	AMRVK5473H
;; 1	2	KMNCX9294W
;; 1	3	EHRXZ2102Z
;; 1	4	COZHR7852Q
;; 1	5	FJVNR6433Q
;; 1	6	WTCKQ4682U"
  
;; [ plateset-id accession-file]
;;   (let [ col1name (first (get-col-names accession-file))
;;         col2name (second (get-col-names accession-file))
;;         col3name (nth (get-col-names accession-file) 2)        
;;         table (table-to-map accession-file)
;;         content (into [] (map #(process-accs-map %) table))
;;         sql-statement (str "UPDATE sample SET accs_id = ? WHERE sample.ID IN ( SELECT sample.id FROM plate_set, plate_plate_set, plate, well, well_sample, sample WHERE plate_plate_set.plate_set_id=" (str plateset-id)   " AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID AND plate_plate_set.plate_order=? AND well.by_col=?)")
;;         ]
;;     (if (and (= col3name "accs.id")(= col1name "plate")(= col2name "well"))
;;       (with-open [con (j/get-connection cm/conn)
;;                   ps  (j/prepare con [sql-statement])]
;;         (p/execute-batch! ps content))    
;;       (javax.swing.JOptionPane/showMessageDialog nil  (str "Expecting the headers \"plate\", \"well\", and \"accs.id\", but found\n" col1name ", " col2name  ", and " col3name  "."  )))))



;; (defn assoc-plate-ids-with-plate-set-id
;;   "plate-ids: integer array of plate ids  int[]
;;   plate-set-id integer"
;;   [ plate-ids plate-set-id ]
;;   (let [
;;         sorted-plate-ids (sort plate-ids)
;;         plate-order (range 1 (+ 1 (count sorted-plate-ids)))
;;         content (pairs sorted-plate-ids plate-order)
;;         sql-statement (str "INSERT INTO plate_plate_set (plate_set_id, plate_id, plate_order) VALUES (" (str plate-set-id)", ?,?)")
;;         ]
;;       (with-open [con (j/get-connection cm/conn)
;;                   ps  (j/prepare con [sql-statement])]
;;         (p/execute-batch! ps content))    
;;       ))


;; (defn new-user
;;   ;;tags are any keyword
;;   ;; group-id is int
;;   [ user-name tags password group-id ]
;;   (let [ sql-statement (str "INSERT INTO lnuser(usergroup, lnuser_name, tags, password) VALUES (?, ?, ?, ?)")
;;         ]
;;   (j/execute-one! cm/conn [sql-statement group-id user-name tags password])))

;; (defn new-project-original
;;   ;;tags are any keyword
;;   ;; group-id is int
;;   [ project-name description lnuser-id ]
;;   (let [ sql-statement (str "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)")
;;         new-project-id-pre (j/execute-one! cm/conn [sql-statement description project-name lnuser-id]{:return-keys true})
;;         new-project-id (:project/id new-project-id-pre)
;;         ]
;;   (j/execute-one! cm/conn [(str "UPDATE project SET project_sys_name = " (str "'PRJ-" new-project-id "'") " WHERE id=?") new-project-id])))


;; ;;https://github.com/seancorfield/next-jdbc/blob/master/test/next/jdbc_test.clj#L53-L105

;; (defn get-ids-for-sys-names
;;   "sys_names vector of system_names
;;    table table to be queried
;;    column name of the sys_name column e.g. plate_sys_name, plate_set_sys_name
;;   execute-multi! not returning the result so this is a hack"
;;   [sys-names table column-name]
;;   (into [] (map :plate_set/id
;;        (flatten
;;         (let [ sql-statement (str "SELECT id FROM " table  "  WHERE " column-name  " = ?")
;;               ;;content (into [](map vec (partition 1  sys-names)))
;;               con (j/get-connection  cm/conn)
;;               ;;ps  (j/prepare con [sql-statement ])
;;               results nil]
;;            (for [x sys-names]  (concat results (j/execute! con  [sql-statement x])))
;;           )))))


;; ;;(get-ids-for-sys-names ["PS-1" "PS-2" "PS-3" "PS-4" ] "plate_set" "plate_set_sys_name" )
;; ;;(get-ids-for-sys-names ["PS-10"] "plate_set" "plate_set_sys_name" )





;; ;;must get rid of import file in Utilities.java

;; (defn create-assay-run
;;   " String _assayName,
;;       String _descr,
;;       int _assay_type_id,
;;       int _plate_set_id,
;;       int _plate_layout_name_id
;;   "
;;   [ assay-run-name description assay-type-id plate-set-id plate-layout-name-id ]
;;   (let [ session-id (cm/get-session-id)
;;         sql-statement1 (str "INSERT INTO assay_run(assay_run_name , descr, assay_type_id, plate_set_id, plate_layout_name_id, lnsession_id) VALUES (?, ?, ?, ?, ?, " session-id ")")
;;         sql-statement2 "UPDATE assay_run SET assay_run_sys_name = (SELECT CONCAT('AR-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
;;         sql-statement3 "SELECT LAST_INSERT_ID()"      
;;         new-assay-run-id-pre (j/with-transaction [tx cm/conn]
;;                                (j/execute! tx [sql-statement1 assay-run-name description assay-type-id plate-set-id plate-layout-name-id])
;;                                (j/execute! tx [sql-statement2])
;;                                (j/execute! tx [sql-statement3]))]
;;     (first (vals (first new-assay-run-id-pre))))) 

;; ;;(create-assay-run "n1" "d1" 1 1 1)


;; ;; used to process and  load manipulated maps
;; ;; defn process-assay-results-to-load
;; ;; "order is important; must correlate with SQL statement order of ?'s"
;; ;;   [x]
;; ;;  (into [] [(Integer/parseInt(:plate x )) (Integer/parseInt(:well x)) (Double/parseDouble(:response x ))  (Double/parseDouble(:bkgrnd_sub x )) (Double/parseDouble(:norm x )) (Double/parseDouble(:norm_pos x )) (Double/parseDouble(:p_enhance x ))]))


;; ;; (defn load-assay-results
;; ;;   [ assay-run-id data-table]
;; ;;   (let [ sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response ) VALUES ( " assay-run-id ", ?, ?, ?)")
;; ;;         content (into [] (map #(process-assay-results-map %) data-table))
;; ;;         ]
;; ;;       (with-open [con (j/get-connection cm/conn)
;; ;;                   ps  (j/prepare con [sql-statement])]
;; ;;         (p/execute-batch! ps content))))



;; (defn new-hit-list
;; "hit-list is a vector of integers"
;;   [ hit-list-name description number-of-hits assay-run-id hit-list]
;;   (let [
;;         lnsession-id (cm/get-session-id)
        
;;         sql-statement1 "INSERT INTO hit_list(hitlist_name, descr, n, assay_run_id, lnsession_id) VALUES (?,?,?,?,?)"
;;         sql-statement2 "UPDATE hit_list SET hitlist_sys_name = (SELECT CONCAT('HL-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
;;         sql-statement3 "SELECT LAST_INSERT_ID()"
;;         new-hit-list-id-pre (j/with-transaction [tx cm/conn]
;;                               (j/execute! tx [sql-statement1 hit-list-name  description  number-of-hits  assay-run-id lnsession-id])
;;                               (j/execute! tx [sql-statement2])
;;                               (j/execute! tx [sql-statement3])) 

;;         new-hit-list-id (first (vals (first new-hit-list-id-pre)))
        
;;         sql-statement4 (str "INSERT INTO hit_sample(hitlist_id, sample_id) VALUES(" (str new-hit-list-id) ", ?)")
;;         content (into [](map vector hit-list))
;;         ]  
;;     (with-open [con (j/get-connection cm/conn)
;;                 ps  (j/prepare con [sql-statement4])]
;;       (p/execute-batch! ps content))
;;      ))




;; (defn new-hit-list-old
;; "hit-list is a vector of integers"
;;   [ hit-list-name description number-of-hits assay-run-id hit-list]
;;   (let [
;;         lnsession-id (cm/get-session-id)
;;         sql-statement (str "INSERT INTO hit_list(hitlist_name, descr, n, assay_run_id, lnsession_id) VALUES ('" hit-list-name "', '" description "', " (str number-of-hits) ", " (str assay-run-id) ", " (str lnsession-id) ")")
;;         new-hit-list-id-pre (j/execute-one! cm/conn [sql-statement]{:return-keys true})
;;         new-hit-list-id (:hit_list/id new-hit-list-id-pre)
;;         sql-statement2 (str "UPDATE hit_list SET hitlist_sys_name = 'HL-" (str new-hit-list-id) "' WHERE id=" (str new-hit-list-id))
;;         dummy (j/execute-one! cm/conn [sql-statement2])
;;         sql-statement3 (str "INSERT INTO hit_sample(hitlist_id, sample_id) VALUES(" (str new-hit-list-id) ", ?)")
;;         content (into [](map vector hit-list))
;;         ]  
;;      (with-open [con (j/get-connection cm/conn)
;;                  ps  (j/prepare con [sql-statement3])]
;;       (p/execute-batch! ps content))
;;      ))



;; (defn process-rearray-map-to-load
;; "order is important; must correlate with SQL statement order of ?'s"
;;   [x]
;;  (into [] [ (:id x ) (:source_plate_sys_name x) (:source_by_col x ) (:plate_sys_name x ) (:by_col x )]))


;; (defn rearray-transfer-samples 
;;   "used during rearray process
;; first selection: select get in plate, well order, not necessarily sample order "
;;   [ source-plate-set-id  dest-plate-set-id  hit-list-id]
;;   (let [ sql-statement1 "SELECT  sample.id FROM plate_set, plate_plate_set, plate, well, well_sample, sample WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ? AND sample.ID  IN  (SELECT hit_sample.sample_id FROM hit_sample WHERE hit_sample.hitlist_id = ?) ORDER BY plate.ID, well.ID"
;;         all-hit-sample-ids  (first (sorted-set (proto/-execute-all cm/conn [ sql-statement1 source-plate-set-id hit-list-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )))
;;         num-hits (count all-hit-sample-ids)
;;         sql-statement2 "SELECT well.ID FROM plate_set, plate_plate_set, plate, well, plate_layout WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND plate_set.plate_layout_name_id=plate_layout.plate_layout_name_id AND plate_layout.well_by_col= well.by_col AND plate_set.id= ? AND plate_layout.well_type_id=1 ORDER BY well.ID"
;;         dest-wells (take num-hits (first (sorted-set (proto/-execute-all cm/conn [ sql-statement2 dest-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))))
;;         hit-well (pairs  (map :id dest-wells) (map :id all-hit-sample-ids))
;;         sql-statement3 " INSERT INTO well_sample (well_id, sample_id) VALUES (?,?)"
;;         a      (with-open [con (j/get-connection cm/conn)
;;                            ps  (j/prepare con [sql-statement3])]
;;                  (p/execute-batch! ps hit-well))
;;         sql-statement4 "INSERT INTO rearray_pairs (src, dest) VALUES (?,?)"
;;         rearray-pairs-id-pre (j/execute-one! cm/conn [sql-statement4 source-plate-set-id dest-plate-set-id]{:return-keys true}) 
;;         rearray-pairs-id (:rearray_pairs/id rearray-pairs-id-pre)
;;         sql-statement5 "SELECT  plate.plate_sys_name AS \"source_plate_sys_name\", well.by_col AS \"source_by_col\", sample.ID   FROM plate_set, plate_plate_set, plate, well, well_sample, sample  WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ?  AND sample.ID IN  (SELECT hit_sample.sample_id FROM hit_sample WHERE hit_sample.hitlist_id = ? ORDER BY sample.ID)"
;;         orig-plates-with-hits (set (proto/-execute-all cm/conn [ sql-statement5 source-plate-set-id hit-list-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
;;         sql-statement6 "SELECT plate.plate_sys_name, well.by_col, sample.ID  FROM plate_set, plate_plate_set, plate, well, well_sample, sample  WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id=sample.ID and plate_set.id= ?  ORDER BY sample.ID"
;;         new-plates-of-hits (set (proto/-execute-all cm/conn [ sql-statement6 dest-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
;;         joined-data (s/join orig-plates-with-hits new-plates-of-hits{:id :id})     
;;         sql-statement7 (str "INSERT INTO worklists ( rearray_pairs_id, sample_id, source_plate, source_well, dest_plate, dest_well) VALUES (" (str rearray-pairs-id) ", ?, ?, ?, ?, ? )")
;;         content (into [] (map #(process-rearray-map-to-load %) joined-data))
;;         ]
;;       (with-open [con (j/get-connection cm/conn)
;;                  ps  (j/prepare con [sql-statement7])]
;;       (p/execute-batch! ps content))))





;; ;; (defn reformat-plate-set-old
;; ;;   "Called from DialogReformatPlateSet OK action listener"
;; ;;   [source-plate-set-id  source-num-plates  n-reps-source  dest-descr  dest-plate-set-name  dest-num-plates  dest-plate-format-id  dest-plate-type-id  dest-plate-layout-name-id ]
;; ;;   (let [
;; ;;         project-id (cm/get-project-id)
;; ;;         dest-plate-set-id (new-plate-set dest-descr, dest-plate-set-name, dest-num-plates, dest-plate-format-id, dest-plate-type-id, project-id, dest-plate-layout-name-id, false )
;; ;;         sql-statement1 "select well.plate_id, plate_plate_set.plate_order, well.by_col, well.id AS source_well_id FROM plate_plate_set, well  WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = well.plate_id ORDER BY well.plate_id, well.ID"
;; ;;         source-plates (proto/-execute-all cm/conn [ sql-statement1 source-plate-set-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
;; ;;         rep-source-plates (loop [  counter 1 temp ()]
;; ;;                             (if (> counter n-reps-source)  temp
;; ;;                                 (recur   (+ 1 counter)
;; ;;                                          (concat (map #(assoc % :rep counter) source-plates) temp))))

       
;; ;;         sorted-source-pre    (sort-by (juxt :plate_id :rep :source_well_id)  rep-source-plates)
;; ;;         num  (count sorted-source-pre)
;; ;;          sorted-source (into [] (loop [  counter 0
;; ;;                                        new-set #{}
;; ;;                                        remaining sorted-source-pre]
;; ;;                                   (if (> counter  (- num 1 ))  new-set
;; ;;                                       (recur   (+ 1 counter)
;; ;;                                                (s/union new-set #{(assoc (first remaining) :sort-order counter)})
;; ;;                                                (rest remaining)))))
;; ;;         sql-statement2 "SELECT plate_plate_set.plate_ID, well.by_col,  well.id, well_numbers.well_name, well_numbers.quad  FROM well, plate_plate_set, well_numbers, plate_layout  WHERE plate_plate_set.plate_set_id = ?  AND plate_plate_set.plate_id = well.plate_id AND well_numbers.plate_format= ? AND well.by_col = well_numbers.by_col AND plate_layout.plate_layout_name_id = ? AND well.by_col=plate_layout.well_by_col AND plate_layout.well_type_id = 1 order by plate_id, quad, well_numbers.by_col"
;; ;;         dest-plates-unk-wells (proto/-execute-all cm/conn [ sql-statement2 dest-plate-set-id dest-plate-format-id dest-plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} )
;; ;;         sorted-dest-pre    (sort-by (juxt :plate_id :quad :by_col)  dest-plates-unk-wells)
;; ;;         num-dest  (count sorted-dest-pre)
;; ;;         sorted-dest (into [] (loop [  counter 0
;; ;;                                     new-set #{}
;; ;;                                     remaining sorted-dest-pre]
;; ;;                                (if (> counter  (- num-dest 1 ))  new-set
;; ;;                                    (recur   (+ 1 counter)
;; ;;                                             (s/union new-set #{(assoc (first remaining) :sort-order counter)})
;; ;;                                             (rest remaining)))))
;; ;;         joined-data  (s/join sorted-source sorted-dest {:sort-order :sort-order})
;; ;;         dwell-swell (map #(process-dwell-swell-to-load %) joined-data)
;; ;;         sql-statement3 "INSERT INTO well_sample (well_id, sample_id) VALUES ( ?, (SELECT sample.id FROM sample, well, well_sample WHERE well_sample.well_id=well.id AND well_sample.sample_id=sample.id AND well.id= ?))" 
;; ;;         ]
;; ;;     (with-open [con (j/get-connection cm/conn)
;; ;;                 ps  (j/prepare con [sql-statement3])]
;; ;;       (p/execute-batch! ps dwell-swell))
;; ;;     dest-plate-set-id))
;;  (defn process-dwell-sid-to-load
;;  "order is important; must correlate with SQL statement order of ?'s"
;;    [x]
;;   (into [] [ (:id x) (:sample_id x) ]))



;; (defn reformat-plate-set
;;   "Called from DialogReformatPlateSet OK action listener"
;;   [source-plate-set-id  source-num-plates  n-reps-source  dest-descr  dest-plate-set-name  dest-num-plates  dest-plate-format-id  dest-plate-type-id  dest-plate-layout-name-id ]
;;   (let [
;;         project-id (cm/get-project-id)
;;         dest-plate-set-id (new-plate-set dest-descr, dest-plate-set-name, dest-num-plates, dest-plate-format-id, dest-plate-type-id, project-id, dest-plate-layout-name-id, false )
;;         sql-statement1 "select well.plate_id, plate_plate_set.plate_order, well.by_col, well.id AS source_well_id, sample.id AS sample_id  FROM plate_plate_set, well, sample, well_sample  WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = well.plate_id AND well_sample.well_id=well.id AND well_sample.sample_id=sample.id  ORDER BY well.plate_id, well.ID"
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
;;         ;;id is dest well id and :source_well_id is source well id
;;         dwell-sid (map #(process-dwell-sid-to-load %) joined-data)
;;         sql-statement3 "INSERT INTO well_sample (well_id, sample_id) VALUES ( ?, ?)" 
;;         ]
;;    ;; (println dwell-sid)
;;     (with-open [con (j/get-connection cm/conn)
;;                 ps  (j/prepare con [sql-statement3])]
;;       (p/execute-batch! ps dwell-sid))
;;     dest-plate-set-id))


;; ;;(reformat-plate-set 3 2 1 "descr1" "reformatted PS3" 1 384 1 19)


;; (defn process-assay-results-to-load
;; "order is important; must correlate with SQL statement order of ?'s"
;;   [x]
;;  (into [] [(:plate x ) (:well x) (:response x ) (:bkgrnd_sub x ) (:norm x ) (:norm_pos x ) (:p_enhance x )]))



;; (defn process-plate-of-data
;;   ;;plate-data:
;;   ;;id: 
;;   [plate-data id]
;;         (let [ 
;;                 ;;plate-data (s/select #(= (:plate %) individual-plate) joined-data)
;;                 positives (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 2) plate-data))))
;;                 negatives (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 3) plate-data))))
;;                 background (is/mean (map #(get % :response)(into [](s/select #(= (:well_type_id %) 4) plate-data))))
;;                 unk-max (last(sort (map #(get % :response)(into [](s/select #(= (:well_type_id %) 1) plate-data)))))
;;               ]
;;            (loop [
;;                  processed-results-set #{}
;;                  well-number 1]
;;             (if (> well-number  (count plate-data));;once all wells processed
;;               processed-results-set
;;               (let [
;;                     response (:response (first (into [] (s/select #(= (:well %) well-number) plate-data))))
;;                     bkgrnd_sub (- response background)
;;                     norm  (/ response unk-max)
;;                     norm_pos (/ response positives)
;;                     p_enhance (* 100(- (/ (- response negatives) (- positives negatives)) 1))
;;                     ]
;;                    (recur 
;;                    (s/union  processed-results-set #{{:plate id :well well-number :response response :bkgrnd_sub bkgrnd_sub :norm norm :norm_pos norm_pos :p_enhance p_enhance}})
;;                    (+ 1 well-number)))))))




;; (defn process-assay-results-map
;; ;;used when manipulating maps before loading postgres
;;   [x]
;; (into {} { :plate (Integer/parseInt(:plate x )) :well (Integer/parseInt(:well x)) :response (Double/parseDouble(:response x ))}))

  
;; (defn associate-data-with-plate-set
;;   "  String _plate_set_sys_name,  a vector of sys-name; 
;;       int _top_n_number"
;;   [assay-run-name description plate-set-sys-names format-id assay-type-id plate-layout-name-id input-file-name auto-select-hits hit-selection-algorithm top-n-number]
;;  (let [ plate-set-ids (get-ids-for-sys-names plate-set-sys-names "plate_set" "plate_set_sys_name");;should only be one though method handles array 
;;        num-of-plate-ids (count (get-all-plate-ids-for-plate-set-id (first plate-set-ids))) ;;should be only one plate-set-id
;;        expected-rows-in-table  (* num-of-plate-ids format-id)
;;        table-map (table-to-map input-file-name)
;;        ]
;;    (if (= expected-rows-in-table (count table-map))
;;      ;;^^ first let determines if file is valid
;;      ;;plate-set-ids could be a vector with many but for this workflow only expecting one; get it out with (first)
;;      ;;vv second let does processing
;;      (let [
;;            sql-statement "SELECT well_by_col, well_type_id, replicates, target FROM plate_layout WHERE plate_layout_name_id =?"
;;            layout  (set (proto/-execute-all cm/conn [ sql-statement plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
;;            data (set (map #(process-assay-results-map %) (table-to-map input-file-name)))
;;            joined-data (s/join data layout {:well :well_by_col})
;;            num-plates   (count (distinct (map :plate  joined-data)))
;;            processed-plates  (loop [new-set #{}
;;                                     plate-counter 1]
;;                                (if (> plate-counter  num-plates) new-set
;;                                    (recur                                          
;;                                     (s/union new-set (process-plate-of-data
;;                                                       (s/select #(= (:plate %) plate-counter) joined-data)  plate-counter ))
;;                                     (+ 1 plate-counter))))        
;;            a (s/project processed-plates [:plate :well :response :bkgrnd_sub :norm :norm_pos :p_enhance])
;;            b (into [] a)
;;            content (into [] (map #(process-assay-results-to-load %) b))
;;            new-assay-run-id (create-assay-run  assay-run-name description assay-type-id (first plate-set-ids) plate-layout-name-id )
;;            sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response, bkgrnd_sub, norm, norm_pos, p_enhance ) VALUES ( "  new-assay-run-id ", ?, ?, ?, ?, ?, ?, ?)")
;;            ]                                  
;;            (with-open [con (j/get-connection cm/conn)
;;                               ps  (j/prepare con [sql-statement])]
;;              (p/execute-batch! ps content))
;; ;;          (println joined-data)
;;            new-assay-run-id  ;;must return new id for auto-select-hits when true
;;        ;;(clojure.pprint/pprint processed-plates)
;;        )      ;;end of second let
;;  (javax.swing.JOptionPane/showMessageDialog nil (str "Expecting " expected-rows-in-table " rows but found " (count table-map) " rows in data file." )) )));;row count is not correct

;; ;;(associate-data-with-plate-set "run1test" "test-desc" ["PS-2"] 96 1 1 "/home/mbc/sample96controls4lowp1.txt" true 1 10)






;;   (def dest-layout-descr [["1S4T"]["2S2T"]["2S4T"]["4S1T"]["4S2T"]] )
;;   (first (get dest-layout-descr 0))
      



;; (defn associate-data-with-plate-set2
;;   "  String _plate_set_sys_name,  a vector of sys-name; 
;;       int _top_n_number"
;;   [assay-run-name description plate-set-sys-names format-id assay-type-id plate-layout-name-id input-file-name auto-select-hits hit-selection-algorithm top-n-number]
;;  (let [ plate-set-ids (get-ids-for-sys-names plate-set-sys-names "plate_set" "plate_set_sys_name");;should only be one though method handles array 
;;        num-of-plate-ids (count (get-all-plate-ids-for-plate-set-id (first plate-set-ids))) ;;should be only one plate-set-id
;;        expected-rows-in-table  (* num-of-plate-ids format-id)
;;        table-map (table-to-map input-file-name)
;;        ]
;;    (if (= expected-rows-in-table (count table-map))
;;      ;;^^ first let determines if file is valid
;;      ;;plate-set-ids could be a vector with many but for this workflow only expecting one; get it out with (first)
;;      ;;vv second let does processing
;;      (let [
;;            sql-statement "SELECT well_by_col, well_type_id, replicates, target FROM plate_layout WHERE plate_layout_name_id =?"
;;            layout  (set (proto/-execute-all cm/conn [ sql-statement plate-layout-name-id]{:label-fn rs/as-unqualified-maps :builder-fn rs/as-unqualified-maps} ))
;;            data (set (map #(process-assay-results-map %) (table-to-map input-file-name)))
;;            joined-data (s/join data layout {:well :well_by_col})
;;            num-plates   (count (distinct (map :plate  joined-data)))
;;            processed-plates  (loop [new-set #{}
;;                                     plate-counter 1]
;;                                (if (> plate-counter  num-plates) new-set
;;                                    (recur                                          
;;                                     (s/union new-set (process-plate-of-data
;;                                                       (s/select #(= (:plate %) plate-counter) joined-data)  plate-counter ))
;;                                     (+ 1 plate-counter))))        
;;            a (s/project processed-plates [:plate :well :response :bkgrnd_sub :norm :norm_pos :p_enhance])
;;            b (into [] a)
;;            content (into [] (map #(process-assay-results-to-load %) b))
;;            new-assay-run-id (create-assay-run  assay-run-name description assay-type-id (first plate-set-ids) plate-layout-name-id )
;;            sql-statement (str "INSERT INTO assay_result( assay_run_id, plate_order, well, response, bkgrnd_sub, norm, norm_pos, p_enhance ) VALUES ( "  new-assay-run-id ", ?, ?, ?, ?, ?, ?, ?)")
;;            ]
;;        (clojure.pprint/pprint layout)
;;         ;;    (with-open [con (j/get-connection cm/conn)
;; ;;                               ps  (j/prepare con [sql-statement])]
;; ;;              (p/execute-batch! ps content))
;; ;; ;;          (println joined-data)
;; ;;            new-assay-run-id  ;;must return new id for auto-select-hits when true
;;        ;;(clojure.pprint/pprint processed-plates)
;;        )      ;;end of second let
;;  (javax.swing.JOptionPane/showMessageDialog nil (str "Expecting " expected-rows-in-table " rows but found " (count table-map) " rows in data file." )) )));;row count is not correct

;; ;;(associate-data-with-plate-set2 "run1test" "test-desc" ["PS-2"] 96 1 1 "/home/mbc/sample96controls4lowp1.txt" true 1 10)
;; ;;(associate-data-with-plate-set2 "assay_run4", "PS-4 LYT-13;384;8in24", ["PS-4"] 384, 1, 13, "/home/mbc/projects/ln/resources/raw_plate_data/ar4raw.txt" false nil nil)


;; ;; (defn new-project2
;; ;;   ;;tags are any keyword
;; ;;   ;; group-id is int
;; ;;   [ project-name description lnuser-id ]
;; ;;   (let [ sql-statement (str "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)")
;; ;;         a (j/execute-one! cm/conn [sql-statement description project-name lnuser-id])
        
;; ;;         ]
;; ;;   (j/execute-one! cm/conn [(str  ])))

;; ;;   (let [ sql-statement1 "INSERT INTO project(descr, project_name, lnsession_id) VALUES (?, ?, ?)"
;; ;;         sql-statement2 "UPDATE project SET project_sys_name = (SELECT CONCAT('PRJ-',  LAST_INSERT_ID()))  WHERE id= (SELECT LAST_INSERT_ID())"
       
;; ;;         ]
;; ;;   (j/with-transaction [tx cm/conn]
;; ;;   (j/execute! tx [sql-statement1 "p-desc" "p-name" 1])
;; ;;   (j/execute! tx [sql-statement2]))) 

;; (defn get-plate-layout-name-id-for-plate-set-id [plate-set-id]
;;   (let [
;;         sql-statement "SELECT plate_layout_name_id FROM plate_set WHERE id = ?;"
;;         result  (doall (j/execute! cm/conn [ sql-statement plate-set-id]{:return-keys true} ))
;;         ]
;;     (first(vals (first result)))))

;; ;;(get-plate-layout-name-id-for-plate-set-id 3)


;; (defn get-num-samples-for-plate-set-id [ plate-set-id ]
;;   (let [
;;         sql-statement  (str "SELECT sample.id FROM plate, plate_plate_set, well, sample, well_sample WHERE plate_plate_set.plate_set_id = ? AND plate_plate_set.plate_id = plate.id AND well.plate_id = plate.id AND well_sample.well_id = well.id AND well_sample.sample_id = sample.id ORDER BY plate_plate_set.plate_id, plate_plate_set.plate_order, well.id")
;;         result (doall (j/execute! cm/conn [ sql-statement plate-set-id]{:return-keys true} ))
;;         ]
;;     (count  result)))


;; ;; (defn prep-for-dialog-reformat-plate-set
;; ;;   "prepare data for the intermediate dialog DialogReformatPlateSet befor the reformat operation"
;; ;;   [ plate-set-sys-name descr num-plates plate-type formatv ]

;; ;;   (let [
;; ;;         plate-set-id (get-ids-for-sys-names plate-set-sys-name "plate_set" "plate_set_sys_name")
;; ;;         num-samples (get-num-samples-for-plate-set-id (first plate-set-id))
;; ;;         plate-layout-name-id (get-plate-layout-name-id-for-plate-set-id (first plate-set-id))
;; ;;         ]
;; ;;     (println (first plate-set-id))
;; ;;     (println (first plate-set-sys-name))
;; ;;     (println num-samples)
;; ;;     (println plate-layout-name-id)
;; ;;     (println formatv)
    
;; ;;    (case (str  formatv)
;; ;;      "96" (DialogReformatPlateSet.
;; ;;            nil
;; ;;            1
;; ;;            "PS-2"
;; ;;            "des" 2
;; ;;            184
;; ;;            "1"
;; ;;            "96"
;; ;;            1)
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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;example data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn eg-make-projects [node]
  (do 
    (new-project node "With AR, HL" "3 plate sets with 2 96 well plates each" )
    (Thread/sleep 200)
   (new-project node "With AR" "1 plate set with 2 384 well plates each" )
    (Thread/sleep 200)
   (new-project node "With AR" "1 plate set with 1 1536 well plate" )
    (Thread/sleep 200)
   (new-project node "MyTestProj4" "description4" )
    (Thread/sleep 200)
   (new-project node "MyTestProj5" "description5" )
    (Thread/sleep 200)
   (new-project node "MyTestProj6" "description6" )
    (Thread/sleep 200)
   (new-project node "MyTestProj7" "description7" )
    (Thread/sleep 200)
   (new-project node "MyTestProj8" "description8" )
    (Thread/sleep 200)
   (new-project node "MyTestProj9" "description9" )
    (Thread/sleep 200)
   (new-project node "Plates only, no data" "2 plate sets with 10 96 well plates each" )   
    (Thread/sleep 200)
   ))


(defn eg-make-plate-sets [node]
  (do
   (new-plate-set node "2 96 well plates" "with AR (low values), HL" 96 "assay" :lyt1 2 :prj1 true)
    (Thread/sleep 200)
   (new-plate-set node "2 96 well plates" "with AR (low values), HL" 96 "assay" :lyt1 2 :prj1 true)
    (Thread/sleep 200)
   (new-plate-set node "2 96 well plates" "with AR (high values), HL" 96 "assay" :lyt1 2 :prj1 true)
    (Thread/sleep 200)
   (new-plate-set node "2 384 well plates" "with AR (low values), HL" 384 "assay" :lyt13 2 :prj2 true)
    (Thread/sleep 200)
   (new-plate-set node "2 96 well plates" "with AR (low values), HL" 1536 "assay" :lyt37 1 :prj3 true)
    (Thread/sleep 200)
   (new-plate-set node "Plates only" "using LYT-1/96/4in12" 96 "assay" :lyt1 10 :prj10 true)
    (Thread/sleep 200)
   (new-plate-set node "Plates only" "using LYT-1/96/4in12" 96 "assay" :lyt1 10 :prj10 true)
    (Thread/sleep 200)
   ))
