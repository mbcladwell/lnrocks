(ns ln.dialog
  (:use [seesaw core table dev mig border])

  (:require [clojure.java.io :as io]
            [clojure.string]
             [ln.codax-manager :as cm])  
  (:import [javax.swing JFileChooser JEditorPane JFrame JScrollPane BorderFactory AbstractButton]
           java.awt.Font java.awt.Toolkit )
  (:import [java.net.URL])
  (:gen-class))

(def p (promise))

(defn login-dialog
  ;;
  []
  (-> (let [ name-input (text :columns 30 :id :nameid )
            pass-input (text :columns 30 :id :passid)]        
        (frame :title "Login to LIMS*Nucleus"
               ;;do not on exit close or you will kill repl
               :size [500 :by 240]
               :content  (mig-panel
                          :constraints ["wrap 4"]
                          :items [ [(label :text "Name: "
                                           :h-text-position :right) ]
                                  [ name-input "span 2" ]
                                  [ "           " ]
                                  [ "Password:" ]
                                  [ pass-input "span 2"]
                                  [ "           " ]
                                  [ "           " ]
                                  [ "           " ]
                                  [(checkbox :text "Save for future auto-login?" :id :cbox :selected? false) "span 2"]
                                  [ "           " ]
                                  
                                  [(button :text "Login"
                                           :listen [:mouse-clicked
                                                    (fn [e] (deliver p
                                                               (cm/set-u-p-al (config (select (to-root e)  [:#nameid]) :text)
                                                                              (config  (select (to-root e)  [:#passid]) :text)
                                                                              (config  (select (to-root e)  [:#cbox]) :selected?)))
                                                               (hide! (to-root e)) )])]
                                  [(button :text "Cancel"
                                           :listen [:mouse-clicked (fn [e] (hide! (to-root e)))] )]]))) 
      pack!
      show! (move! :to [ ( - ( / (.getWidth (.getScreenSize (Toolkit/getDefaultToolkit))) 2) 320),
                                ( - ( / (.getHeight (.getScreenSize (Toolkit/getDefaultToolkit))) 2) 240) ]  ) ))
