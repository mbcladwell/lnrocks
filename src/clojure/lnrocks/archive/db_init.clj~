(ns ln.db-init
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as hsql]
            [honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [ln.db-inserter :as dbi]
            [ln.codax-manager :as cm])
           
  (:import java.sql.DriverManager)
  (:gen-class))


;; (def pg-db-init  {:dbtype "postgresql"
;;                   :dbname "lndb"
;;                   :host (cm/get-host)
;;                   :user (cm/get-user)
;;                   :password (cm/get-password)
;;                   :port (cm/get-port)
;;                   :ssl false
;;                   :sslfactory "org.postgresql.ssl.NonValidatingFactory"})

               
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Database setup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(def all-table-names
  ;;for use in a map function that will delete all tables
  ;;single command looks like:  (jdbc/drop-table-ddl :lnuser {:conditional? true } )
  ["well_numbers" "worklists" "rearray_pairs"  "import_plate_layout" "plate_layout" "well_type" "hit_sample" "hit_list" "assay_result" "assay_run" "assay_type" "well_sample" "sample" "well" "plate" "plate_plate_set" "plate_set" "layout_source_dest" "plate_layout_name" "plate_format" "plate_type" "project" "lnsession" "lnuser" "lnuser_groups"] )


(def tables-to-truncate
  "abridged set of tables to be truncated when deleting example data"
  ["project" "plate_set" "plate" "hit_sample" "hit_list" "assay_run" "assay_result" "sample" "well" "lnsession" ])


(def all-tables
  ;;for use in a map function that will create all tables
  ;; example single table:
  ;;;     [(jdbc/create-table-ddl :lnsession
  ;;                 [[:id "SERIAL PRIMARY KEY"]
  ;;                  [:lnuser_id :int]
  ;;                  [:updated  :timestamp "with time zone not null DEFAULT current_timestamp"]
 ;;                   ["FOREIGN KEY (lnuser_id) REFERENCES lnuser(id)"]]) ]

  
  [ 
   [(jdbc/create-table-ddl :lnuser_groups
                         [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                          [:usergroup "varchar(250)"]
                          [:updated  :timestamp ]])]
   
   [(jdbc/create-table-ddl :lnuser
                          [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                           [:usergroup_id "INT(20)"]
                           [:lnuser_name "VARCHAR(250) not null unique"]
                           [:tags "varchar(250)"]
                           [:password "varchar(64) not null"]
                           [:updated  :timestamp ]
                           ["FOREIGN KEY (usergroup_id) REFERENCES lnuser_groups(id)"]])]

 
   [(jdbc/create-table-ddl :lnsession
                         [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                          [:lnuser_id :int]
                            [:updated  :timestamp ]
                          ["FOREIGN KEY (lnuser_id) REFERENCES lnuser(id)"]]) ]
   
   [(jdbc/create-table-ddl :project
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:project_sys_name "varchar(30)"]
                            [:descr "varchar(250)"]
                            [:project_name "varchar(250)"]
                           [:lnsession_id :int]
                            [:updated  :timestamp ]
                            ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id)"]]
                           )]
   ;;CREATE INDEX ON project(lnsession_id);
   [(jdbc/create-table-ddl :plate_type
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:plate_type_name "varchar(30)"] ])]
   
   [(jdbc/create-table-ddl :plate_format
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:format "varchar(6)"]
                            [:rownum :int]
                            [:colnum :int]])]
   [(jdbc/create-table-ddl :plate_layout_name
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:sys_name "varchar(30)"]
                            [:name "varchar(250)"]
                            [:descr "varchar(250)"]
                            [:plate_format_id :int]
                            [:replicates :int]
                            [:targets :int]
                            [:use_edge :int]
                            [:num_controls :int]
                            [:unknown_n :int]
                            [:control_loc "varchar(30)"]
                            [:source_dest "varchar(30)"]
                            ["FOREIGN KEY (plate_format_id) REFERENCES plate_format(id)"]])]
   [(jdbc/create-table-ddl :layout_source_dest
                           [[:src :int "NOT NULL"]
                            [:dest :int "NOT NULL"]
                            ])]
   [(jdbc/create-table-ddl :plate_set
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                             [:plate_set_name "varchar(250)"]
                            [:descr "varchar(250)"]
                            [:plate_set_sys_name "varchar(30)"]
                            [:num_plates :int ]
                            [:plate_format_id :int ]
                            [:plate_type_id :int ]
                            [:project_id :int ]
                            [:plate_layout_name_id :int ]
                            [:lnsession_id :int ]
                            [:updated  :timestamp ]
                            ["FOREIGN KEY (plate_type_id) REFERENCES plate_type(id)"]
                            ["FOREIGN KEY (plate_format_id) REFERENCES plate_format(id)"]
                            ["FOREIGN KEY (project_id) REFERENCES project(id) on delete cascade"]
                            ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id) on delete cascade"]
                            ["FOREIGN KEY (plate_layout_name_id) REFERENCES plate_layout_name(id)"]
                            ])]

    [(jdbc/create-table-ddl :plate
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:barcode "varchar(250)"]
                            [:plate_sys_name "varchar(30)"]                        
                            [:plate_type_id :int]
                            [:plate_format_id :int]
                            [:plate_layout_name_id :int]
                            [:updated  :timestamp ]
                            ["FOREIGN KEY (plate_type_id) REFERENCES plate_type(id)"]
		            ["FOREIGN KEY (plate_format_id) REFERENCES plate_format(id)"]
		            ["FOREIGN KEY (plate_layout_name_id) REFERENCES plate_layout_name(id)"]   
                            ])]
     [(jdbc/create-table-ddl :plate_plate_set
                           [[:plate_set_id :int]
                            [:plate_id :int]
                            [:plate_order :int]
                            ["FOREIGN KEY (plate_set_id) REFERENCES plate_set(id)"]
		            ["FOREIGN KEY (plate_id) REFERENCES plate(id)"]
                           ])]
   [(jdbc/create-table-ddl :sample
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:sample_sys_name "varchar(30)"]
                            [:project_id :int]
                            [:accs_id "varchar(30)"]
                            [:plate_id :int]                         
                            ["FOREIGN KEY (project_id) REFERENCES project(id)"]
		           ])]
   [(jdbc/create-table-ddl :well
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:by_col :int]
                            [:plate_id :int]
                            ["FOREIGN KEY (plate_id) REFERENCES plate(id)"]
		           ])]
   [(jdbc/create-table-ddl :well_sample
                           [[:well_id :int]
                            [:sample_id :int]
                            ["FOREIGN KEY (well_id) REFERENCES well(id)"]
		            ["FOREIGN KEY (sample_id) REFERENCES sample(id)"]
		           ])]

    
  [(jdbc/create-table-ddl :assay_type
                           [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                            [:assay_type_name "varchar(250)"]
                            
                           ])]
  [(jdbc/create-table-ddl :assay_run
                          [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                           [:assay_run_sys_name "varchar(30)"]
                           [:assay_run_name "varchar(250)"]
                           [:descr "varchar(250)"]
                            [:assay_type_id :int]
                            [:plate_set_id :int]
                            [:plate_layout_name_id :int]
                            [:lnsession_id :int]
                            [:updated  :timestamp ]                          
                           ["FOREIGN KEY (plate_set_id) REFERENCES plate_set(id)"]
                           ["FOREIGN KEY (plate_layout_name_id) REFERENCES plate_layout_name(id)"]
		           ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id)"]
		           ["FOREIGN KEY (assay_type_id) REFERENCES assay_type(id)"]
		           ])]

    [(jdbc/create-table-ddl :assay_result
                          [   [:assay_run_id :int]
                            [:plate_order :int]
                            [:well :int]
                           [:response :real]
                           [:bkgrnd_sub :real]
                           [:norm :real]
                           [:norm_pos :real]
                           [:p_enhance :real]
                           
                            [:updated  :timestamp ]                          
                           ["FOREIGN KEY (assay_run_id) REFERENCES assay_run(id)"]
                           ])]

    [(jdbc/create-table-ddl :hit_list
                          [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                           [:hitlist_sys_name "varchar(30)"]
                           [:hitlist_name "varchar(250)"]
                           [:descr "varchar(250)"]
                            [:n :int]
                           [:lnsession_id :int]
                           [:assay_run_id :int]
                            [:updated  :timestamp ]               
                           ["FOREIGN KEY (lnsession_id) REFERENCES lnsession(id)"]
                           ["FOREIGN KEY (assay_run_id) REFERENCES assay_run(id)"]
		           ])]
  [(jdbc/create-table-ddl :hit_sample
                           [[:hitlist_id :int "NOT NULL"]
                            [:sample_id :int "NOT NULL"]
                            ["FOREIGN KEY (hitlist_id) REFERENCES hit_list(id)  ON DELETE cascade"]
                            ["FOREIGN KEY (sample_id) REFERENCES sample(id)  ON DELETE cascade"]
                            ])]

       [(jdbc/create-table-ddl :well_type
                               [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                             [:name "varchar(30)"]
                             ])]

     [(jdbc/create-table-ddl :plate_layout
                          [   [:plate_layout_name_id :int]
                            [:well_by_col :int]
                            [:well_type_id :int]
                           [:replicates :int]
                           [:target :int]                        
                           ["FOREIGN KEY (plate_layout_name_id) REFERENCES plate_layout_name(id)"]
                           ["FOREIGN KEY (well_type_id) REFERENCES well_type(id)"]
                           ])]
     
     
        [(jdbc/create-table-ddl :rearray_pairs
                                [[:id "INT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT"]
                                 [:src :int]
                                 [:dest :int]
                                 ])]
 
       [(jdbc/create-table-ddl :worklists
                          [   [:rearray_pairs_id :int]
                            [:sample_id :int]
                           [:source_plate "varchar(10)"]
                           [:source_well :int]
                           [:dest_plate "varchar(10)"]
                           [:dest_well :int]
                           ["FOREIGN KEY (rearray_pairs_id) REFERENCES rearray_pairs(id)  ON DELETE cascade"]
                           ["FOREIGN KEY (sample_id) REFERENCES sample(id)"]
                           ])]

       [(jdbc/create-table-ddl :well_numbers
                          [   [:plate_format :int]
                           [:well_name "varchar(5)"]
                            [:row_name "varchar(5)"]
                           [:row_num :int]
                           [:col "varchar(5)"]
                           [:total_col_count :int]
                           [:by_row :int]
                           [:by_col :int]
                           [:quad :int]
                           [:parent_well :int]
                           ])]
   ])


(def all-indices
  [["CREATE INDEX pln_pfid ON plate_layout_name(plate_format_id);"]
   ["CREATE INDEX bc ON plate(barcode);"]
   ["CREATE INDEX ps_pfid ON plate_set(plate_format_id);"]
   ["CREATE INDEX ps_ptid ON plate_set(plate_type_id);"]
   ["CREATE INDEX ps_pid ON plate_set(project_id);"]
   ["CREATE INDEX ps_sid ON plate_set(lnsession_id);"]
   ["CREATE INDEX p_ptid ON plate(plate_type_id);"]
   ["CREATE INDEX p_pfid ON plate(plate_format_id);"]
   ["CREATE INDEX pps_psid ON plate_plate_set(plate_set_id);"]
   ["CREATE INDEX pps_pid ON plate_plate_set(plate_id);"]
   ["CREATE INDEX p_sid ON project(lnsession_id);"]
   ["CREATE INDEX s_pid ON sample(project_id);"]
   ["CREATE INDEX w_pid ON well(plate_id);"]
   ["CREATE INDEX ws_wid ON well_sample(well_id);"]
   ["CREATE INDEX ws_sid ON well_sample(sample_id);"]
   ["CREATE INDEX ar_atid ON assay_run(assay_type_id);"]
   ["CREATE INDEX ar_pdif ON assay_run(plate_set_id);"]
   ["CREATE INDEX ar_plnid ON assay_run(plate_layout_name_id);"]
   ["CREATE INDEX ar_sid ON assay_run(lnsession_id);"]
   ["CREATE INDEX ar_arid ON assay_result(assay_run_id);"]
   ["CREATE INDEX ar_po ON assay_result(plate_order);"]
   ["CREATE INDEX ar_w ON assay_result(well);"]
   ["CREATE INDEX hl_arid ON hit_list(assay_run_id);"]
   ["CREATE INDEX hl_sid ON hit_list(lnsession_id);"]
   ["CREATE INDEX hs_hlid ON hit_sample(hitlist_id);"]
   ["CREATE INDEX hs_sid ON hit_sample(sample_id);"]
   ["CREATE INDEX pl_plnid ON plate_layout(plate_layout_name_id);"]
   ["CREATE INDEX pl_wtid ON plate_layout(well_type_id);"]
   ["CREATE INDEX pl_wbc ON plate_layout(well_by_col);"]
   ["CREATE INDEX rp_s ON rearray_pairs(src);"]
   ["CREATE INDEX rp_d ON rearray_pairs(dest);"]
   ["CREATE INDEX wn_bc ON well_numbers(by_col);"]
   ])

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


(def required-data
  ;;inserts required data into table using jdbc/insert-multi!
  ;;this is data that should not be deleted when repopulating with example data
  ;;this is data that is needed for basic functionality
  [[ :lnuser_groups 
    [ :usergroup ]
    [["administrator"]
     ["user" ]]]
                     
   [ :lnuser
    [ :lnuser_name :tags :usergroup_id :password ]
    [["ln_admin" "ln_admin@labsolns.com" 1  "welcome"]
     ["ln_user" "ln_user@labsolns.com" 1 "welcome"]
     ["klohymim" "NA" 1 "hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_"]
     ["plapan_ln_admin" "NA" 1 "welcome"]]]
   
   [ :plate_type [:plate_type_name]
    [["assay"]["rearray"]["master"]["daughter"]["archive"]["replicate"]]]
   
   [ :plate_format [:id :format :rownum :colnum]
    [[ 96, "96", 8, 12]
     [384, "384",16, 24]
     [1536, "1536", 32, 48]]]

   [ :plate_layout_name [:sys_name :name :descr :plate_format_id :replicates :targets :use_edge :num_controls :unknown_n :control_loc :source_dest]
    [["LYT-1" "4 controls col 12" "1S1T" 96 1 1 1 4 92 "E12-H12" "source"]
     ["LYT-2" "4 controls cols 23 24" "1S4T" 384  1 4 1 4 368 "I23-P24" "dest"]
     ["LYT-3" "4 controls cols 23 24" "2S2T" 384  2 2 1 4 368 "I23-P24" "dest"]
     ["LYT-4" "4 controls cols 23 24" "2S4T" 384  2 4 1 4 368 "I23-P24" "dest"]
     ["LYT-5" "4 controls cols 23 24" "4S1T" 384  4 1 1 4 368 "I23-P24" "dest"]
     ["LYT-6" "4 controls cols 23 24" "4S2T" 384  4 2 1 4 368 "I23-P24" "dest"]
     ["LYT-7" "8 controls col 12" "1S1T" 96  1 1 1 8 88 "A12-H12" "source"]
     ["LYT-8" "8 controls cols 23 24" "1S4T" 384  1 4 1 8 352 "A23-P24" "dest"]
     ["LYT-9" "8 controls cols 23 24" "2S2T" 384  2 2 1 8 352 "A23-P24" "dest"]
     ["LYT-10" "8 controls cols 23 24" "2S4T" 384  2 4 1 8 352 "A23-P24" "dest"]
 ["LYT-11" "8 controls cols 23 24" "4S1T" 384  4 1 1 8 352 "A23-P24" "dest"]
 ["LYT-12" "8 controls cols 23 24" "4S2T" 384  4 2 1 8 352 "A23-P24" "dest"]
 ["LYT-13" "8 controls col 24" "1S1T" 384  1 1 1 8 376 "I24-P24" "source"]
 ["LYT-14" "8 controls cols 47 48" "1S4T" 1536  1 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-15" "8 controls cols 47 48" "2S2T" 1536  2 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-16" "8 controls cols 47 48" "2S4T" 1536  2 4 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-17" "8 controls cols 47 48" "4S1T" 1536  4 1 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-18" "8 controls cols 47 48" "4S2T" 1536  4 2 1 8 1504 "Q47-AF48" "dest"]
 ["LYT-19" "16 controls col 24" "1S1T" 384  1 1 1 16 368 "A24-P24" "source"]
 ["LYT-20" "16 controls cols 47 48" "1S4T" 1536  1 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-21" "16 controls cols 47 48" "2S2T" 1536  2 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-22" "16 controls cols 47 48" "2S4T" 1536  2 4 1 16 1472 "A47-AF48" "dest"]
 ["LYT-23" "16 controls cols 47 48" "4S1T" 1536  4 1 1 16 1472 "A47-AF48" "dest"]
 ["LYT-24" "16 controls cols 47 48" "4S2T" 1536  4 2 1 16 1472 "A47-AF48" "dest"]
 ["LYT-25" "7 controls col 23" "1S1T" 384  1 1 0 7 301 "I23-O23" "source"]
 ["LYT-26" "7 controls cols 46 47" "1S4T" 1536  1 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-27" "7 controls cols 46 47" "2S2T" 1536  2 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-28" "7 controls cols 46 47" "2S4T" 1536  2 4 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-29" "7 controls cols 46 47" "4S1T" 1536  4 1 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-30" "7 controls cols 46 47" "4S2T" 1536  4 2 0 7 1204 "Q46-AE47" "dest"]
 ["LYT-31" "14 controls col 23" "1S1T" 384  1 1 0 14 294 "B23-O23" "source"]
 ["LYT-32" "14 controls cols 46 47" "1S4T" 1536  1 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-33" "14 controls cols 46 47" "2S2T" 1536  2 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-34" "14 controls cols 46 47" "2S4T" 1536  2 4 0 14 1176 "B46-AE47" "dest"]
 ["LYT-35" "14 controls cols 46 47" "4S1T" 1536  4 1 0 14 1176 "B46-AE47" "dest"]
 ["LYT-36" "14 controls cols 46 47" "4S2T" 1536  4 2 0 14 1176 "B46-AE47" "dest"]
 ["LYT-37" "8 controls cols 47 48" "1S1T" 1536  1 1 1 8 1504 "Q47-AF48" "source"]
 ["LYT-38" "16 controls cols 47 48" "1S1T" 1536  1 1 1 16 1472 "A47-AF48" "source"]
 ["LYT-39" "7 controls cols 46 47" "1S1T" 1536  1 1 0 7 1204 "Q46-AE47" "source"]
     ["LYT-40" "14 controls cols 46 47" "1S1T" 1536  1 1 0 14 1176 "B46-AE47" "source"]
     ["LYT-41" "all blanks" "not reformattable" 1536  1 1 0 0 0 "none" "dest"]]]

  [ :layout_source_dest [:src :dest ]
   [[1 2][1 3][1 4][1 5][1 6][7 8][7 9][7 10][7 11][7 12][13 14][13 15][13 16][13 17][13 18][19 20][19 21][19 22][19 23][19 24][25 26][25 27][25 28][25 29][25 30][31 32][31 33][31 34][31 35][31 36][37 41][38 41][39 41][40 41]]]

  [ :assay_type [:assay_type_name ]
   [["ELISA"]["Octet"]["SNP"]["HCS"]["HTRF"]["FACS"]]]

    [ :well_type [:name ]
     [["unknown"]["positive"]["negative"]["blank"]["edge"]]]
   
   [ :well_numbers [:plate_format :well_name :row_name :row_num :col :total_col_count :by_row :by_col :quad :parent_well ]
   ;;ln.data-sets/well-numbers
       (let   [  table (dbi/table-to-map "resources/data/well_numbers_for_import.txt")
               content (into [] (map #(process-well-numbers-data %) table))]
         content)]
    

      [ :plate_layout [ :plate_layout_name_id :well_by_col :well_type_id :replicates :target]
       ;; ln.plate-layout-data/plate-layout-data
       (let   [  table (dbi/table-to-map "resources/data/plate_layouts_for_import.txt")
               content (into [] (map #(process-layout-data %) table))]
         content)]
   ])


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

