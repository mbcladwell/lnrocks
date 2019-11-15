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

;;(crux/entity (crux/db node ) :counters)
;;(get-ps-plt-spl-ids 1 2 3)

(defn get-plate-layout
  ;;x is :id e.g.  41
  [node x]
  (filter #(= (:id %) x) (:plate-layout  (crux/entity (crux/db node ) :plate-layout))))



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

