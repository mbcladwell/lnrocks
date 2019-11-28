(ns lnrocks.db-retriever
  (:require [clojure.set :as s]
            [crux.api :as crux]
            [clojure.java.browse :as browse])
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
(defn get-plates-in-project [node x]
  (crux/q (crux/db node)
          '{:find [e p  ]
            :where [[e :ps-name p]]}))
                    
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


(defn get-help-url-prefix [ node ]
  (:help-url-prefix (crux/entity (crux/db node ) :props)))


 (defn open-help-page [s]
   (browse/browse-url (str (get-help-url-prefix) s)))

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
(defn get-project-id ^Integer []
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


(defn set-plate-id [i]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :plate-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))

 (defn get-plate-id [ node ]
 (:plate-id (crux/entity (crux/db node ) :props)))

 (defn get-session-id ^Integer [ node ]
 (:session-id (crux/entity (crux/db node ) :props)))


   (defn set-session-id [node i]
  (let [
        old-props (crux/entity (crux/db node ) :props)
        new-props (assoc old-props :session-id i)
        dummy   (crux/submit-tx node [[:crux.tx/cas old-props new-props]]) 
         ]
    ))


     
(defn get-home-dir []
   (java.lang.System/getProperty "user.home"))
  
(defn get-temp-dir []
   (java.lang.System/getProperty "java.io.tmpdir"))
             
  
(defn get-working-dir []
   (java.lang.System/getProperty "user.dir"))

