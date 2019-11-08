(ns ln.codax-manager
  (:require [crux.api :as crux]
            [clojure.java.io :as io])
  (:gen-class ))

  (import (crux.api ICruxAPI))


(defn read-props-text-file []
  (read-string (slurp "limsnucleus.properties")))

;;(read-props-text-file)


(defn create-ln-props-from-text []
 (let [props (c/open-database! "ln-props")]
  (c/with-write-transaction [props tx]
    (-> tx
  (c/assoc-at [:assets ] (read-props-text-file))
  (c/assoc-at [:assets :session] {:project-id 1
	                          :project-sys-name ""
	                          :user-id 1
                                  :user-name ""
                                  :plateset-id 1
                                  :plateset-sys-name ""
	                          :user-group-id 1
                                  :user-group ""
	                          :session-id 1
                                  :working-dir ""
                                  :authenticated false
                                  })))
 (c/close-database! props)))

(defn set-props-to-elephantsql []
 (let [props (c/open-database! "ln-props")]
  (c/with-write-transaction [props tx]
    (-> tx
        (c/assoc-at [:assets :conn] {:source "test"
                                     :dbtype "postgresql"
  	                             :dbname "klohymim"
 	                             :host  "raja.db.elephantsql.com"
  	                             :port  5432
  	                             :user  "klohymim"
  	                             :password  "hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_"
 	                             :sslmode  false
                                     :auto-login true
 	                             :help-url-prefix  "http://labsolns.com/software/" 
                                     })
        (c/assoc-at [:assets :session] {:project-id 1
	                                :project-sys-name "PRJ-1"
	                                :user-id 3
                                        :user-name "klohymim"
                                        :plateset-id 1
                                        :plateset-sys-name ""
	                                :user-group-id 2
                                        :user-group "user"
	                                :session-id nil
                                        :working-dir ""
                                        :authenticated true
                                        })))
  (c/close-database! props)))

        ;;psql postgres://klohymim:hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_@raja.db.elephantsql.com:5432/klohymim

;;(set-props-to-elephantsql)

(defn set-props-to-hostgator []
 (let [props (c/open-database! "ln-props")]
  (c/with-write-transaction [props tx]
    (-> tx
        (c/assoc-at [:assets :conn] {:source "test"
                                     :dbtype "mysql"
  	                             :dbname "plapan_lndb"
 	                             :host  "192.254.187.215"
  	                             :port  3306
  	                             :db-user  "plapan_ln_admin"
  	                             :db-password  "welcome"
  	                             :user  "ln_admin"
  	                             :password  "welcome"
 	                             :sslmode  false
                                     :auto-login true
 	                             :help-url-prefix  "http://labsolns.com/software/" 
                                     }) 
        (c/assoc-at [:assets :session] {:project-id 1
	                                :project-sys-name "PRJ-1"
	                                :user-id 3
                                        :user-name "plapan_ln_admin"
                                        :plateset-id 1
                                        :plateset-sys-name ""
	                                :user-group-id 2
                                        :user-group "administrator"
	                                :session-id 1
                                        :working-dir ""
                                        :authenticated true
                                        })))
  (c/close-database! props)))

;;(set-props-to-hostgator)

(defn open-or-create-props
  ;;1. check working directory - /home/user/my-working-dir
  ;;2. check home directory      /home/user
  []
  (c/close-all-databases!)
  (if (.exists (io/as-file "ln-props"))
    (def props (c/open-database! "ln-props"))  
    (if (.exists (io/as-file (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (def props (c/open-database! (str (java.lang.System/getProperty "user.home") "/ln-props") ))
      (if (.exists (io/as-file (str (java.lang.System/getProperty "user.dir") "/limsnucleus.properties") ))
        (do
          (create-ln-props-from-text) 
          (def props (c/open-database! "ln-props")))
        
        (do            ;;no limsnucleus.properties - login to mysql
          (set-props-to-hostgator)
          (def props (c/open-database! "ln-props")) ;;end of user.dir if
          (JOptionPane/showMessageDialog nil "limsnucleus.properties file is missing\nLogging in to example database!"  ))

    ))))


(open-or-create-props)



;;(set-props-to-hostgator)


;;(create-ln-props-from-text)
;;(open-or-create-props)
;;(print-ap)
;;(c/close-database! props)
;;


(defn set-user [u]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx [:assets :conn :ln-user] u)))

(defn set-db-user [u]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx [:assets :conn :db-user] u)))


(defn set-init [b]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx [:assets :conn :init] b)))


(defn set-password [p]
  (c/with-write-transaction [props tx]
        (c/assoc-at! tx  [:assets :conn :ln-password] p)))

(defn set-db-password [p]
  (c/with-write-transaction [props tx]
        (c/assoc-at! tx  [:assets :conn :db-password] p)))

(defn get-dbtype
  "mysql; postgresql"
  []
  (c/get-at! props [:assets :conn :dbtype]))

(defn get-host []
   (c/get-at! props [:assets :conn :host]))

(defn get-port []
  (c/get-at! props [:assets :conn :port]))

(defn get-source
"test: the example elephantsql cloud instance
  local: local laptop
  network: internal server
  heroku: heroku cloud instance
  elephantsql: elephantsql cloud instance
  aws: AWS cloud instance"
  []
  (c/get-at! props [:assets :conn :source]))

(defn get-dbname []
  (c/get-at! props [:assets :conn :dbname]))

(defn get-ln-user []
  (c/get-at! props [:assets :conn :ln-user]))

(defn get-ln-password []
  (c/get-at! props [:assets :conn :ln-password]))

(defn get-sslmode []
  (c/get-at! props [:assets :conn :sslmode]))

(defn get-auto-login []
  (c/get-at! props [:assets :conn :auto-login]))

(defn set-auto-login [b]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :conn :auto-login] b)))


(defn set-u-p-al
  ;;user name, password, auto-login
  ;;only used during login
  [u p al]
  (c/with-write-transaction [props tx]
    (-> tx
     (c/assoc-at   [:assets :conn :ln-user] u)
     (c/assoc-at  [:assets :conn :ln-password] p) 
     (c/assoc-at  [:assets :conn :auto-login] al))))

;;(set-u-p-al "ln_admin" "welcome" true)
;;(str props)
;;(print-ap)
;;(open-or-create-props)
;;(c/close-database! props)
;;(c/destroy-database! props)


(defn set-uid-ugid-ug-auth [ uid ugid ug auth ]
  ;; user-id user-group-id user-group-name authenticated
  (c/with-write-transaction [props tx]
    (-> tx
      (c/assoc-at  [:assets :session :user-id] uid)   
      (c/assoc-at  [:assets :session :user-group-id] ugid)
      (c/assoc-at  [:assets :session :user-group] ug)
      (c/assoc-at  [:assets :session :authenticated] auth))))

(defn set-authenticated [b]
  (c/with-write-transaction [props tx]
    (c/assoc-at tx  [:assets :session :authenticated] b)))

(defn get-authenticated []
  (c/get-at! props [:assets :session :authenticated ]))

(defn get-all-props
  ;;note that the keys must be quoted for java
  []
  (into {} (java.util.HashMap.
            {        ":dbtype" (c/get-at! props [:assets :conn :dbtype])
             ":host" (c/get-at! props [:assets :conn :host])
            ":port" (c/get-at! props [:assets :conn :port])
           ":sslmode" (c/get-at! props [:assets :conn :sslmode])
          ":source" (c/get-at! props [:assets :conn :source])
          ":dbname" (c/get-at! props [:assets :conn :dbname])
             ":help-url-prefix" (c/get-at! props [:assets :conn :help-url-prefix])
             ":ln-password" (c/get-at! props [:assets :conn :ln-password])
             ":ln-user" (c/get-at! props [:assets :conn :ln-user])           
          ":db-password" (c/get-at! props [:assets :conn :db-password])
          ":db-user" (c/get-at! props [:assets :conn :db-user])})))

(defn get-all-props-clj
  ;;a map for clojure
  [] 
  ({:dbtype (c/get-at! props [:assets :conn :dbtype])
    :host (c/get-at! props [:assets :conn :host])
    :port (c/get-at! props [:assets :conn :port])
    :sslmode (c/get-at! props [:assets :conn :sslmode])
    :source (c/get-at! props [:assets :conn :source])
    :dbname (c/get-at! props [:assets :conn :dbname])
    :help-url-prefix (c/get-at! props [:assets :conn :help-url-prefix])
    :db-password (c/get-at! props [:assets :conn :db-password])
    :db-user (c/get-at! props [:assets :conn :db-user])
    :ln-password (c/get-at! props [:assets :conn :ln-password])
    :ln-user (c/get-at! props [:assets :conn :ln-user])
    }))




  (defn print-ap 
    "This version prints everything"
    []
    (println (str "Whole map: " (c/get-at! props []) )))

  ;;(print-ap)
  ;;(print-all-props)

(defn set-dbtype [b]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :conn :dbtype] b)))

(defn set-user-id [i]
  (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :user-id] i)))

;;(set-user-id 100)

(defn get-user-id []
  (c/get-at! props [:assets :session :user-id ]))

(defn get-init []
  (c/get-at! props [:assets :conn :init ]))

(defn get-db-user []
  (c/get-at! props [:assets :conn :db-user ]))

(defn get-db-password []
  (c/get-at! props [:assets :conn :db-password ]))

(defn get-user []
  (c/get-at! props [:assets :conn :ln-user ]))

(defn get-password []
  (c/get-at! props [:assets :conn :ln-password ]))


(defn set-user-group [i]
    (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :user-group] i)))

(defn get-user-group []
  (c/get-at! props [:assets :session :user-group ]))

;;(get-user-group)
(defn get-user-group-id ^Integer []
  (c/get-at! props [:assets :session :user-group-id ]))

(defn set-user-group-id [i]
    (c/with-write-transaction [props tx]
        (c/assoc-at tx  [:assets :session :user-group-id] i)))


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


(def conn   {:dbtype (get-dbtype)
             :dbname (get-dbname)
             :host (get-host)
             :db-user (get-db-user)
             :db-password (get-db-password)
             :user (get-db-user)
             :password (get-db-password)
             :port (get-port)
             :useTimezone true
             :serverTimezone "UTC"
             :useSSL (get-sslmode)})

(def conn-create   {:dbtype (get-dbtype)
                    :host (get-host)
                    :dbname nil
             :user (get-user)
             :password (get-password)
             :port (get-port)
             :useTimezone true
             :serverTimezone "UTC"
             :useSSL (get-sslmode)})


;;(println conn)


;; (def conn-admin   {:dbtype (get-dbtype)
;;                    :dbname (get-dbname)
;;                    :host (get-host)
;;                    :user (if (= (get-source) "test") "klohymim" "plapan_ln_admin")
;;                    :password (if (= (get-source) "test") "hwc3v4_rbkT-1EL2KI-JBaqFq0thCXM_" "welcome")
;;                    :port (get-port)
;;                    :ssl (get-sslmode)})

;;(println conn)

(defn update-ln-props [host port dbname source sslmode db-user db-password user password help-url user-dir]
  (c/with-write-transaction [props tx]
    (-> tx
      (c/assoc-at  [:assets :conn :host] host)   
      (c/assoc-at  [:assets :conn :port] port)
      (c/assoc-at  [:assets :conn :sslmode] sslmode)
      (c/assoc-at  [:assets :conn :source] source)
      (c/assoc-at  [:assets :conn :db-user] db-user)   
      (c/assoc-at  [:assets :conn :db-password] db-password)
      (c/assoc-at  [:assets :conn :user] user)   
      (c/assoc-at  [:assets :conn :password] password)
      (c/assoc-at  [:assets :conn :help-url-prefix] help-url)
      (c/assoc-at  [:assets :conn :dbname] dbname))))


 
    (defn get-help-url-prefix []
          (c/get-at! props [:assets :conn :help-url-prefix ]))
;;(get-help-url-prefix)

(defn  get-connection-string [target]	  
  (case target
    "heroku" (str "jdbc:postgresql://"  (get-host) ":" (get-port)  "/" (get-dbname) "?sslmode=require&user=" (get-db-user) "&password="  (get-db-password))
    "local" (str "jdbc:mysql://localhost:3306/lndb?user=" (get-user) "&password=" (get-password) "&useSSL=false&useTimezone=true&serverTimezone=UTC")	   
    "elephantsql" (str "jdbc:postgresql://" (get-host) ":" (get-port) "/" (get-dbname) "?user=" (get-db-user) "&password=" (get-db-password) "&SSL=" (get-sslmode))
     "mysql" (str "jdbc:mysql://" (get-host) ":"  (get-port) "/" (get-dbname) "?useTimezone=true&serverTimezone=UTC&user=" (get-db-user) "&password=" (get-db-password) "&SSL=" (get-sslmode)  )
     "hostgator" (str "jdbc:mysql://" (get-host) ":"  (get-port) "/" (get-dbname) "?user=" (get-db-user) "&password=" (get-db-password) "&SSL=" (get-sslmode)  )
     "test" (str "jdbc:mysql://" (get-host) ":" (get-port) "/" (get-dbname)  "?useTimezone=true&serverTimezone=UTC&user=" (get-db-user) "&password=" (get-db-password) "&SSL=" (get-sslmode))
   "postgres" (str "jdbc:postgresql://" (get-host) ":" (get-port) "/" (get-dbname)"?user="  (get-db-user) "&password=" (get-db-password) "&SSL="  (get-sslmode))))

;;(get-connection-string "mysql")
;;(def mysql-conn (get-connection-string "mysql"))

(defn pretty-print []
  (do
    (println "All values")
    (println "-------------------")
    (println "conn")
    (println (str ":init       " (get-init)))
    (println (str ":auto-login " (get-auto-login)))
    (println (str ":dbtype     " (get-dbtype)))
    (println (str ":dbname     " (get-dbname)))
    (println (str ":host       " (get-host)))
    (println (str ":db-user    " (get-db-user)))
    (println (str ":db-password" (get-db-password)))
    (println (str ":user    " (get-user)))
    (println (str ":password   " (get-password)))
    (println (str ":source     " (get-source)))
    (println (str ":sslmode    " (get-sslmode)))
    (println (str ":help-url-prefix    " (get-help-url-prefix)))
    (println "-------------------")
    (println "session")
    (println (str ":plateset-id       " (get-plate-set-id)))
    (println (str ":session-id        " (get-session-id)))
    (println (str ":user-group        " (get-user-group)))
    (println (str ":authenticated     " (get-authenticated)))
    (println (str ":plateset-sys-name " (get-plate-set-sys-name)))
    (println (str ":project-id        " (get-project-id)))
    (println (str ":user-id           " (get-user-id)))
    (println (str ":user-group-id     " (get-user-group-id)))
    (println "-------------------------")
    
    ))


	
(defn look [] 
    (def props (c/open-database! "ln-props"))
   
    (pretty-print)
    (println "*****")(println "*****")(println "*****")
    (c/close-database! props))

;;(look)

;;(c/close-database! props)
;;(set-props-to-hostgator)
;;(set-user-group "administrator")
;;(open-or-create-props)
;;(c/close-database! props)
;;(c/destroy-database! props)
;;(println props)
;;(get-session-id)
;;(set-session-id 1)
;;(get-user-id)

