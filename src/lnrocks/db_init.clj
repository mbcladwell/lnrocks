(ns lnrocks.db-init
  (:require
   ;;[clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [crux.api :as crux]
            [lnrocks.core :as core]
            [lnrocks.db-inserter :as dbi]
            )
  (:import [crux.api ICruxAPI])
             (:gen-class))

    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Database setup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn open-or-create-db
  ;;1. check working directory - /home/user/my-working-dir
  ;;2. check home directory      /home/user
  []
  (if (.exists (io/as-file "ln-props"))
    (def props (c/open-database! "ln-props"))  
    (if (.exists (io/as-file (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (def props (c/open-database! (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (if (.exists (io/as-file (str (java.lang.System/getProperty "user.dir") "/limsnucleus.properties") ))
        (do
          (create-ln-props-from-text)
          (def props (c/open-database! "ln-props")))
        (do            ;;no limsnucleus.properties - login to elephantSQL
          (login-to-elephantsql)
          (def props (c/open-database! "ln-props"))
          (JOptionPane/showMessageDialog nil "limsnucleus.properties file is missing\nLogging in to example database!"  )
          )))))


(open-or-create-db)




(def counters
 {:crux.db/id :counters
  :project 0
  :plate-set 0
  :plate 0
  :sample 0
  :hit-list 0
  :work-list 0}
  )

(crux/submit-tx core/node [[:crux.tx/put counters]] )




(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))


(def props
  {:crux.db/id :props
   :help-url-prefix "http://labsolns.com/software/"
   :session-id 1
   :project-id 1
   :project-sys-name "PRJ-1"
   :plate-set-sys-name "PS-1"
   :plate-set-id 1})
  
(crux/submit-tx node [[:crux.tx/put props]] )

(def helpers
  [{:crux.db/id :plate-formats :96 96 :384 384 :1536 1536}
   {:crux.db/id :plate-type 1 "assay" 2 "rearray" 3 "master" 4 "daughter" 5 "archive" 6 "replicate"}
   {:crux.db/id :plate-layout :plate-layout (dbi/load-plate-layouts)}
   {:crux.db/id :assay-type  1 "ELISA" 2 "Octet" 3 "SNP" 4 "HCS" 5 "HTRF" 6 "FACS"}
   {:crux.db/id :well-type  1 "unknown" 2 "positive" 3 "negative" 4 "blank" 5 "edge"}
   {:crux.db/id :well-numbers :well-numbers (dbi/load-well-numbers) }
   {:crux.db/id :layout-src-dest    [{:source 1 :dest  2}{:source 1 :dest 3}{:source 1 :dest 4}{:source 1 :dest 5}{:source 1 :dest 6}{:source 7 :dest 8}{:source 7 :dest 9}{:source 7 :dest 10}{:source 7 :dest 11}{:source 7 :dest 12}{:source 13 :dest 14}{:source 13 :dest 15}{:source 13 :dest 16}{:source 13 :dest 17}{:source 13 :dest 18}{:source 19 :dest 20}{:source 19 :dest 21}{:source 19 :dest 22}{:source 19 :dest 23}{:source 19 :dest 24}{:source 25 :dest 26}{:source 25 :dest 27}{:source 25 :dest 28}{:source 25 :dest 29}{:source 25 :dest 30}{:source 31 :dest 32}{:source 31 :dest 33}{:source 31 :dest 34}{:source 31 :dest 35}{:source 31 :dest 36}{:source 37 :dest 41}{:source 38 :dest 41}{:source 39 :dest 41}{:source 40 :dest 41}]
   ])



;; (easy-ingest core/node helpers)

(defn get-well-numbers
  ;;x: 96, 384, or 1536
  [x]
  (filter #(= (:format %) x) (:well-numbers  (crux/entity (crux/db core/node ) :well-numbers))))

;;(get-well-numbers 96)


  ;;(:unknown-n (first (get-plate-layout 2)))

  (def example-data
 [{:crux.db/id :projects

    )


  

;;     [(jdbc/create-table-ddl :hit_list
;;                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
;;                            [:hitlist_sys_name "varchar(30)"]
;;                            [:hitlist_name "varchar(250)"]
;;                            [:descr "varchar(250)"]
;;                             [:n :int]
;;                            [:lnsession_id :int]
;;                            [:assay_run_id :int]
;;                             [:updated  :timestamp ]               
;;                            ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id)"]
;;                            ["FOREIGN KEY (assay_run_id) REFERENCES assay_run(id)"]
;; 		           ])]
;;   [(jdbc/create-table-ddl :hit_sample
;;                            [[:hitlist_id :int "NOT NULL"]
;;                             [:sample_id :int "NOT NULL"]
;;                             ["FOREIGN KEY (hitlist_id) REFERENCES hit_list(id)  ON DELETE cascade"]
;;                             ["FOREIGN KEY (sample_id) REFERENCES sample(id)  ON DELETE cascade"]
;;                             ])]

     
     
;;         [(jdbc/create-table-ddl :rearray_pairs
;;                                 [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
;;                                  [:src :int]
;;                                  [:dest :int]
;;                                  ])]
 
;;        [(jdbc/create-table-ddl :worklists
;;                           [   [:rearray_pairs_id :int]
;;                             [:sample_id :int]
;;                            [:source_plate "varchar(10)"]
;;                            [:source_well :int]
;;                            [:dest_plate "varchar(10)"]
;;                            [:dest_well :int]
;;                            ["FOREIGN KEY (rearray_pairs_id) REFERENCES rearray_pairs(id)  ON DELETE cascade"]
;;                            ["FOREIGN KEY (sample_id) REFERENCES sample(id)"]
;;                            ])]


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Required data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-layout-data
  "processes that tab delimitted, R generated layouts for import
   order is important; must correlate with SQL statement order of ?'s"
  [x]
(into [] [ (Integer/parseInt(:id x)) (Integer/parseInt(:well x )) (Integer/parseInt(:type x )) (Integer/parseInt(:reps x )) (Integer/parseInt(:target x ))]))

(defn process-well-numbers-data
  "processes that tab delimitted, R generated well_numbers for import
because some are strings, all imported as string
   order is important; must correlate with SQL statement order of ?'s"
  [x]
  (into [] [ (Integer/parseInt (String. (:format x)))
            (:wellname x )
            (:row_name x )
            (Integer/parseInt (String. (:rownum x )))
            (Integer/parseInt (String. (:col x )))
            (Integer/parseInt (String. (:totcolcount x)))
            (Integer/parseInt (String. (:byrow x )))
            (Integer/parseInt (String. (:bycol x )))
            (Integer/parseInt (String. (:quad x )))
            (Integer/parseInt (String. (:parentwell x ))) ]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Optional example data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn add-projects [ ]
  (do
                   (dbi/new-project "3 plate sets with 2 96 well plates each", "With AR, HL", 1 ) 
                   (dbi/new-project "1 plate set with 2 384 well plates each", "With AR", 1 ) 
                   (dbi/new-project "1 plate set with 1 1536 well plate", "With AR", 1 ) 
                   (dbi/new-project "description 4", "MyTestProj4", 1 ) 
                   (dbi/new-project "description 5", "MyTestProj5", 1 ) 
                   (dbi/new-project "description 6", "MyTestProj6", 1 ) 
                   (dbi/new-project "description 7", "MyTestProj7", 1 ) 
                   (dbi/new-project "description 8", "MyTestProj8", 1 ) 
                   (dbi/new-project "description 9", "MyTestProj9", 1 ) 
                   (dbi/new-project "2 plate sets with 10 96 well plates each", "Plates only, no data", 1 ) 
                   ))
;;(add-projects)

;; \copy (Select * From plate_set) To '/home/mbc/projects/lnrocks/resources/data/plate-set.csv' With CSV

  
(defn add-plate-sets []
  (do
            (dbi/new-plate-set "with AR (low values), HL", "2 96 well plates", 2,96,1,1,1,true) 
            (dbi/new-plate-set "with AR (low values), HL", "2 96 well plates", 2,96,1,1,1,true) 
            (dbi/new-plate-set "with AR (high values), HL", "2 96 well plates", 2,96,1,1,1,true) 
            (dbi/new-plate-set "with AR (low values), HL", "2 384 well plates", 2,384,1,2,13,true) 
            (dbi/new-plate-set "with AR (low values), HL", "1 1536 well plate", 1, 1536, 1, 3, 37,true) 
            (dbi/new-plate-set "using LYT-1/;96/;4in12", "Plates only", 10,96,1,10,1,true) 
             (dbi/new-plate-set "using LYT-1/;96/;4in12", "Plates only", 10,96,1,10,1,true) 
             ))
;;(add-plate-sets)

(defn load-assay-data
  "hits must be handle separately so name and description can be entered"
  [] 
  (do
    (dbi/associate-data-with-plate-set "assay_run1", "PS-1 LYT-1;96;4in12", ["PS-1"] 96, 1, 1, "resources/data/ar1raw.txt" false nil nil)
    (dbi/associate-data-with-plate-set "assay_run2", "PS-2 LYT-1;96;4in12", ["PS-2"] 96, 1, 1, "resources/data/ar2raw.txt" false nil nil)
    (dbi/associate-data-with-plate-set "assay_run3", "PS-3 LYT-1;96;4in12", ["PS-3"] 96, 5, 1, "resources/data/ar3raw.txt" false nil nil)
    (dbi/associate-data-with-plate-set "assay_run4", "PS-4 LYT-13;384;8in24", ["PS-4"] 384, 1, 13, "resources/data/ar4raw.txt" false nil nil)
    (dbi/associate-data-with-plate-set "assay_run5", "PS-5 LYT-37;1536;32in47,48", ["PS-5"] 1536, 1, 37, "resources/data/ar5raw.txt" false nil nil)))
    


(defn add-hit-lists []
                    (dbi/new-hit-list "hit list 1", "descr1", 10, 1,   [87 39 51 59 16 49 53 73 65 43]) 
                    (dbi/new-hit-list "hit list 2", "descr2", 20, 1,   [154, 182, 124, 172, 171, 164, 133, 155, 152, 160, 118, 93, 123, 142, 183, 145, 95, 120, 158, 131]) 
                    (dbi/new-hit-list "hit list 3", "descr3", 10, 2,   [216, 193, 221, 269, 244, 252, 251, 204, 217, 256]) 
                    (dbi/new-hit-list "hit list 4", "descr4", 20, 2,   [311, 277, 357, 314, 327, 303, 354, 279, 346, 318, 344, 299, 355, 300, 325, 290, 278, 326, 282, 334]) 
                    (dbi/new-hit-list "hit list 5", "descr5", 10, 3,  [410, 412, 393, 397, 442, 447, 428, 374, 411, 437]) 
                    (dbi/new-hit-list "hit list 6", "descr6", 20, 3,  [545, 514, 479, 516, 528, 544, 501, 472, 463, 494, 531, 482, 513, 468, 465, 510, 535, 478, 502, 488]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;executables used by interface
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn drop-database
;;needed for interface button
[]
  (jdbc/db-do-commands cm/conn true  "DROP DATABASE lndb"))

;;(drop-database)

(defn initialize-limsnucleus
  ;;(map #(jdbc/db-do-commands cm/conn (jdbc/drop-table-ddl % {:conditional? true } )) all-table-names)
  []
    (doall (map #(jdbc/db-do-commands cm/conn true  %) (map #(format  "DROP TABLE IF EXISTS %s CASCADE" %)  all-table-names ) ))
  ;;(jdbc/db-do-commands cm/conn-create true  "CREATE DATABASE lndb")
  (doall (map #(jdbc/db-do-commands cm/conn true %) all-tables))
  (doall  (map #(jdbc/db-do-commands cm/conn true %) all-indices))
  ;; this errors because brackets not stripped
  ;;(map #(jdbc/insert-multi! cm/conn %) required-data)
  (doall  (map #(apply jdbc/insert-multi! cm/conn % ) required-data))
  (cm/set-init false))

;;(initialize-limsnucleus)
;;(println cm/conn)

;;root@xps:/home/mbc# mysql --user=root -p2727 lndb
;;mysql>CREATE USER 'ln_admin'@'%' IDENTIFIED BY 'welcome';
;;mysql>GRANT ALL PRIVILEGES ON *.* TO 'ln_admin'@'%' WITH GRANT OPTION;


(defn add-example-data
  ;;
  []
  ;; order important!
  (do
    ;;(doall (map #(jdbc/db-do-commands cm/conn true  %) (map #(format  "TRUNCATE TABLE %s" %)  tables-to-truncate )))
;;    (doall (map #(jdbc/db-do-commands cm/conn true  %) (map #(format  "TRUNCATE %s CASCADE" %)  tables-to-truncate )))
  (jdbc/insert! cm/conn :lnsession {:lnuser_id 1})
  (cm/set-session-id 1)  
  (add-projects)
  (add-plate-sets)
  (load-assay-data)
  (add-hit-lists)))

;;(add-example-data)

(defn delete-example-data
  []
  ;;(jdbc/execute! cm/conn "TRUNCATE project, plate_set, plate, hit_sample, hit_list, assay_run, assay_result, sample, well, lnsession;"))

  (jdbc/execute! cm/conn "TRUNCATE project, plate_set, plate, hit_sample, hit_list, assay_run, assay_result, sample, well, lnsession RESTART IDENTITY CASCADE;"))

