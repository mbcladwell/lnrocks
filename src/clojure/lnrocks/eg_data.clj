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
                :crux.db/id :hl1
                :description "descr1"
                :hits #{:spl87 :spl39 :spl51 :spl59 :spl16 :spl49 :spl53 :spl73 :spl65 :spl43}
                :prj-id 1
                :id 1}
               {:name "hit list 2"
                :crux.db/id :hl2
                :description "descr2"
                :hits  #{:spl154 :spl182 :spl124 :spl172 :spl171 :spl164 :spl133 :spl155 :spl152 :spl160 :spl118 :spl93 :spl123 :spl142 :spl183 :spl145 :spl95 :spl120 :spl158 :spl131}
                :prj-id 1
                :id 2}
               ;;for project2
               {:name "hit list 3"
                :crux.db/id :hl3
                
                :description "descr3"
                :hits #{:spl216 :spl193 :spl221 :spl269 :spl244 :spl252 :spl251 :spl204 :spl217 :spl256}
                :prj-id 2
                :id 3}
               {:name "hit list 4"
                :crux.db/id :hl4
                :description "descr4"
                :hits #{:spl311 :spl277 :spl357 :spl314 :spl327 :spl303 :spl354 :spl279 :spl346 :spl318 :spl344 :spl299 :spl355 :spl300 :spl325 :spl290 :spl278 :spl326 :spl282 :spl334}
                :prj-id 2
                :id 4}
               ;;for project3
               {:name "hit list 5"
                :crux.db/id :hl5
                
                :description "descr5"
                :hits #{:spl410 :spl412 :spl393 :spl397 :spl442 :spl447 :spl428 :spl374 :spl411 :spl437}
                :prj-id 3
                :id 5}
               {:name "hit list 6"
                :crux.db/id :hl6
                :description "descr6"
                :hits #{:spl545 :spl514 :spl479 :spl516 :spl528 :spl544 :spl501 :spl472 :spl463 :spl494 :spl531 :spl482 :spl513 :spl468 :spl465 :spl510 :spl535 :spl478 :spl502 :spl488}
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
           :assay-type  (case (Integer/parseInt (String.(:assay-type-id x))) 1  "ELISA" 2 "Octet" 3 "SNP" 4 "HCS" 5 "HTRF" 6 "FACS")
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




;;((dbi/extract-data-for-id (:id (first plate-sets))

(defn assoc-ar-with-ps
  "plate-sets 1 through 5 have assay-runs 1 through 5"
  [node]
  (let  [ps1 (crux/entity (crux/db node ) :ps1)
        new-ps1 (update ps1 :assay-runs (comp set conj) (crux/entity (crux/db node) :ar1))
              
        ps2 (crux/entity (crux/db node ) :ps2)
        new-ps2 (update ps2 :assay-runs (comp set conj) (crux/entity (crux/db node) :ar2))
        
        ps3 (crux/entity (crux/db node ) :ps3)
        new-ps3 (update ps3 :assay-runs (comp set conj) (crux/entity (crux/db node) :ar3))
        ps4 (crux/entity (crux/db node ) :ps4)
        new-ps4 (update ps4 :assay-runs (comp set conj) (crux/entity (crux/db node) :ar4))
        ps5 (crux/entity (crux/db node ) :ps5)
        new-ps5 (update ps5 :assay-runs (comp set conj) (crux/entity (crux/db node) :ar5))]
(do
           (crux/submit-tx node [[:crux.tx/cas ps1 new-ps1]])
           (crux/submit-tx node [[:crux.tx/cas ps2 new-ps2]])
           (crux/submit-tx node [[:crux.tx/cas ps3 new-ps3]])
           (crux/submit-tx node [[:crux.tx/cas ps4 new-ps4]])
           (crux/submit-tx node [[:crux.tx/cas ps5 new-ps5]])
           (println "Assay runs associated with plate-sets."))))
  





(defn assoc-hl-with-prj [node]
     (let [prj1 (crux/entity (crux/db node ) :prj1)
          new-prj1 (update prj1 :hit-lists (comp set conj)
                            (crux/entity (crux/db node) :hl1)
                           (crux/entity (crux/db node) :hl2))
          prj2 (crux/entity (crux/db node ) :prj2)
          new-prj2 (update prj2 :hit-lists (comp set conj)
                           (crux/entity (crux/db node) :hl3)
                           (crux/entity (crux/db node) :hl4))       
          prj3 (crux/entity (crux/db node ) :prj3)
          new-prj3 (update prj3 :hit-lists (comp set conj)
                           (crux/entity (crux/db node) :hl5)
                           (crux/entity (crux/db node) :hl6))]
(do
    (crux/submit-tx node [[:crux.tx/cas prj1 new-prj1]])
    (crux/submit-tx node [[:crux.tx/cas prj2 new-prj2]])
    (crux/submit-tx node [[:crux.tx/cas prj3 new-prj3]])
    (println "Hit lists associated with projects."))))




    
(require '[clojure.inspector :as insp])

;;Number of IDs needed for example data set
;; project 10
;; plate-set 8
;; plate 29
;; sample 4648
;; assay-run 5



(defn load-eg-data
  [node]
  (do
    (load-assay-run-data node)
    (load-hit-lists node)
    (assoc-ar-with-ps node)
    (assoc-hl-with-prj node)
    (dbr/update-counters-post-eg-data node)
    ))





(defn diag-eg-data [node]
  (do
    (println "=========eg-data=================")
    (println ":hl6 --  "  (crux/entity (crux/db node) :hl6) )
    (println ":ar5 --  " (first (crux/entity (crux/db node) :ar5)) )
    (println ":plt29 --  " (first (crux/entity (crux/db node) :plt29)) )
    (println "a well/sample from :plt29 -- "(first (:wells (crux/entity (crux/db node) :plt29)) ))
    (println ":ps7 --  " (first (crux/entity (crux/db node) :ps7)) )
    (println ":prj10 --  " (first (crux/entity (crux/db node) :prj10)) )
    ))
