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


(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))


(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  [x coll]
  (map #(dissoc % :id) (filter #(= (:id %) x) coll ) ))


(def props
  {:crux.db/id :props
   :help-url-prefix "http://labsolns.com/software/"
   :session-id 1
   :project-id 1
   :project-sys-name "PRJ-1"
   :plate-set-sys-name "PS-1"
   :plate-set-id 1})
  

(def helpers
  [{:crux.db/id :plate-formats :96 96 :384 384 :1536 1536}
   {:crux.db/id :plate-type 1 "assay" 2 "rearray" 3 "master" 4 "daughter" 5 "archive" 6 "replicate"}
   ;;{:crux.db/id :plate-layout :plate-layout (load-plate-layouts)}
   {:crux.db/id :assay-type  1 "ELISA" 2 "Octet" 3 "SNP" 4 "HCS" 5 "HTRF" 6 "FACS"}
   {:crux.db/id :well-type  1 "unknown" 2 "positive" 3 "negative" 4 "blank" 5 "edge"}
  ;; {:crux.db/id :well-numbers :well-numbers (dbi/load-well-numbers) }
   {:crux.db/id :layout-src-dest :layout-src-dest   [{:source 1 :dest  2}{:source 1 :dest 3}{:source 1 :dest 4}{:source 1 :dest 5}{:source 1 :dest 6}{:source 7 :dest 8}{:source 7 :dest 9}{:source 7 :dest 10}{:source 7 :dest 11}{:source 7 :dest 12}{:source 13 :dest 14}{:source 13 :dest 15}{:source 13 :dest 16}{:source 13 :dest 17}{:source 13 :dest 18}{:source 19 :dest 20}{:source 19 :dest 21}{:source 19 :dest 22}{:source 19 :dest 23}{:source 19 :dest 24}{:source 25 :dest 26}{:source 25 :dest 27}{:source 25 :dest 28}{:source 25 :dest 29}{:source 25 :dest 30}{:source 31 :dest 32}{:source 31 :dest 33}{:source 31 :dest 34}{:source 31 :dest 35}{:source 31 :dest 36}{:source 37 :dest 41}{:source 38 :dest 41}{:source 39 :dest 41}{:source 40 :dest 41}]}
   ])





     
     
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
  (into {} { :id (Integer/parseInt(:id x))
            :well (Integer/parseInt(:well x ))
            :type  (Integer/parseInt(:type x ))
            :reps (Integer/parseInt(:reps x ))
            :target (Integer/parseInt(:target x ))}))

(defn process-layout-names
  "processes that tab delimitted, R generated layouts for import
   order is important; must correlate with SQL statement order of ?'s"
  [x]
  (into {} { :id (Integer/parseInt(:id x))
            :crux.db/id (keyword (str "lyt"(:id x )))
            :sys-name (:sys-name x )
            :name (:name x )
            :description (:description x )
            :plate-format-id (Integer/parseInt(:plate-format-id x ))
            :replicates (Integer/parseInt(:replicates x))
            :targets (Integer/parseInt(:targets x))
            :use-edge (Integer/parseInt(:use-edge x))
            :num-controls (Integer/parseInt(:num-controls x))
            :unknown-n (Integer/parseInt(:unknown-n x))
            :control-loc (:control-loc x)
            :source-dest (:source-dest x)  }))



(defn load-plate-layouts [node]
  ;;add data to layout names using the key :layout
  (let   [table (util/table-to-map "resources/data/plate_layouts_for_import.txt")
          layout-data (into [] (map #(process-layout-data %) table))
          table2 (util/table-to-map "resources/data/plate_layout_name.txt")
          layout-names (into [] (map #(process-layout-names %) table2))
          result (map #(assoc % :layout (extract-data-for-id (:id %)  layout-data)) layout-names)]         
    (loop [counter 1
           new-pl  (first (filter #(= (:id %) counter) result))
           dummy    (crux/submit-tx node [[:crux.tx/put new-pl]] )]
      (if (> counter (+ 1 (count result)))
        (println "Plate layouts loaded!")
        (recur
         (+ counter 1)
         (first (filter #(= (:id %) counter) result))
          (crux/submit-tx node [[:crux.tx/put new-pl]] )
         )))))




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
            :parentwell (Integer/parseInt (String. (:parentwell x )))
            }))



(defn load-well-numbers [node]
  (let   [table (util/table-to-map "resources/data/well_numbers_for_import.txt")
          content (into [] (map #(process-well-numbers-data %) table))
          formats [96 384 1538]
          ]
         (loop [counter 0
                new-wn (map #(dissoc  % :format) (filter #(= (:format %) (get formats counter)) content))
                new-wn2 {:crux.db/id (keyword (str "wn" (get formats counter))) :format (get formats counter) :well-nums new-wn }
                dummy    (crux/submit-tx node [[:crux.tx/put new-wn2]] )]
           (if (> counter 2)
             (println "Well numbers loaded!")
             (recur
              (+ counter 1)
              (map #(dissoc  % :format) (filter #(= (:format %) (get formats counter)) content))
               {:crux.db/id (keyword (str "wn" (get formats counter))) :format (get formats counter) :well-nums new-wn }
              (crux/submit-tx node [[:crux.tx/put new-wn2]] )
              )))))



(defn initialize-db [node]
  (do
    (crux/submit-tx node [[:crux.tx/put counters]] )
    (crux/submit-tx node [[:crux.tx/put props]] )
    (easy-ingest node helpers)
    (load-well-numbers node)
    (load-plate-layouts node)))
