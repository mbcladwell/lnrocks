(ns ln.session
  (:require 
            ;;[honeysql.core :as hsql]
            ;;[honeysql.helpers :refer :all :as helpers]
            [clojure.data.csv :as csv]
            [ln.codax-manager :as cm]
            [clojure.java.browse :as browse]
           ;; [ln.db-inserter :as dbi]
            [ln.db-retriever :as dbr]
            [ln.db-inserter :as dbi]
            [ln.dialog :as d])
   (:import javax.swing.JOptionPane)
  (:gen-class ))

;;https://push-language.hampshire.edu/t/calling-clojure-code-from-java/865
;;(open-props-if-exists)

(defn login-to-database
  ;;if user is blank or auto-login is false, pop up the login dialog
  ;;store results, validate results, and start dbm
  []

  (if (cm/get-init)
  (ln.DialogPropertiesNotFound.(cm/get-all-props))
  (if(or (clojure.string/blank? (cm/get-user))
         (not (cm/get-auto-login)))
    (do
      (d/login-dialog)
      (loop [completed? (realized? d/p)]
      (if (eval completed?)
      (do
        (dbr/authenticate-user)
        (if(cm/get-authenticated)
          (do
            (dbr/register-session (cm/get-user-id))
            (ln.DatabaseManager.))
          (do
            (cm/set-auto-login false)
            (JOptionPane/showMessageDialog nil "Invalid login credentials!" ))))
      (recur  (realized? d/p)))));; the if is true i.e. need a login dialog
    (do
      (dbr/register-session (cm/get-user-id))
      (ln.DatabaseManager. )))))  ;;if is false - can auto-login



;;(cm/set-u-p-al "aaa" "bbb" false)

;;(login-to-database)

(defn open-help-page [s]
  (browse/browse-url (str (cm/get-help-url-prefix) s)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]    
  (login-to-database ))

;;(-main)



;;https://cb.codes/a-tutorial-of-stuart-sierras-component-for-clojure/

