(ns lnrocks.db-inserter
  (:require [clojure.string :only [split split-lines trim]]
            [crux.api :as crux]
            [clojure.set :as s]
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



(defn new-project [ node prj-name desc user-id]
  (let [prj-id (:start (dbr/counter node :project 1))
        session-id (:session-id (crux/entity (crux/db node) :props))
        doc {:crux.db/id (keyword (str "prj" prj-id))
             :project-sys-name (str "PRJ-" prj-id)
             :name prj-name
             :description desc
             :lnsession-id session-id
             :id prj-id
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


(defn new-wells
  "format: 96, 384, 1535"
  [node format unknown-n with-samples sample-start-id]
  (if with-samples     
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
        (loop [ counter 1
               spl-id sample-start-id
               dummy (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                          :sample-sys-name (str "SPL-"  spl-id )
                                                          :id  spl-id
                                                          :accession nil}]])
               filled-wells (assoc empty-wells (get well-vec (- counter 1)) spl-id)
               ]
          (if (> counter unknown-n )
            filled-wells
            (recur
             (+ counter 1)
             (+ spl-id 1)
             (crux/submit-tx node [[:crux.tx/put {:crux.db/id (keyword (str "spl"  spl-id))
                                                  :sample-sys-name (str "SPL-"  spl-id )
                                                  :id  spl-id
                                                  :accession nil}]])
             (assoc filled-wells (get well-vec (- counter 1)) spl-id))))))
        (let  [empty-wells (case format
                      96 util/map96wells
                      384 util/map384wells
                      1536 util/map1536wells)]
      empty-wells)
))



(defn new-plates
"return a vector of the new plate ids
here layout is the passed in map"
  [ node all-ids layout num-plates with-samples]
  (let [
        ;;layout (crux/entity (crux/db node) (keyword (str "lyt" layout-id)))
        unknown-n (:unknown-n layout)
        format (:plate-format-id layout)
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
                :wells (new-wells node format unknown-n with-samples spl-id-start)
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
                :wells (new-wells node format unknown-n with-samples spl-id-start)
                :plate-order counter
          }
         (conj new-plate-ids plt-id)
         (crux/submit-tx node [[:crux.tx/put doc]])))
    )))



(defn new-plate-set [ node ps-name desc plate-format-id plate-type-id  plate-layout-name-id num-plates project-id user-id with-samples]
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
             :plates (new-plates node all-ids layout num-plates with-samples)
             :plate-layout-name-id plate-layout-name-id
             }       ]
    (crux/submit-tx node [[:crux.tx/put doc]] )
    ps-id))






