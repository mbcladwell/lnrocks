(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
           [lnrocks.db-init :as init]
           [lnrocks.eg-data :as egd]
            
            [clojure.inspector :as insp]
            [clojure.java.io :as io]
            [clojure.set :as s]
              )
  (:import [crux.api ICruxAPI ])
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html


(defn define-db-var []
  
   (def ^crux.api.ICruxAPI node
     (crux/start-node
      {:crux.node/topology :crux.standalone/topology
       :crux.node/kv-store "crux.kv.rocksdb/kv"
       :crux.standalone/event-log-dir "data/eventlog-1"
       :crux.kv/db-dir "data/db-dir1"
       :crux.standalone/event-log-kv-store "crux.kv.rocksdb/kv"})))


(if (.exists (io/as-file "data"))
  (do
    (define-db-var)
    (println "db already exists"))
 (do
   (println "initializing database at startup.")
   (define-db-var)

   (init/initialize-db node)
   (egd/load-eg-data node)
   (init/diag-init node)
   (egd/diag-eg-data node)
   ))

;;(egd/load-eg-plate-sets node)
 ;;get assay runs   (println ":ps3 --  " (first (:wells (crux/entity (crux/db node) :ps1)) )
;;(insp/inspect-tree new-ps5)


(defn extract-data-for-id
  ;;get the data for a single id; remove the id from the collection
  ;;x the id
  ;;coll the collection;; its id must be :id
  [x coll]
  (map #(dissoc % :id) (filter #(= (:id %) x) coll ) ))


(defn process-layout-file
  "processes a user supplied layout file"
  [x]
  (into {} { 
            :well (Integer/parseInt(:well x ))
            :type  (Integer/parseInt(:type x ))
           }))




(defn new-plate-layout [ plate-layout-name, descr, plate-format-id]
  (let   [table (util/table-to-map "resources/data/plate_layouts_for_import.txt")
          layout-data nil ;;(into [] (map #(process-layout-data %) table))
          table2 (util/table-to-map "resources/data/plate_layout_name.txt")
          layout-names nil ;;(into [] (map #(process-layout-names %) table2))
          result (map #(assoc % :layout (extract-data-for-id (:id %)  layout-data)) layout-names)]         
    (loop [counter 1
           new-pl  (first (filter #(= (:id %) counter) result))]
         ;;  dummy    (crux/submit-tx node [[:crux.tx/put new-pl]] )]
      (if (> counter (+ 1 (count result)))
        (println "Plate layouts loaded!")
        (recur
         (+ counter 1)
         (first (filter #(= (:id %) counter) result))
        ;;  (crux/submit-tx node [[:crux.tx/put new-pl]] )
         )))))

  

(defn get-all-plates-for-ps [node ps-id]
(let [data (crux/q (crux/db node)
	           '{:find [n i i2 ]
	             :where [[e :id n]
                             [e :plate-order i]
                             [e :plate-set-id i2]
                             ]
                     :order-by [[n :desc]]})
      colnames ["PlateID" "Order" "Plate Set ID"]]
  (into [] (cons colnames data ))))

(get-all-plates-for-ps node 1)

;;(crux/entity (crux/db node ) :ps1)
;;(insp/inspect-tree (crux/entity (crux/db node ) :ps1))

;;(def  all-ids (dbr/get-ps-plt-spl-ids node  1 3 (* 3 92) ))
;;(new-plates node {:plate-set 11, :plate 54, :sample 5201}  1 3 true)

;; (new-plate-set node "ps-name" "desc" 96 1  :lyt1 3 1 1 true)
  
;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))
           
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(insp/inspect-tree (crux/entity (crux/db node) :ar1))

;;(insp/inspect-tree (lnrocks.eg-data/load-assay-run-data node))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "In main")
  (lnrocks.DatabaseManager. ))


