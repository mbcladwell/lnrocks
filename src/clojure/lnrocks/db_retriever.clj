(ns lnrocks.db-retriever
  (:require [clojure.set :as s]
            [crux.api :as crux])
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
                           :work-list 0)]

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


;;(new-plate-set "my set 1" "desc" 3 96 1 1 1 2 true)
;;(:plates (crux/entity (crux/db node ) :PS-13))
;;(crux/entity (crux/db node ) :counters)
(defn get-plates-in-project [node x]
  (crux/q (crux/db node)
          '{:find [e p  ]
            :where [[e :ps-name p]]}))
                    
  ;;(count (get-plates-in-project 2))








;;     (defn get-help-url-prefix []
;;           (c/get-at! props [:assets :conn :help-url-prefix ]))


;; (defn open-help-page [s]
;;   (browse/browse-url (str (cm/get-help-url-prefix) s)))

;; (defn get-all-data-for-assay-run
;;   "provides a map"
;;   [ assay-run-id ]
;;      )

;; (defn set-project-id [i]
;;   (c/with-write-transaction [props tx]
;;     (c/assoc-at tx  [:assets :session :project-id] i)))

;; ;;https://stackoverflow.com/questions/9457537/why-does-int-10-produce-a-long-instance
;; ;; dont cast to int, gets promoted to Long upon java interop
;; (defn get-project-id ^Integer []
;;   (c/get-at! props [:assets :session :project-id ]))

;; (defn set-project-sys-name [s]
;;     (c/with-write-transaction [props tx]
;;         (c/assoc-at tx  [:assets :session :project-sys-name] s)))
;; ;;(set-project-sys-name "PRJ-??")
;;   ;;(print-ap)


;; (defn get-project-sys-name []
;;     (c/get-at! props [:assets :session :project-sys-name ]))

;;   (defn set-plate-set-sys-name [s]
;;       (c/with-write-transaction [props tx]

;;         (c/assoc-at tx  [:assets :session :plate-set-sys-name] s)))

;; (defn get-plate-set-sys-name []
;;   (c/get-at! props [:assets :session :plate-set-sys-name ]))

;;   (defn set-plate-set-id [i]
;;       (c/with-write-transaction [props tx]

;;         (c/assoc-at tx  [:assets :session :plate-set-id ] i)))

;; (defn get-plate-set-id []
;;   (c/get-at! props [:assets :session :plate-set-id ]))


;; (defn set-plate-id [i]
;;    (c/with-write-transaction [props tx]
;;         (c/assoc-at tx  [:assets :session :plate-id ] i)))

;; (defn get-plate-id []
;;   (c/get-at! props [:assets :session :plate-id ]))


;; (defn get-session-id ^Integer []
;;   (c/get-at! props [:assets :session :session-id ]))

;; ;;(get-session-id)

;; (defn set-session-id [i]
;;   (c/with-write-transaction [props tx]
;;     (c/assoc-at tx  [:assets :session :session-id] i)))
  
;; (defn get-home-dir []
;;    (java.lang.System/getProperty "user.home"))
  
;; (defn get-temp-dir []
;;    (java.lang.System/getProperty "java.io.tmpdir"))
             
  
;; (defn get-working-dir []
;;    (java.lang.System/getProperty "user.dir"))

