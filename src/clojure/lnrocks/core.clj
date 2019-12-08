(ns lnrocks.core
  (:require [crux.api :as crux]
            [lnrocks.util :as util]
            [lnrocks.db-retriever :as dbr]
            [lnrocks.db-inserter :as dbi]
           [lnrocks.db-init :as init]
           [lnrocks.eg-data :as egd]
           [clojure.java.browse :as browse]

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

  
(def user-groups  ["admin" "user"])
(defn get-user-groups []
  user-groups)

(def assay-types ["ELISA"  "Octet"  "SNP"  "HCS"  "HTRF"  "FACS"])
(defn get-assay-types []
  assay-types)

(def plate-types ["assay"  "rearray"  "master"  "daughter"  "archive"  "replicate"])

(defn get-plate-types []
  plate-types)

(def formats [96 384 1536])
(defn get-plate-formats []
  formats)


;;(dbr/get-plates-for-plate-set-id node 1)

(defn get-all-projects []
  (dbr/get-all-projects node))

;;(get-all-projects)

(defn get-plate-sets-for-project [ prj-id]
  (dbr/get-plate-sets-for-project node prj-id))

;;(get-plate-sets-for-project 1)


(defn get-plates-for-plate-set-id [ ps-id]
  (dbr/get-plates-for-plate-set-id node ps-id))

;;(dbr/get-plates-for-plate-set-id node 1)

(defn get-wells-for-plate-id [ plt-id]
  (dbr/get-wells-for-plate-id node plt-id))

;;(dbr/get-wells-for-plate-id node 1)


;;(insp/inspect-tree (crux/entity (crux/db node) :spl1))
;;(insp/inspect-tree  new-ps1)
;;    (egd/assoc-plt-with-ps node)

;; (def      ps1 (crux/entity (crux/db node ) :ps1))
  ;;   (def  new-ps1  (update ps1 :plates (comp set conj)
    ;;                    (crux/entity (crux/db node ) :plt1)
      ;;                  (crux/entity (crux/db node ) :plt2)))       



 (defn get-session-id ^Integer [ ]
 (:session-id (crux/entity (crux/db node ) :props)))

 (defn get-user-group [ ]
 (:user-group (crux/entity (crux/db node ) :props)))

;;(get-user-group)

(defn get-user [ ]
 (:user (crux/entity (crux/db node ) :props)))

;;(get-user)

(defn get-help-url-prefix [ ]
  (:help-url-prefix (crux/entity (crux/db node ) :props)))

 (defn open-help-page [s]
   (browse/browse-url (str (get-help-url-prefix) s)))

(defn get-project-sys-name []
   (:project-sys-name (crux/entity (crux/db node ) :props)))

;;(get-project-sys-name)

(defn get-project-id []
   (:project-id (crux/entity (crux/db node ) :props)))

(defn get-project-desc [prj-id]
(:description (crux/entity (crux/db node ) (keyword (str "prj" prj-id)))  ))

(defn get-plate-set-sys-name []
  (:plate-set-sys-name (crux/entity (crux/db node ) :props)))

(defn get-plate-set-id []
  (:plate-set-id (crux/entity (crux/db node ) :props)))

(defn get-plate-sys-name []
  (:plate-sys-name (crux/entity (crux/db node ) :props)))

(defn get-plate-id []
  (:plate-id (crux/entity (crux/db node ) :props)))


(defn delete-project [prj-id]
   (crux/submit-tx  node  [[:crux.tx/evict (keyword (str "prj" prj-id))]])  )

;;(delete-project 6)

(defn update-project
  "see database inserter"
  [name descr prj-id]
  (let [ old (crux/entity (crux/db node) (keyword (str "prj" prj-id)))
        new1 (assoc old :name name)
        new2 (assoc new1 :description descr)
        new3 (assoc new2 :lnsession-id (get-session-id))]
  (crux/submit-tx node [[:crux.tx/cas old new3]])))

;;(update-project "newname" "newdescr" 5)
;;(insp/inspect-tree (crux/entity (crux/db node) :prj5))

(defn insert-user
  "In DialogAddUser"
  [name tag password group]
  (let [ user-id (dbr/counter node :user 1)
        new-user {:crux.db/id (keyword (str "lnuser" user-id))
                  :id user-id
                  :group group
                  :lnuser_name name
                  :tags #{[tag]}
                  :password password}]
    (crux/submit-tx node [[:crux.tx/put new-user]])))



(defn get-plate-set-owner-id [ps-id]
  (:owner-id (crux/entity (crux/db node ) (keyword (str "ps" ps-id)))  ))

;;(get-plate-set-owner-id 1)
;;(insp/inspect-tree (crux/entity (crux/db node) :ps5))



(defn get-plate-layout-names
 "select id, name from plate_layout_name WHERE plate_format_id = ?;"
  [ format]
(let [data (crux/q (crux/db node)
	           '{:find [n s1 ]
	             :where [[e :id n]
                             [e :name s1]
                             [e :plate-format-id format]
                            ]
                     :order-by [[n :desc]]})
      colnames ["ID" "Plate Layout Name" ] ]
  (into {} (java.util.HashMap.
            {":colnames" colnames
             ":data" data}))))

  ;;(get-plate-layout-names 96)
;;(insp/inspect-tree (crux/entity (crux/db node ) :lyt1))


(defn get-worklist
  "SELECT sample_id, source_plate, source_well, dest_plate, dest_well FROM worklists WHERE rearray_pairs_id = ?;
:worklist now part of plate-set"
  [wl-id]
(let [data (crux/q (crux/db node)
	           '{:find [n s1 ]
	             :where [[e :id n]
                             [e :name s1]
                             [e :plate-format-id format]
                            ]
                     :order-by [[n :desc]]})
      colnames ["ID" "Plate Layout Name" ] ]
  (into {} (java.util.HashMap.
            {":colnames" colnames
             ":data" data}))))

;;(insp/inspect-tree (crux/entity (crux/db node ) :ps1))


  


(defn get-plate-set-data [ ps-id])


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
  (lnrocks.DialogMainFrame. )
  )




