(ns lnrocks.core 
  (:gen-class))

;;https://juxt.pro/blog/posts/crux-tutorial-datalog.html
 (require '[crux.api :as crux])
(import (crux.api ICruxAPI))



(defn counter
  ;;entity:  plate, plate-set, sample or project
  ;;need: how many will be created
  ;;returns vector of start and end id
  [ entity need ]
  (let [old (crux/entity (crux/db node) :counters)
        start (+ 1 (entity old))
        end (+ start (- need 1))
        new (assoc old entity end)]
    (crux/submit-tx node [[:crux.tx/cas old new]])
    {:start start :end end}))



(defn new-project
  [ name description session-id]
  (let [ prj-id (:start (counter :project 1))
        doc  {:crux.db/id (keyword (str "prj-" prj-id))
                :name name
                :description description
                :session session-id}]
    (crux/submit-tx node [[:crux.tx/put doc]] )
  prj-id))

(new-project "MyNewProj" "a test of function" 1)
(crux/entity (crux/db node) :prj-69)

(defn easy-ingest
  "Uses Crux put transaction to add a vector of documents to a specified
  node"
  [node docs]
  (crux/submit-tx node
                  (vec (for [doc docs]
                         [:crux.tx/put doc]))))

(def new-docs [
               {:crux.db/id :prj-1
                :name "With AR, HL"
                :description "3 plate sets with 2 96 well plates each"
                :session 1}
               {:crux.db/id :prj-2
                :name "1 plate set with 2 384 well plates each"
                :description "With AR"
                :session 1}
               {:crux.db/id :prj-3
                :name "1 plate set with 1 1536 well plate"
                :description "With AR"
                :session 1}
               {:crux.db/id :prj-4
                :name "Test Project 4"
                :description "Description 4"
                :session 1}
               {:crux.db/id :prj-5
                :name "Test Project 5"
                :description "Description 5"
                :session 1}
               {:crux.db/id :prj-6
                :name "Test Project 6"
                :description "Description 6"
                :session 1}
               {:crux.db/id :prj-7
                :name "Test Project 7"
                :description "Description 7"
                :session 1}
               {:crux.db/id :prj-8
                :name "Test Project 8"
                :description "Description 8"
                :session 1}
               {:crux.db/id :prj-9
                :name "Test Project 9"
                :description "Description 9"
                :session 1}
               {:crux.db/id :prj-10
                :name "Plates only, no data"
                :description "2 plate sets with 10 96 well plates each"
                :session 1}  ])

(easy-ingest node new-docs)

(defn new-plate-set
  ;;with-samples: boolean
  [ plate-set-name description num-plates plate-format-id plate-type-id
   project-id plate-layout-name-id lnsession-id with-samples] 
  (let [ps-id (:start (counter :plate-set 1))
        plt-ids (counter :plate-set num-plates)
        start (:start plt-ids)
        end (:end plt-ids)
        ps-doc {:crux.db/id (keyword (str "ps-" ps-id))
                :name plate-set-name
                :description description
                :num-plates num-plates
                :plate-format-id plate-format-id
                :plate-type-id plate-type-id
                :project-id project-id
                :plate-layout-name-id plate-layout-name-id
                :session lnsession-id}
        ]
     (crux/submit-tx node [[:crux.tx/put doc]] )
     
    )
  )

(defn new-plate
  ;;with-samples: boolean
  [ project-id plate-set-id plate-id plate-format-id plate-type-id with-samples] 
  (let [ plt-doc {:crux.db/id (keyword (str "plt-" plate-id))
                  :plate-format-id plate-format-id
                  :plate-type-id plate-type-id
                  :project-id project-id}]
    (crux/submit-tx node [[:crux.tx/put plt-doc]] )
     
    )
  )



(counter :plate-set 1)




(crux/q (crux/db crux)
        '{:find [element]
          :where [[element :type :element/metal]]} )



(=
 (crux/q (crux/db crux)
         '{:find [element]
           :where [[element :type :element/metal]]} )

 (crux/q (crux/db crux)
         {:find '[element]
          :where '[[element :type :element/metal]]} )

 (crux/q (crux/db crux)
         (quote
          {:find [element]
           :where [[element :type :element/metal]]}) ))

(crux/q (crux/db crux)
        '{:find [name]
          :where [[e :type :element/metal]
                  [e :common-name name]]} )



(crux/q (crux/db crux)
        '{:find [name rho appearance]
          :where [[e :density rho]
                  [e :common-name name]
                  [e :appearance appearance]]})

(crux/q (crux/db crux)
        {:find '[name]
         :where '[[e :type t]
                  [e :common-name name]]
         :args [{'t :element/metal}]})

(defn filter-type
  [type]
  (crux/q (crux/db crux)
        {:find '[name]
         :where '[[e :type t]
                  [e :common-name name]]
         :args [{'t type}]}))

(defn filter-appearance
  [description]
  (crux/q (crux/db crux)
        {:find '[name IUPAC]
         :where '[[e :common-name name]
                  [e :IUPAC-name IUPAC]
                  [e :appearance appearance]]
         :args [{'appearance description}]}))


(filter-type :element/metal)

(crux/submit-tx
 crux
 [[:crux.tx/put
   (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES"])]])

(crux/submit-tx
 crux
 [[:crux.tx/put
   {:crux.db/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2114-12-03"]])

(crux/submit-tx
 crux
 [[:crux.tx/put 
   {:crux.db/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2113-12-03" ;; Valid time start
   #inst "2114-12-03"] ;; Valid time end

  [:crux.tx/put 
   {:crux.db/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Full}
   #inst "2112-12-03"
   #inst "2113-12-03"]

  [:crux.tx/put 
   {:crux.db/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? false}
   #inst "2112-06-03"
   #inst "2112-12-02"]

  [:crux.tx/put 
   {:crux.db/id :consumer/RJ29sUU
    :consumer-id :RJ29sUU
    :first-name "Jay"
    :last-name "Rose"
    :cover? true
    :cover-type :Promotional}
   #inst "2111-06-03"
   #inst "2112-06-03"]])


(crux/q (crux/db crux #inst "2115-07-03")
        '{:find [cover type]
          :where [[e :consumer-id :RJ29sUU]
                  [e :cover? cover]
                  [e :cover-type type]]})

(crux/submit-tx
 crux
 [[:crux.tx/put
   (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP"])]])

(defn stock-check
  [company-id item]
  {:result (crux/q (crux/db crux)
                   {:find '[name funds stock]
                    :where ['[e :company-name name]
                            '[e :credits funds]
                            ['e item 'stock]]
                    :args [{'e company-id}]})
   :item item})


(defn format-stock-check
  [{:keys [result item] :as stock-check}]
  (for [[name funds commod] result]
    (str "Name: " name ", Funds: " funds ", " item " " commod)))


(crux/submit-tx
 crux
 [[:crux.tx/cas
   ;; Old doc
   {:crux.db/id :blue-energy
    :seller? false
    :buyer? true
    :company-name "Blue Energy"
    :credits 1000}
   ;; New doc
   {:crux.db/id :blue-energy
    :seller? false
    :buyer? true
    :company-name "Blue Energy"
    :credits 900
    :units/CH4 10}]

  [:crux.tx/cas
   ;; Old doc
   {:crux.db/id :tombaugh-resources
    :company-name "Tombaugh Resources Ltd."
    :seller? true
    :buyer? false
    :units/Pu 50
    :units/N 3
    :units/CH4 92
    :credits 51}
   ;; New doc
   {:crux.db/id :tombaugh-resources
    :company-name "Tombaugh Resources Ltd."
    :seller? true
    :buyer? false
    :units/Pu 50
    :units/N 3
    :units/CH4 82
    :credits 151}]])


(format-stock-check (stock-check :tombaugh-resources :units/CH4))
 
  (format-stock-check (stock-check :blue-energy :units/CH4))


(crux/submit-tx
 crux
 [[:crux.tx/cas
   ;; Old doc
   {:crux.db/id :gold-harmony
    :company-name "Gold Harmony"
    :seller? true
    :buyer? false
    :units/Au 10211
    :credits 51}
   ;; New doc
   {:crux.db/id :gold-harmony
    :company-name "Gold Harmony"
    :seller? true
    :buyer? false
    :units/Au 211
    :credits 51}]

  [:crux.tx/cas
   ;; Old doc
   {:crux.db/id :encompass-trade
    :company-name "Encompass Trade"
    :seller? true
    :buyer? true
    :units/Au 10
    :units/Pu 5
    :units/CH4 211
    :credits 100002}
   ;; New doc
   {:crux.db/id :encompass-trade
    :company-name "Encompass Trade"
    :seller? true
    :buyer? true
    :units/Au 10010
    :units/Pu 5
    :units/CH4 211
    :credits 1002}]])

 (format-stock-check (stock-check :gold-harmony :units/Au))
  (format-stock-check (stock-check :encompass-trade :units/Au))

(crux/submit-tx
 crux
 [[:crux.tx/put
   (assoc manifest :badges ["SETUP" "PUT" "DATALOG-QUERIES" "BITEMP" "CAS" ])]])


 (crux/q (crux/db crux)
          {:find '[belongings]
           :where '[[e :cargo belongings]]
           :args [{'belongings "secret note"}]})



(crux/submit-tx crux
                [[:crux.tx/put {:crux.db/id :kaarlang/clients
                                :clients [:encompass-trade]}
                  #inst "2110-01-01T09"
                  #inst "2111-01-01T09"]

                 [:crux.tx/put {:crux.db/id :kaarlang/clients
                                :clients [:encompass-trade :blue-energy]}
                  #inst "2111-01-01T09"
                  #inst "2113-01-01T09"]

                 [:crux.tx/put {:crux.db/id :kaarlang/clients
                                :clients [:blue-energy]}
                  #inst "2113-01-01T09"
                  #inst "2114-01-01T09"]

                 [:crux.tx/put {:crux.db/id :kaarlang/clients
                                :clients [:blue-energy :gold-harmony :tombaugh-resources]}
                  #inst "2114-01-01T09"
                  #inst "2115-01-01T09"]])

(crux/history-ascending
 (crux/db crux)
 (crux/new-snapshot (crux/db crux #inst "2116-01-01T09")) 
 :kaarlang/clients)
;;;;;;;;;;;;;;;;plate manager begins here




(defn -main
  "I don't do a whole lot ... yet."
  [& args]
 

  (def ^crux.api.ICruxAPI node
    (crux/start-node
       {:crux.node/topology :crux.standalone/topology
        :crux.node/kv-store "crux.kv.rocksdb/kv"
        :crux.standalone/event-log-dir "data/eventlog-1"
        :crux.kv/db-dir "data/db-dir1"
        :crux.standalone/event-log-kv-store "crux.kv.rocksdb/kv"}))

  (println "RocksDB initialized!"))

(-main)
