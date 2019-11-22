(ns ln.db-retriever
  (:require [next.jdbc :as j]
            [next.jdbc.prepare :as p]
            [next.jdbc.result-set :as rs]
            [next.jdbc.protocols :as proto]
            [codax.core :as c]
            [clojure.set :as s]

           ;; [honeysql.core :as hsql]
           ;; [honeysql.helpers :refer :all :as helpers]
            [ln.codax-manager :as cm])
         ;;   [clojure.data.csv :as csv]
          ;;  [clojure.java.io :as io])            
  (:import [java.sql.DriverManager]
           [javax.swing.JOptionPane]))




(defn authenticate-user
  ;;variable from codax
  []
  (let [user (cm/get-user)
        password (cm/get-password)
        results (j/execute-one! cm/conn ["SELECT lnuser.id, lnuser.password, lnuser_groups.id, lnuser_groups.usergroup  FROM lnuser, lnuser_groups  WHERE lnuser_groups.id = lnuser.usergroup and lnuser_name = ?"  user ])]
        (println (str "user: " user))
        (println password)
        (println results)
    (if (= password (:lnuser/password results) )
      (do
        (println "before uid ugid ug auth")
        (println  ( :lnuser_groups/usergroup results))
        (cm/set-uid-ugid-ug-auth
           (:lnuser/id  results)          
           ( :lnuser_groups/id results)
           ( :lnuser_groups/usergroup results)
           true)
        (println "after uid ugid ug auth")
          
          (cm/set-authenticated true));;valid
    (cm/set-authenticated false);;invalid
    )))

;;(authenticate-user)
;;(cm/print-ap)
;;(println cm/pg-db)
;;(j/insert! cm/pg-db :lnsession {:lnuser_id  1 } )


(defn register-session
  ;;user id
  [ uid ]
  (let [
        user-id-pre (j/execute-one!  cm/conn  ["INSERT INTO lnsession(lnuser_id) values(?)" uid] )
        user-id (first(vals  user-id-pre))
        ug-id-pre (j/execute-one!  cm/conn ["SELECT usergroup_id FROM lnuser WHERE lnuser.id =?" uid ] )
        ug-id (first (vals  ug-id-pre))
        ug-name-pre (j/execute-one!  cm/conn ["SELECT usergroup FROM lnuser_groups WHERE id =?" uid ] )
        ug-name (first (vals  ug-name-pre))
        ]
    (c/with-write-transaction [cm/props tx]
    (-> tx
      (c/assoc-at  [:assets :session :user-id] user-id)   
      (c/assoc-at  [:assets :session :user-group-id] ug-id)
      (c/assoc-at  [:assets :session :user-group] ug-name)))))
    
 
;;(register-session 1)


;;(get-num-samples-for-plate-set 1)


(defn get-all-data-for-assay-run
  "provides a map"
  [ assay-run-id ]
  (let [
        sql-statement "SELECT assay_run.assay_run_sys_name, plate_set.plate_set_sys_name , plate.plate_sys_name, plate_plate_set.plate_order, well_numbers.well_name, well_type.name, well.by_col, well.ID AS \"well_id\", assay_result.response, assay_result.bkgrnd_sub, assay_result.norm, assay_result.norm_pos, assay_result.p_enhance, plate_layout.target, well.ID AS \"well_id\", sample.sample_sys_name, sample.accs_id   FROM  plate_layout_name , plate_set, plate_plate_set, plate, well, assay_result, assay_run, well_numbers, plate_layout, well_type, well_sample, sample WHERE plate_plate_set.plate_set_id=plate_set.id AND plate_plate_set.plate_id=plate.ID and plate.id=well.plate_id  AND plate_set.ID = assay_run.plate_set_id AND assay_result.assay_run_id= assay_run.id AND assay_result.plate_order=plate_plate_set.plate_order AND assay_result.well=well.by_col AND assay_run.ID = ? AND well_numbers.plate_format= plate_layout_name.plate_format_id AND well_numbers.by_col=well.by_col AND plate_layout_name.ID= assay_run.plate_layout_name_id AND plate_layout.plate_layout_name_id= assay_run.plate_layout_name_id AND plate_layout.well_type_id=well_type.ID AND plate_layout.well_by_col=well.by_col AND well_sample.sample_id=sample.ID AND well_sample.well_id=well.ID AND well.ID IN (SELECT well.ID FROM  plate_plate_set, plate, well WHERE plate_plate_set.plate_id = plate.ID AND well.plate_id = plate.ID AND plate_plate_set.plate_set_id = assay_run.plate_set_id)"       
        a  (proto/-execute-all cm/conn [sql-statement assay-run-id ]{:label-fn rs/as-maps :builder-fn rs/as-maps} )
        b (s/project a [ :assay_run/assay_run_sys_name :plate_set/plate_set_sys_name :plate/plate_sys_name :plate_plate_set/plate_order  :well_numbers/well_name  :well_type/name  :well/by_col  :well/ID :assay_result/response :assay_result/bkgrnd_sub :assay_result/norm :assay_result/norm_pos :assay_result/p_enhance :plate_layout/target :sample/sample_sys_name :sample/accs_id ])
        c (map #(s/rename-keys % {:assay_run/assay_run_sys_name :assay_run_sys_name, :plate_set/plate_set_sys_name :plate_set_sys_name, :plate/plate_sys_name :plate_sys_name, :plate_plate_set/plate_order :plate_order, :well_numbers/well_name :well_name,  :well_type/name :well_type, :well/by_col :by_col,  :well/ID :well_id, :assay_result/response :response, :assay_result/bkgrnd_sub :bkgrnd_sub, :assay_result/norm :norm, :assay_result/norm_pos :norm_pos, :assay_result/p_enhance :p_enhance, :plate_layout/target :target, :sample/sample_sys_name :sample_sys_name, :sample/accs_id :accs_id}) b)
        d (sort-by (juxt :plate_order :by_col) c)
        ]
    d ))
