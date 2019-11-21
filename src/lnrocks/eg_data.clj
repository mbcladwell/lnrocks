(ns lnrocks.eg-data
  (:require
   ;;[clojure.data.csv :as csv]
   [clojure.java.io :as io]
   [crux.api :as crux]
   [lnrocks.db-inserter :as dbi]
   [lnrocks.db-retriever :as dbr]
   [lnrocks.util :as util]
   [clojure.inspector :as insp])
  (:import [crux.api ICruxAPI])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;Optional example data
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  ;;k: key-to-remove
  [x coll k]
  (map #(dissoc % k) (filter #(= (:id %) x) coll ) ))


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

(defn load-hit-lists [node]
  (let   [hl hitlists]
         (loop [counter 1
                hl-single  (first (filter #(= (:id %) counter) hl))
                dummy    (crux/submit-tx node [[:crux.tx/put hl-single]] )]
           (if (> counter (+ 1 (count hl)))
             (println "Hit lists loaded!")
             (recur
              (+ counter 1)
              (first (filter #(= (:id %) counter) hl))
              (crux/submit-tx node [[:crux.tx/put hl-single]] )
              )))))




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


(defn load-assay-run-data [node]
  ;;add data to assay-run names using the key :pdata  (for processed data)
  (let   [table (util/table-to-map "resources/data/assay-run.txt")
          assay-names (into [] (map #(process-assay-run-names %) table))
          table2 (util/table-to-map "resources/data/processed_data_for_import.txt")
          assay-data (into [] (map #(process-assay-run-data %) table2))
          result (map #(assoc % :pdata (extract-data-for-id (:id %)  assay-data :id)) assay-names)]
    (loop [counter 1
           an-ar  (first (filter #(= (:id %) counter) result))
           dummy    (crux/submit-tx node [[:crux.tx/put an-ar]] )]
      (if (> counter (+ 1 (count result)))
        (println "Assay runs loaded!")
        (recur
         (+ counter 1)
         (first (filter #(= (:id %) counter) result))
         (crux/submit-tx node [[:crux.tx/put an-ar]] )
         )))))

;;(lnrocks.eg-data/load-assay-run-data node)

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


(defn load-eg-plate [node]
         (let   [table (util/table-to-map "resources/data/plates.txt")
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
           (loop [counter 1
                  a-plate  (first (filter #(= (:id %) counter) plates2))
                  dummy    (crux/submit-tx node [[:crux.tx/put a-plate]] )]
             (if (> counter (+ 1 (count plates2)))
               (println "Plates loaded!")
               (recur
                (+ counter 1)
                (first (filter #(= (:id %) counter) plates2))
                (crux/submit-tx node [[:crux.tx/put a-plate]] ))))))



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


(defn load-eg-plate-sets
"plate-sets 1 through 5 have assay-runs 1 through 5"
  [node]
         (let   [  table (util/table-to-map "resources/data/plate-set-names.txt")
                 plate-sets (into [] (map #(process-eg-plate-set-data %) table))
                 dummy  (loop [counter 1
                               a-plate-set  (first (filter #(= (:id %) counter) plate-sets))
                               dummy    (crux/submit-tx node [[:crux.tx/put a-plate-set]] )]
                          (if (> counter (+ 1 (count plate-sets)))
                            (println "Plate-Sets loaded!")
                            (recur
                             (+ counter 1)
                             (first (filter #(= (:id %) counter) plate-sets))
                             (crux/submit-tx node [[:crux.tx/put a-plate-set]] ))))

                 ps1 (crux/entity (crux/db node ) :ps1)
                 new-ps1 (update ps1 :assay-runs (comp set conj)
                                 (crux/entity (crux/db node) :ar1))
        
                 ps2 (crux/entity (crux/db node ) :ps2)
                 new-ps2 (update ps2 :assay-runs (comp set conj)
                                 (crux/entity (crux/db node) :ar2))

                 ps3 (crux/entity (crux/db node ) :ps3)
                 new-ps3 (update ps3 :assay-runs (comp set conj)
                                 (crux/entity (crux/db node) :ar3))
                 ps4 (crux/entity (crux/db node ) :ps4)
                 new-ps4 (update ps4 :assay-runs (comp set conj)
                                 (crux/entity (crux/db node) :ar4))
                 ps5 (crux/entity (crux/db node ) :ps5)
                 new-ps5 (update ps5 :assay-runs (comp set conj)
                                 (crux/entity (crux/db node) :ar5))]

           (crux/submit-tx node [[:crux.tx/cas ps1 new-ps1]])
           (crux/submit-tx node [[:crux.tx/cas ps2 new-ps2]])
           (crux/submit-tx node [[:crux.tx/cas ps3 new-ps3]])
           (crux/submit-tx node [[:crux.tx/cas ps4 new-ps4]])
           (crux/submit-tx node [[:crux.tx/cas ps5 new-ps5]])))


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


(defn load-eg-projects [node]
  (let   [table (util/table-to-map "resources/data/projects.txt")
          proj-data (into [] (map #(process-eg-prj-data %) table))
          dummy (loop [counter 1
                       a-proj  (first (filter #(= (:id %) counter) proj-data))
                       dummy    (crux/submit-tx node [[:crux.tx/put a-proj]] )]
                  (if (> counter (+ 1 (count proj-data)))
                    (println "Projects loaded!")
                    (recur
                     (+ counter 1)
                     (first (filter #(= (:id %) counter) proj-data))
                     (crux/submit-tx node [[:crux.tx/put a-proj]] ))))
          prj1 (crux/entity (crux/db node ) :prj1)
          new1-prj1 (update prj1 :hit-lists (comp set conj)
                           (crux/entity (crux/db node) :hl1)
                           (crux/entity (crux/db node) :hl2))
          new2-prj1 (update new1-prj1 :plate-sets (comp set conj)
                           (crux/entity (crux/db node) :ps1)
                           (crux/entity (crux/db node) :ps2)
                           (crux/entity (crux/db node) :ps3))
          prj2 (crux/entity (crux/db node ) :prj2)
          new1-prj2 (update prj2 :hit-lists (comp set conj)
                           (crux/entity (crux/db node) :hl3)
                           (crux/entity (crux/db node) :hl4))

          new2-prj2 (update new1-prj2 :plate-sets (comp set conj)
                           (crux/entity (crux/db node) :ps4))
          
          prj3 (crux/entity (crux/db node ) :prj3)
          new1-prj3 (update prj3 :hit-lists (comp set conj)
                           (crux/entity (crux/db node) :hl5)
                           (crux/entity (crux/db node) :hl6))
          new2-prj3 (update new1-prj3 :plate-sets (comp set conj)
                            (crux/entity (crux/db node) :ps5))
      
          prj10 (crux/entity (crux/db node ) :prj10)
          new-prj10 (update prj10 :plate-sets (comp set conj)
                           (crux/entity (crux/db node) :ps6)
                           (crux/entity (crux/db node) :ps7)
                           (crux/entity (crux/db node) :ps8))]

    (crux/submit-tx node [[:crux.tx/cas prj1 new2-prj1]])
    (crux/submit-tx node [[:crux.tx/cas prj2 new2-prj2]])
    (crux/submit-tx node [[:crux.tx/cas prj3 new2-prj3]])
    (crux/submit-tx node [[:crux.tx/cas prj10 new-prj10]])))




(require '[clojure.inspector :as insp])
;;(insp/inspect-tree projects)
;;(insp/inspect-tree (load-eg-plate-sets))

;;(insp/inspect-tree (load-eg-projects))


;;Number of IDs needed for example data set
;; project 10
;; plate-set 8
;; plate 29
;; sample 4648
;; assay-run 5

(defn load-eg-data [node]
  (do
    (load-hit-lists node)
    (load-assay-run-data node)
    (load-eg-plate node)
    (load-eg-plate-sets node)
    (load-eg-projects node)

    
))