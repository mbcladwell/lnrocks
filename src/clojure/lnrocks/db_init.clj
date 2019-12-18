(ns lnrocks.db-init
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
;;;Database setup
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;number is the last used
;;use + 1 as the next available
(def counters
 {:crux.db/id :counters
  :project 0
  :plate-set 0
  :plate 0
  :sample 0
  :hit-list 0
  :assay-run 0
  :work-list 0
  :session-id 1
  :layout 0
  :user 2}
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
   :session-id :lnsession1
   :project-id :prj1
   :user-group "admin"
   :user-name "ln_admin"
   :user-id :lnuser1
   :project-sys-name "PRJ-1"
   :plate-set-sys-name "PS-1"
   :plate-set-id :ps1
   :plate-id :plt1
   :plate-sys-name "PLT-1"})
  


(def helpers
  [{:crux.db/id :plate-formats :96 96 :384 384 :1536 1536}
   {:crux.db/id :plate-type :1 "assay" :2 "rearray" :3 "master" :4 "daughter" :5 "archive" :6 "replicate"}
   ;;{:crux.db/id :plate-layout :plate-layout (load-plate-layouts)}
   {:crux.db/id :assay-type  :1 "ELISA" :2 "Octet" :3 "SNP" :4 "HCS" :5 "HTRF" :6 "FACS"}
   {:crux.db/id :well-type  :1 "unknown" :2 "positive" :3 "negative" :4 "blank" :5 "edge"}
  ;; {:crux.db/id :well-numbers :well-numbers (dbi/load-well-numbers) } 
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
            :source-dest (:source-dest x)
                }))



(defn load-plate-layouts
  "add data to layout names using the key :layout
  source layouts have a :dest key that is a set of associated destinations"
  [node]
  (let   [table (util/table-to-map "resources/data/plate_layouts_for_import.txt")
          layout-data (into [] (map #(process-layout-data %) table))
          table2 (util/table-to-map "resources/data/plate_layout_name.txt")
          layout-names (into [] (map #(process-layout-names %) table2))
          result (map #(assoc % :layout (extract-data-for-id (:id %)  layout-data)) layout-names)]         
    (loop [counter 1
           new-pl  (first (filter #(= (:id %) counter) result))
           dummy (if (= (:source-dest new-pl) "source")
                   (assoc new-pl :dest (:dest (first (filter  #(= (:source %) (:id new-pl)) src-dest)))))
           dummy2   nil
           ]
      (if (> counter (+ 1 (count result)))
        (println "Plate layouts loaded!")
        (recur
         (+ counter 1)
         (first (filter #(= (:id %) counter) result))
         (if (= (:source-dest new-pl) "source")
           (do
           (assoc new-pl :dest (:dest (first (filter  #(= (:source %) (:id new-pl)) src-dest))))
           (println (str ":id " (:id new-pl) "       :source-dest " (:source-dest new-pl) "      :dest " (:dest new-pl)     ))     
           ))
         (crux/submit-tx node [[:crux.tx/put new-pl]] )
         )))))


(def src-dest [ {:source 1 :dest #{:lyt2 :lyt3 :lyt4 :lyt5 :lyt6}}
                {:source 7 :dest #{:lyt8 :lyt9 :lyt10 :lyt11 :lyt12}}
               {:source 13 :dest #{:lyt14 :lyt15 :lyt16 :lyt17 :lyt18}}
               {:source 19 :dest #{:lyt20 :lyt21 :lyt22 :lyt23 :lyt24}}
               {:source 25 :dest #{:lyt26 :lyt27 :lyt28 :lyt29 :lyt30}}
               {:source 31 :dest #{:lyt32 :lyt33 :lyt34 :lyt35 :lyt36}}
               {:source 37 :dest #{:lyt41}}
               {:source 38 :dest #{:lyt41}}
               {:source 39 :dest #{:lyt41}}
               {:source 40 :dest #{:lyt41}}])



(defn assoc-lyt-src-dest [node]
  (loop [ counter 0
         sd-map (nth src-dest counter)
         old-lyt (crux/entity (crux/db node) (keyword (str "lyt" (:source sd-map) ))) 
         new-lyt (assoc old-lyt :dest (:dest sd-map))
         dummy (println (str "counter: " counter "  old id: " (:id old-lyt) "  new id: " (:id new-lyt)  ))
         ;;dummy2 (crux/submit-tx node [[:crux.tx/cas old-lyt new-lyt]])          
         ]
    (if (= counter (count src-dest))
           (println "Layout src-dest associations made")
           (recur
            (+ counter 1)
            (nth src-dest counter)
            (crux/entity (crux/db node) (keyword (str "lyt" (:source sd-map) )))
            (assoc old-lyt :dest (:dest sd-map))
            (println (str "counter: " counter "  old id: " (:id old-lyt) "  new id: " (:id new-lyt)  ))
            ;;(crux/submit-tx node [[:crux.tx/cas old-lyt new-lyt]]) 
            ))))



(def lnusers
  [{:crux.db/id :lnuser1
    :id 1
    :group "administrator"
    :lnuser-name "ln_admin"
    :tags #{["ln_admin@labsolns.com"]}
    :password "welcome"}

   {:crux.db/id :lnuser2
    :id 2
    :group "user"
    :lnuser-name "ln_user"
    :tags #{["ln_user@labsolns.com"]}
    :password "welcome"}
   ]
  )

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
          formats [96 384 1536]
          ]
         (loop [counter 0
                new-wn (map #(dissoc  % :format) (filter #(= (:format %) (get formats counter)) content))
                new-wn2 {:crux.db/id (keyword (str "wn" (get formats counter))) :format (get formats counter) :well-nums new-wn }
                dummy    (crux/submit-tx node [[:crux.tx/put new-wn2]] )]
           (if (> counter 3)
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
    (delay 2000)
    (easy-ingest node helpers)
    (easy-ingest node lnusers)
    (delay 2000)
    (load-well-numbers node)
    (delay 2000)
    (load-plate-layouts node)))

(defn diag-init [node]
  (do
(println ":lyt1 --  " (first (crux/entity (crux/db node) :lyt1)) )
(println ":lyt41 --  " (first (crux/entity (crux/db node) :lyt41)) )
(println ":counters --  " (crux/entity (crux/db node) :counters) )
(println ":props --  " (crux/entity (crux/db node) :props) )
(println ":plate-formats --  " (first (crux/entity (crux/db node) :plate-formats)) )
(println ":layout-src-dest --  " (first (crux/entity (crux/db node) :layout-src-dest)) )
(println ":wn96 --  " (first (crux/entity (crux/db node) :wn96)) )
(println ":wn1536 --  " (first (crux/entity (crux/db node) :wn1536)) )

    ))
