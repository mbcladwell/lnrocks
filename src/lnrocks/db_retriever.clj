(ns lnrocks.db-retriever
  (:require [clojure.set :as s]
            [crux.api :as crux])
            ;;   [clojure.data.csv :as csv]
          ;;  [clojure.java.io :as io])            
 ;; (:import [javax.swing.JOptionPane])
  )



    (defn get-help-url-prefix []
          (c/get-at! props [:assets :conn :help-url-prefix ]))


(defn open-help-page [s]
  (browse/browse-url (str (cm/get-help-url-prefix) s)))

(defn get-all-data-for-assay-run
  "provides a map"
  [ assay-run-id ]
     )

(defn set-project-id [i]
  (c/with-write-transaction [props tx]
    (c/assoc-at tx  [:assets :session :project-id] i)))

;;https://stackoverflow.com/questions/9457537/why-does-int-10-produce-a-long-instance
;; dont cast to int, gets promoted to Long upon java interop
(defn get-project-id ^Integer []
  (c/get-at! props [:assets :session :project-id ]))

(defn set-project-sys-name [s]
    (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :project-sys-name] s)))
;;(set-project-sys-name "PRJ-??")
  ;;(print-ap)


(defn get-project-sys-name []
    (c/get-at! props [:assets :session :project-sys-name ]))

  (defn set-plate-set-sys-name [s]
      (c/with-write-transaction [props tx]

        (c/assoc-at tx  [:assets :session :plate-set-sys-name] s)))

(defn get-plate-set-sys-name []
  (c/get-at! props [:assets :session :plate-set-sys-name ]))

  (defn set-plate-set-id [i]
      (c/with-write-transaction [props tx]

        (c/assoc-at tx  [:assets :session :plate-set-id ] i)))

(defn get-plate-set-id []
  (c/get-at! props [:assets :session :plate-set-id ]))


(defn set-plate-id [i]
   (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :plate-id ] i)))

(defn get-plate-id []
  (c/get-at! props [:assets :session :plate-id ]))


(defn get-session-id ^Integer []
  (c/get-at! props [:assets :session :session-id ]))

;;(get-session-id)

(defn set-session-id [i]
  (c/with-write-transaction [props tx]
    (c/assoc-at tx  [:assets :session :session-id] i)))
  
(defn get-home-dir []
   (java.lang.System/getProperty "user.home"))
  
(defn get-temp-dir []
   (java.lang.System/getProperty "java.io.tmpdir"))
             
  
(defn get-working-dir []
   (java.lang.System/getProperty "user.dir"))

