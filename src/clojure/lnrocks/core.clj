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


(defn new-wells
  "format: 96, 384, 1535"
  [node format unknown-n sample-start-id]
  (let [ 
        empty-wells (case format
                      96 util/map96wells
                      384 util/map384wells
                      1536 util/map1536wells)
        well-vec util/ve]
    
    (loop [ counter 1
           spl-id sample-start-id
           dummy (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                      :sample-sys-name (str "SPL-"  spl-id )
                                                      :id  spl-id
                                                      :accession nil}]])
           (if (> counter unknown-n )
             )
           (recur
            (+ spl-id 1)
            (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                      :sample-sys-name (str "SPL-"  spl-id )
                                                      :id  spl-id
                                                      :accession nil}]])
     )
    )
  )


(defn new-plates
"return a vector of the new plate ids"
  [ node all-ids layout num-plates]
  (let [
        unknown-n (:unknown-n layout)
        ps-id (:plate-set all-ids)
        plt-id-start (:plate all-ids)
        spl-id-start (:sample all-ids)
        user-id (:user-id (crux/entity (crux/db node) :props))
        ]
    (loop [
           counter 1
           plt-id plt-id-start 
           doc   {:crux.db/id (keyword (str "plt" plt-id))
                :plate-sys-name (str "PLT-" plt-id)
                :plate-set-id ps-id
                :id plt-id
                :user-id user-id
                :wells #{}
                :plate-order counter
            }

           new-plate-ids []
           dummy (crux/submit-tx node [[:crux.tx/put doc]])]
           
          ;; dummy (crux/submit-tx node [[:crux.tx/put doc]])]
      (if (> counter  num-plates)
        (println (str num-plates " plates created. " new-plate-ids))
        (recur
         (+ counter 1)
         (+ plt-id 1)
         {:crux.db/id (keyword (str "plt" plt-id))
                :plate-sys-name (str "PLT-" plt-id)
                :plate-set-id ps-id
                :id plt-id
                :user-id user-id
                :wells #{}
                :plate-order counter
          }
         (conj new-plate-ids plt-id)
         (crux/submit-tx node [[:crux.tx/put doc]])))
    )))


(defn new-plate-set [ node ps-name desc plate-format-id plate-type-id  plate-layout-name-id num-plates project-id user-id]
  (let [
        layout (crux/entity (crux/db node) plate-layout-name-id)
        plate-format-id (:format-id layout)
        unknown-n (:unknown-n layout)    
        all-ids (dbr/get-ps-plt-spl-ids node  1 num-plates (* num-plates unknown-n) )
        ps-id (:plate-set all-ids)
        session-id (:session-id (crux/entity (crux/db node) :props))
        doc {:crux.db/id (keyword (str "ps" ps-id))
             :plate-set-sys-name (str "PS-" ps-id)
             :plate-set-name ps-name
             :description desc
             :lnsession-id session-id
             :plate-format-id plate-format-id
             :plate-type-id plate-type-id
             :id ps-id
             :user-id (:user-id (crux/entity (crux/db node) :props))
             :num-plates num-plates
             :project-id project-id
             :plates (new-plates node all-ids layout num-plates)
             :plate-layout-name-id plate-layout-name-id
             }       ]
    (crux/submit-tx node [[:crux.tx/put doc]] )
    ps-id))



;;(def  all-ids (dbr/get-ps-plt-spl-ids node  1 3 (* 3 92) ))
;;(new-plates node {:plate-set 11, :plate 54, :sample 5201}  :lyt1 3 )

;; (new-plate-set node "ps-name" "desc" 96 1  :lyt1 3 1 1)
  
;; (crux/entity (crux/db node) :plt55)
;;(new-plate-set node "MyNewPs" "a test of function" 96 1 1 2 1 1)
;;(:plates (crux/entity (crux/db node) :ps2))
                  ;;      (crux/entity (crux/db node ) :plt20)

;;(def barcode-file "/home/mbc/projects/lnrocks/egdata/barcodes/barcodes.txt")
;; ps7 has 10 plates

            


;;(crux/entity (crux/db node ) :spl4649)

;;(insp/inspect-tree (crux/entity (crux/db node ) :ps1))

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))
           
;;(count prj1)
;;(crux/submit-tx node [[:crux.tx/put a]] )

;;(insp/inspect-tree (crux/entity (crux/db node) :prj1))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

(println "In main"))


