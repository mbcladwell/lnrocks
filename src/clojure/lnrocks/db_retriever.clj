(ns lnrocks.db-retriever
  (:require [clojure.set :as s]
            [crux.api :as crux]
            [clojure.java.browse :as browse]
            [lnrocks.util :as util])
            ;;   [clojure.data.csv :as csv]
          ;;  [clojure.java.io :as io])            
 ;; (:import [javax.swing.JOptionPane])
  )


(defn counter
  ;;entity:  plate, plate-set, sample or project
  ;;need: how many will be created
  ;;returns vector of start and end id
  ;;can only get one per method or transaction aborted
  [node entity need ]
  (let [old (crux/entity (crux/db node) :counters)
        start (+ 1 (entity old))
        end (+ start (- need 1))
        new (assoc old entity end)]
    (crux/submit-tx node [[:crux.tx/cas old new]])
    {:start start :end end}))



(defn get-ps-plt-spl-ids
  ;;args are the integer quantities needed
  ;; :plate-set :plate :sample
  ;;returns vector of start ids for each  
  [ node need-ps need-plt need-spl ]
  (let [orig-counters (crux/entity (crux/db node) :counters)
        old-ps (:plate-set orig-counters)
        ps (+ 1 old-ps) ;;this is the start id
        ps-end (+ old-ps need-ps) ;;this goes into the db as the next start id
        old-plt (:plate orig-counters)
        plt (+ 1 old-plt) ;;this is the start id
        plt-end (+ old-plt need-plt) ;;this goes into the db as the next start id
        old-spl (:sample orig-counters)
        spl (+ 1 old-spl) ;;this is the start id
        spl-end (+ old-spl need-spl) ;;this goes into the db as the next start id
        new1 (assoc orig-counters :plate-set ps-end)
        new2 (assoc new1 :plate plt-end)
        new3 (assoc new2 :sample spl-end)
        ]
    (crux/submit-tx node [[:crux.tx/cas orig-counters new3]])
    {:plate-set ps :plate plt :sample spl}))


;;Number of IDs needed for example data set
;; project 10
;; plate-set 8
;; plate 29
;; sample 4648
;; assay-run 5

(defn update-counters-post-eg-data [node]
 (let [orig-counters (crux/entity (crux/db node) :counters)
       new-counters (assoc orig-counters
                           :project 10
                           :plate-set 8
                           :plate 29
                           :sample 4648
                           ::assay-run 5
                           :hit-list 6
                           :work-list 0
                           :layout 42)]

   (crux/submit-tx node [[:crux.tx/cas orig-counters new-counters]])
   (println "")
   (println "====updated counters=====")
     (println "project:   10" )
     (println "plate-set: 8" )
     (println "plate:     29" )
     (println "sample:    4648" )
     (println "assay-run: 5" )
     (println "hit-list:  6" )
     (println "work-list: 0" )
 

   ))
 
;;(crux/entity (crux/db node ) :counters)
;;(get-ps-plt-spl-ids 1 2 3)

(defn get-plate-layout
  ;;x is :id e.g.  41
  [node x]
  (filter #(= (:id %) x) (:plate-layout  (crux/entity (crux/db node ) :plate-layout))))

(defn get-well-numbers
  ;;x: 96, 384, or 1536
  [node x]
  (filter #(= (:format %) x) (:well-numbers  (crux/entity (crux/db node ) :well-numbers))))

;;(get-well-numbers 96)  

(defn get-num-samples-for-plate-set-id
"subtract nil count from format"
  [ node n ]
  (let [
        ps (crux/entity (crux/db node ) (keyword (str "ps" n)))
        format (:plate-format-id ps)
        list-of-well-maps (map #(:wells %) (:plates ps))
        count-of-nils (reduce + (map #(count %) (map #(filter  (comp nil? last) %) list-of-well-maps)))
        ]
    (- (* format (count (:plates ps))) count-of-nils)))

;;(new-plate-set "my set 1" "desc" 3 96 1 1 1 2 true)
;;(:plates (crux/entity (crux/db node ) :PS-13))
;;(crux/entity (crux/db node ) :counters)
;; (defn get-plates-in-project [node x]
;;   (crux/q (crux/db node)
;;           '{:find [e p  ]
;;             :where [[e :ps-name p]]}))
                    
  ;;(count (get-plates-in-project 2))


(defn register-session [ node ]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        session-id (counter node :session 1)
        new-props (assoc old-props :session session-id)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]])
 
        ]
  session-id
))





;; (defn get-all-data-for-assay-run
;;   "provides a map"
;;   [ assay-run-id ]
;;      )

 (defn set-project-id [ node i]
   (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :project-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))

;;https://stackoverflow.com/questions/9457537/why-does-int-10-produce-a-long-instance
;; dont cast to int, gets promoted to Long upon java interop
(defn get-project-id ^Integer [node]
  (:project-id (crux/entity (crux/db node ) :props)))

(defn set-project-sys-name [ node s ]
   (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :project-sys-name s)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))


 (defn get-project-sys-name [node]
  (:project-sys-name (crux/entity (crux/db node ) :props)))

  (defn set-plate-set-sys-name [ node s]
    (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :plate-set-sys-name s)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))


  (defn get-plate-set-sys-name [node]
 (:plate-set-sys-name (crux/entity (crux/db node ) :props)))


    
  (defn set-plate-set-id [ node i]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :plate-set-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))
   

 (defn get-plate-set-id [ node ]
 (:plate-set-id (crux/entity (crux/db node ) :props)))


(defn set-plate-id [node i]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :plate-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))

 (defn get-plate-id [ node ]
 (:plate-id (crux/entity (crux/db node ) :props)))



   (defn set-session-id [node i]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :session-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))

(defn set-user []
  )

(defn set-user-id []
  )

(defn get-user-id []

  )

(defn set-authenticated []

  )

(defn get-source [])
(defn get-db-user [])
(defn get-db-password [])
(defn get-connection-string [])


(defn get-home-dir []
   (java.lang.System/getProperty "user.home"))
  
(defn get-temp-dir []
   (java.lang.System/getProperty "java.io.tmpdir"))
             
  
(defn get-working-dir []
   (java.lang.System/getProperty "user.dir"))


(defn get-all-projects [node]
(let [data (crux/q (crux/db node)
	           '{:find [n s1 s2 s3 s4]
	             :where [[e :id n]
                             [e :project-sys-name s1]
                             [e :name s2]
                             [e :owner-id n2]
                             [e :description s4]
                             [e2 :id n2]
                             [e2 :lnuser_name s3]]
                     :order-by [[n :desc]]})
      colnames ["ID" "ProjectID" "Name" "Owner" "Description"] ]
  (into {} (java.util.HashMap.
            {":colnames" colnames
             ":data" data}))))



(defn get-plate-sets-for-project [node prj-id]
  
   (let [data  (crux/q (crux/db node)
	           {:find '[n s1 s2 s3 n3 s4 s5 s6 s8 ]
	             :where '[[e :id n]
                              [e :plate-set-sys-name s1]
                              [e :plate-set-name s2]
                              [e :plate-format s3]
                              [e :num-plates n3]
                              [e :plate-type s4]
                              [e :plate-layout-name-id s5]
                              [e :descr s6]
                             ;;[e :worklist s7]
                              [e2 :id s5]
                              [e2 :name s8]
                              [e2 :layout n5]
                              [e :project-id n2]]
                     :args [{'n2 prj-id}]
                    :order-by [['n :desc]]})
    ;;  data2  (doall (map #(if (nil? (nth % 9)) (assoc % 9 "NA"))  data))
      colnames ["PlateSetID" "PlateSetName" "Name" "Format" "# plates" "Type" "Layout" "Description" "Layout"  ]]
  (into {} (java.util.HashMap.
            {":colnames" colnames
             ":data" data} ))))

(defn get-plates-for-plate-set-id
  [node psid]
  (let [
        data (crux/q (crux/db node)
	             {:find '[n1  n2 n3 n4 s3 s2 ]
	              :where '[[e :plate-set-id n1]  ;;e is plate
                               [e :id n2]
                               [e :plate-order n3]
                               [e :barcode s2]
                               [e2 :id n1 ]   ;;e2 is plate-set
                               [e2 :plate-type s3]
                               [e2 :plate-format n4]
                               ]
                       :args [{'n1 psid}]
                       :order-by [['n2 :desc]]})
        ps (str "PS-" (nth (first data) 0))
        plt (str "PLT-" (nth (first data) 1))
        colnames ["PlateSetID" "PlateID"  "Order" "Format" "Type" "Barcode ID"]]
    (into {} (java.util.HashMap.
              {":colnames" colnames
               ":data" data} ))))

(defn get-wells-for-plate-id
  [node plt-id]
  (let [
        data (crux/q (crux/db node)
	             {:find '[n1  n2  w]
	              :where '[[e :plate-set-id n1]  ;;e is plate
                               [e :id n2]
                               [e :plate-order n3]
                               [e :wells w]                               
                               ]
                       :args [{'n2 plt-id}]
                      :order-by [['n2 :desc]]})
        ps (str "PS-" (nth (first data) 0))
        plt (str "PLT-" (nth (first data) 1))
        wells  (nth (first data) 2)
        dummy (println (str "wells: " wells))
        num-wells (count wells)
        vec-wells (case num-wells 96 util/vec96wells 384 util/vec384wells 1536 util/vec1536wells)
        data2 (loop [
                     counter 1
                     well (get vec-wells  (- counter 1))
                     mydata [[ps plt (name well)  counter  (str "SPL-" (well wells)) (:accession (crux/entity (crux/db node) (keyword (str "spl" (well wells)))))  ] ]
                     ;;dummy (clojure.pprint/pprint (str  mydata "\n"))
                    ]
               (if (= counter 96)
                 mydata
                 (recur
                  (+ counter 1)
                  (get vec-wells  (- counter 1))
                  (conj mydata [ps plt (name well)  counter  (str "SPL-" (well wells)) (:accession (crux/entity (crux/db node) (keyword (str "spl" (well wells)))))  ])
                  ;;nil
                 ;; (clojure.pprint/pprint mydata)
                  )))   
      colnames ["PlateSetID" "PlateID"  "Well" "Well_NUM" "Sample" "Accession"]]
    (into {} (java.util.HashMap.
              {":colnames" colnames
               ":data" data2} ))))

