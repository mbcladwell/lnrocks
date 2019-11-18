(ns lnrocks.db-init
  (:require
   ;;[clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [crux.api :as crux]
   [lnrocks.core :as core]
   [lnrocks.db-inserter :as dbi]
   [lnrocks.db-retriever :as dbr]
   [lnrocks.util :as util]
   [clojure.inspector :as insp])
  (:import [crux.api ICruxAPI])
  (:gen-class))

    
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Database setup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




(def counters
 {:crux.db/id :counters
  :project 0
  :plate-set 0
  :plate 0
  :sample 0
  :hit-list 0
  :work-list 0}
  )

;;(crux/submit-tx core/node [[:crux.tx/put counters]] )




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
  
;;(crux/submit-tx node [[:crux.tx/put props]] )

(def helpers
  [{:crux.db/id :plate-formats :96 96 :384 384 :1536 1536}
   {:crux.db/id :plate-type 1 "assay" 2 "rearray" 3 "master" 4 "daughter" 5 "archive" 6 "replicate"}
   {:crux.db/id :plate-layout :plate-layout (dbi/load-plate-layouts)}
   {:crux.db/id :assay-type  1 "ELISA" 2 "Octet" 3 "SNP" 4 "HCS" 5 "HTRF" 6 "FACS"}
   {:crux.db/id :well-type  1 "unknown" 2 "positive" 3 "negative" 4 "blank" 5 "edge"}
   {:crux.db/id :well-numbers :well-numbers (dbi/load-well-numbers) }
   {:crux.db/id :layout-src-dest :layout-src-dest   [{:source 1 :dest  2}{:source 1 :dest 3}{:source 1 :dest 4}{:source 1 :dest 5}{:source 1 :dest 6}{:source 7 :dest 8}{:source 7 :dest 9}{:source 7 :dest 10}{:source 7 :dest 11}{:source 7 :dest 12}{:source 13 :dest 14}{:source 13 :dest 15}{:source 13 :dest 16}{:source 13 :dest 17}{:source 13 :dest 18}{:source 19 :dest 20}{:source 19 :dest 21}{:source 19 :dest 22}{:source 19 :dest 23}{:source 19 :dest 24}{:source 25 :dest 26}{:source 25 :dest 27}{:source 25 :dest 28}{:source 25 :dest 29}{:source 25 :dest 30}{:source 31 :dest 32}{:source 31 :dest 33}{:source 31 :dest 34}{:source 31 :dest 35}{:source 31 :dest 36}{:source 37 :dest 41}{:source 38 :dest 41}{:source 39 :dest 41}{:source 40 :dest 41}]}
   ])



;; (easy-ingest core/node helpers)


     
     
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


(def hitlists [
               ;;for project1
               {:name "hit list 1"
                :crux.db/id (keyword (str "hl1"))
           
                :description "descr1"
                :hits [87 39 51 59 16 49 53 73 65 43]
                :prj-id 1
                :id 1}
               {:name "hit list 2"
                :crux.db/id (keyword (str "hl2"))
                :description "descr2"
                :hits  [154, 182, 124, 172, 171, 164, 133, 155, 152, 160, 118, 93, 123, 142, 183, 145, 95, 120, 158, 131]
                :prj-id 1
                :id 2}
               ;;for project2
               {:name "hit list 3"
                :crux.db/id (keyword (str "hl3"))
                
                :description "descr3"
                :hits [216, 193, 221, 269, 244, 252, 251, 204, 217, 256]
                :prj-id 2
                :id 3}
               {:name "hit list 4"
                :crux.db/id (keyword (str "hl4"))
                :description "descr4"
                :hits [311, 277, 357, 314, 327, 303, 354, 279, 346, 318, 344, 299, 355, 300, 325, 290, 278, 326, 282, 334]
                :prj-id 2
                :id 4}
               ;;for project3
               {:name "hit list 5"
                :crux.db/id (keyword (str "hl5"))
                
                :description "descr5"
                :hits [410, 412, 393, 397, 442, 447, 428, 374, 411, 437]
                :prj-id 3
                :id 5}
               {:name "hit list 6"
                :crux.db/id (keyword (str "hl6"))
                :description "descr6"
                :hits [545, 514, 479, 516, 528, 544, 501, 472, 463, 494, 531, 482, 513, 468, 465, 510, 535, 478, 502, 488]
                :prj-id 3
                :id 6}])

(defn process-assay-run-names
  "id	assay-run-sys-name	assay-run-name	description	assay-type-id	plate-set-id	plate-layout-name-id	lnsession-id"
[x]
 (into {} {:id (Integer/parseInt (String. (:id x)))
            :assay-run-sys-name (:assay-run-sys-name x )
            :assay-run-name (:assay-run-name x )
           :crux.db/id (keyword (str "ar"(:id x )))
           :description (:description x )
           :assay-type-id (Integer/parseInt (String. (:assay-type-id x)))
           :plate-set-id (Integer/parseInt (String. (:plate-set-id x)))
           :plate-layout-name-id (Integer/parseInt (String. (:plate-layout-name-id x)))
           :lnsession-id (Integer/parseInt (String. (:lnsession-id x)))
           }))


(defn process-assay-run-data
  "assay.id	PlateOrder	WellNum	Response	Bk_Sub	Norm	NormPos	pEnhanced"
[x]
 (into {} {:id (Integer/parseInt (String. (:assay.id x)))
           :plate-order (Integer/parseInt (String. (:PlateOrder x)))
           :well-num (Integer/parseInt (String. (:WellNum x)))
           :response (Double/parseDouble (String. (:Response x)))
           :bk-sub (Double/parseDouble (String. (:Bk_Sub x)))
           :norm (Double/parseDouble (String. (:Norm x)))
           :normpos (Double/parseDouble (String. (:NormPos x)))
           :penhanced (Double/parseDouble (String. (:pEnhanced x)))
           }))


(defn load-assay-run-data []
  ;;add data to assay-run names using the key :pdata  (for processed data)
  (let   [table (util/table-to-map "resources/data/assay-run.txt")
          assay-names (into [] (map #(process-assay-run-names %) table))
          table2 (util/table-to-map "resources/data/processed_data_for_import.txt")
          assay-data (into [] (map #(process-assay-run-data %) table2))
          result (map #(assoc % :pdata (dbi/extract-data-for-id (:id %)  assay-data)) assay-names)]
         result))

;;(load-assay-run-data)

;;plate_sys_name	plate_type_id	plate_layout_name_id	plate_set_name	descr	num_plates	plate_format_id	project_id	lnsession_id	plate_set_id	plate_id	plate_order
(defn process-sample-file
 [x]
  (into {} {
            :id (Integer/parseInt (String. (:id x)))
            :format (Integer/parseInt (String. (:format x)))
            :layout (Integer/parseInt (String. (:layout x)))
            :samples (Integer/parseInt (String. (:samples x)))
            :id-start (Integer/parseInt (String. (:id-start x)))
            }))


(defn load-well-vector []
  (let   [table (util/table-to-map "resources/data/samples.txt")
          samples (into [] (map #(process-sample-file %) table))
          well-vector  (loop [counter 1
                              w-vec-pre []
                              ]
                          (if (> counter (count samples))
                             w-vec-pre
                         (recur
                          (+ counter 1)
                          (conj w-vec-pre (assoc (first (filter #(= (:id %) counter)  samples)) :wells
                                                 (case (:format (first (filter #(= (:id %) counter)  samples)))
                                                                96 (util/fill-wells util/map96wells (:id-start (first (filter #(= (:id %) counter)  samples))) (:samples (first (filter #(= (:id %) counter)  samples))))
                                                                384 (util/fill-wells util/map384wells (:id-start (first (filter #(= (:id %) counter)  samples))) (:samples (first (filter #(= (:id %) counter)  samples))))
                                                                1536 (util/fill-wells util/map1536wells (:id-start (first (filter #(= (:id %) counter)  samples))) (:samples (first (filter #(= (:id %) counter)  samples))))
                                                                ))))   ))]                   
             well-vector))


;;(insp/inspect-tree (load-well-vector))


(defn process-eg-plate-data
"plate_sys_name	plate_type_id	plate_layout_name_id	plate_set_name	descr	num_plates	plate_format_id	project_id	lnsession_id	plate_set_id	id (this is the plate_id; must be :id)	plate_order"
 [x]
  (into {} {
            :plate-set-id (Integer/parseInt (String. (:plate_set_id x)))
            :id (Integer/parseInt (String. (:id x)))
            :crux.db/id (keyword (str "plt"(:id x )))
            :plate-order (Integer/parseInt (String. (:plate_order x)))
            }))


(defn load-eg-plate []
         (let   [  table (util/table-to-map "resources/data/plates.txt")
                 plates (into [] (map #(process-eg-plate-data %) table))
                 wells-vec (load-well-vector)
                 plates2 (loop [counter 1
                                new-plates []]
                           (if (> counter (count plates))
                             new-plates
                             (recur
                              (+ counter 1)
                              (conj new-plates (assoc (first (filter #(= (:id %) counter)  plates))
                                                      :wells  (:wells (first (filter #(= (:id %) counter)  wells-vec)))))
                              )))]
             plates2))


;;(insp/inspect-tree (load-eg-plate))


(defn process-eg-plate-set-data
"id	plate-set-name	descr	plate-set-sys-name	num-plates	plate-format-id	plate-type-id	project-id	plate-layout-name-id	lnsession-id"
 [x]
  (into {} {:id (Integer/parseInt (String. (:id x)))
            :crux.db/id (keyword (str "ps"(:id x )))
            :plate-set-name (:plate-set-name x )
            :descr (String.(:descr x) )
            :plate-set-sys-name (:plate-set-sys-name x )
            :num-plates (Integer/parseInt (String. (:num-plates x)))
            :plate-format-id (Integer/parseInt (String. (:plate-format-id x)))
            :plate-type-id (Integer/parseInt (String. (:plate-type-id x)))
            :project-id (Integer/parseInt (String. (:project-id x)))
            :plate-layout-name-id (Integer/parseInt (String. (:plate-layout-name-id x)))
            :lnsession-id (Integer/parseInt (String. (:lnsession-id x))) }))


(defn load-eg-plate-sets []
         (let   [  table (util/table-to-map "resources/data/plate-set-names.txt")
                 plate-sets (into [] (map #(process-eg-plate-set-data %) table))
                 assay-data (load-assay-run-data)
                 result1 (loop [counter 1
                                new-plate-set []]
                           (if (> counter (count plate-sets))
                             new-plate-set
                             (recur
                              (+ counter 1)
                              (conj new-plate-set (assoc (first (filter #(= (:id %) counter)  plate-sets)) :assay-runs (map #(dissoc % :plate-set-id) (filter #(= (:plate-set-id %) counter) assay-data)))))
                             ))
                 plates (load-eg-plate)
                 result2 (loop [counter 1
                                new-plate-set []]
                           (if (> counter (count plate-sets))
                             new-plate-set
                             (recur
                              (+ counter 1)
                              (conj new-plate-set (assoc (first (filter #(= (:id %) counter)  result1)) :plates (map #(dissoc % :plate-set-id) (filter #(= (:plate-set-id %) counter) plates)))))
                             ))
                 
                 ]
             result2))
                     

(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  ;;k: key-to-remove
  [x coll k]
  (map #(dissoc % k) (filter #(= (:id %) x) coll ) ))

;;((dbi/extract-data-for-id (:id (first plate-sets))

(defn process-eg-prj-data
  "id 	project-sys-name	description	name	lnsession-id"
  [x]
  (into {} {;; :id (Integer/parseInt (String. (:id x)))
            :crux.db/id (keyword (str "prj"(:id x )))
            :project-sys-name (:project-sys-name x )
            :description (:description x )
            :name (:name x )
            :lnsession-id (Integer/parseInt (String. (:lnsession-id x)))
            :id (Integer/parseInt (String. (:id x)))
            }))


(defn load-eg-projects []
         (let   [  table (util/table-to-map "resources/data/projects.txt")
                 proj-data (into [] (map #(process-eg-prj-data %) table))
                 ps (load-eg-plate-sets)
                 result2  (loop [counter 1
                                new-ps []]
                           (if (> counter 10)
                             new-ps
                             (recur
                              (+ counter 1)
                              (conj new-ps (assoc (first (filter #(= (:id %) counter)  proj-data)) :plate-sets (map #(dissoc % :project-id) (filter #(= (:project-id %) counter) ps)))))
                             ))             
                 hl hitlists
                 result3 (loop [counter 1
                                new-hit-lists []]
                           (if (> counter 10)
                             new-hit-lists
                             (recur
                              (+ counter 1)
                              (conj new-hit-lists (assoc (first (filter #(= (:id %) counter)  result2)) :hit-lists (map #(dissoc % :prj-id) (filter #(= (:prj-id %) counter) hitlists)))))
                             ))

                 ]
             result3))



(require '[clojure.inspector :as insp])
;;(insp/inspect-tree projects)
;;(insp/inspect-tree (load-eg-plate-sets))

;;(insp/inspect-tree (load-eg-projects))


 (def table (util/table-to-map "resources/data/projects.txt"))
 (def proj-data (into [] (map #(process-eg-prj-data %) table)))
(def ps (load-eg-plate-sets))
;;(insp/inspect-tree proj-data)
(def result2 (map #(assoc % :plate-sets (dbi/extract-data-for-id (:project-id %)  ps)) proj-data))




;; select * from plate, plate_set, plate_plate_set where plate_plate_set.plate_set_id = plate_set.id and plate_plate_set.plate_id = plate.id;

;; \copy (select * from plate, plate_set, plate_plate_set where plate_plate_set.plate_set_id = plate_set.id and plate_plate_set.plate_id = plate.id) To '/home/mbc/projects/lnrocks/resources/data/plates.csv' With CSV



;;Number of IDs needed for example data set
;; project 10
;; plate-set 8
;; plate 29
;; sample 4648
