
package lnrocks;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.util.logging.Logger;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.LazySeq;

public class DialogMainFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private JPanel cards; // a panel that uses CardLayout
    private CardLayout card_layout;

    private ProjectPanel project_card;
    private PlateSetPanel plate_set_card;
    private PlatePanel plate_card; //plates in PS
    private AllPlatesPanel all_plates_card; //plates in Project
    private WellPanel well_card;
    private AllWellsPanel all_wells_card;
    private PersistentVector colnames;
    private PersistentVector data;
    
  private static Utilities utils;
//    
    //private DatabaseRetriever dbr;
    // private static Session session;
  private  IFn require = Clojure.var("clojure.core", "require");
 
 
  private Long sessionID;

    public static final int PROJECT = 1; //Card with projects
    public static final int PLATESET = 2; //Card with plate sets
    public static final int PLATE = 3; //Card with plates
    public static final int WELL = 4; //Card with wells
    public static final int ALLPLATES = 5; //Card with plates but all plates for project
    public static final int ALLWELLS = 6; //Card with plates but all plates for project

 
  public DialogMainFrame() {
      // session = _s;
  //    dbm = _dbm;
      utils = new Utilities(this);
      this.setTitle("LIMS*Nucleus");
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      ImageIcon img = new ImageIcon(this.getClass().getResource("/images/mwplate.png"));
      this.setIconImage(img.getImage());
      //dbr = session.getDatabaseRetriever();
      require.invoke(Clojure.read("lnrocks.core"));
      IFn getAllProjects = Clojure.var("lnrocks.core", "get-all-projects");

    /////////////////////////////////////////////
    // set up the project table
   
    cards = new JPanel(new CardLayout());
    card_layout = (CardLayout) cards.getLayout();
    
    CustomTable projectTable = new CustomTable(this, buildTableModel((Map)getAllProjects.invoke()));
    project_card = new ProjectPanel(this, projectTable);
    cards.add(project_card, "ProjectPanel");

    /////////////////////////////////////////////////////////////////
  //   System.out.println("");
  //   System.out.println("");
  //   System.out.println("");
  //   System.out.println("==========test code===========");
  //   System.out.println("");

  //      IFn testLzSeq = Clojure.var("lnrocks.core", "test-lz-seq");   
  //      HashMap<String, PersistentVector> hm = (HashMap<String, PersistentVector>)testLzSeq.invoke();
  //      System.out.println(hm.get(":data"));
  //     System.out.println(hm.get(":colnames"));
      
  //   System.out.println("");
  //    System.out.println("===================================");
    

  //   // IFn getPlateSetsForProject = Clojure.var("lnrocks.core", "get-plate-sets-for-project");   
  // //    Map<String, PersistentVector> hm = (Map)getPlateSetsForProject.invoke(1);
     
  // //    for (Map.Entry<String, PersistentVector> e : hm.entrySet()){
	 
  // // 	 System.out.println(e.getKey());
  // // 	 System.out.println((e.getValue()).chunkedSeq());
  // //    }

  //    //			    + ": " + e.getValue().toArray());}


  //    System.out.println("===================================");
  //   System.out.println("");
  //   System.out.println("");
    
/////////////////////////////////////////////////////////////////
    
    this.getContentPane().add(cards, BorderLayout.CENTER);

    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

    
  public ProjectPanel getProjectPanel() {
    return project_card;
  }

    public PlateSetPanel getPlateSetPanel() {
	return plate_set_card;
    }


  public void showProjectTable() {
      IFn getAllProjects = Clojure.var("lnrocks.core", "get-all-projects");
      
      CustomTable projectTable = new CustomTable(this, buildTableModel((Map)getAllProjects.invoke()));
      project_card = new ProjectPanel(this, projectTable);   
      
      //   project_card = new ProjectPanel(this, (CustomTable)getAllProjects.invoke());
      cards.add(project_card, "ProjectPanel");
      card_layout.show(cards, "ProjectPanel");
  }

    
 public void showPlateSetTable(String _project_sys_name) {
     int project_id = Integer.parseInt(_project_sys_name.substring(4));
     IFn getPlateSetsForProject = Clojure.var("lnrocks.core", "get-plate-sets-for-project");
     LOGGER.info("prj-id: " + project_id);

     Map<String, PersistentVector> hm = (Map)getPlateSetsForProject.invoke(project_id);
     
     for (Map.Entry<String, PersistentVector> e : hm.entrySet()){
	 System.out.println(e.getKey() + ": " + e.getValue().toArray());}


     CustomTable plateSetTable = new CustomTable(this, buildTableModel(hm));
     
      //    CustomTable plateSetTable = new CustomTable(this, buildTableModel((Map)getPlateSetsForProject.invoke(project_id)));
      plate_set_card = new PlateSetPanel(this, plateSetTable,  _project_sys_name);

      cards.add(plate_set_card, "PlateSetPanel");
      card_layout.show(cards, "PlateSetPanel");

 }

    public void showPlateTable(String _plate_set_sys_name) {
      
	int plate_set_id = Integer.parseInt(_plate_set_sys_name.substring(3));

     IFn getPlatesForPlateSetID = Clojure.var("lnrocks.core", "get-plates-for-plate-set-id");
     CustomTable plateTable = new CustomTable(this, buildTableModel((Map)getPlatesForPlateSetID.invoke(plate_set_id)));
     plate_card = new PlatePanel(this, plateTable,  _plate_set_sys_name);

   cards.add(plate_card, "PlatePanel");
   card_layout.show(cards, "PlatePanel");
 }

    public void showAllPlatesTable(String _project_sys_name) {
      
	int project_id = Integer.parseInt(_project_sys_name.substring(4));
	IFn getAllPlatesForProject = Clojure.var("lnrocks.core", "get-all-plates-for-project");
	CustomTable allPlatesTable = new CustomTable(this, buildTableModel((Map)getAllPlatesForProject.invoke(project_id)));
	all_plates_card = new AllPlatesPanel(this, allPlatesTable,  _project_sys_name);
	cards.add(all_plates_card, "AllPlatesPanel");
	card_layout.show(cards, "AllPlatesPanel");
 }


  public void showWellTable(String _plate_sys_name) {
      int plate_id = Integer.parseInt(_plate_sys_name.substring(4));
      System.out.println(plate_id);

      	IFn getWellsForPlateID = Clojure.var("lnrocks.core", "get-wells-for-plate-id");
	CustomTable wellTable = new CustomTable(this, buildTableModel((Map)getWellsForPlateID.invoke(plate_id)));
	well_card = new WellPanel(this, wellTable);


	//well_card = new WellPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(plate_id, DialogMainFrame.WELL));
      cards.add(well_card, "Well");
      card_layout.show(cards, "Well");
  }

      public void showAllWellsTable(String _project_sys_name) {
      int project_id = Integer.parseInt(_project_sys_name.substring(4));

      	IFn getAllWellsForProject = Clojure.var("lnrocks.core", "get-all-wells");
	CustomTable allWellsTable = new CustomTable(this, buildTableModel((Map)getAllWellsForProject.invoke(project_id)));
	all_wells_card = new AllWellsPanel(this, allWellsTable,  _project_sys_name);
     
	//all_wells_card = new AllWellsPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLWELLS), _project_sys_name);
      cards.add(all_wells_card, "AllWells");
      card_layout.show(cards, "AllWells");
  }

    /**
     * The "flip" methods are used with the up button to return to a previous card
     */

  public void flipToProjectTable() {
    card_layout.show(cards, "ProjectPanel");
  }
    
     public void flipToPlateSet() {
     card_layout.show(cards, "PlateSetPanel");
   }


  public void flipToPlate() {
    card_layout.show(cards, "PlatePanel");
  }
    
  public void flipToWell() {
    card_layout.show(cards, "Well");
  }

      
  
  public Utilities getUtilities() {
    return utils;
  }

    public void setMainFrameTitle(String s){
	if(s==""){this.setTitle("LIMS*Nucleus");}else{
	    this.setTitle("LIMS*Nucleus::" + s);}
    }
  public void updateProjectPanel() {
      
  }

  //   public DefaultTableModel buildTableModel(Map<String, PersistentVector> hm) {
  // 	//System.out.println("hm.getData: " + hm.get(":data"));
	 
  // 	PersistentVector colnames = hm.get(":colnames");
  //       clojure.lang.LazySeq predata = hm.get(":data");
  //       //clojure.lang.LazySeq predata = hm.get(":data");
  // 	int columnCount = colnames.count();
	
  // 	Vector<String> columnNames = new Vector<String>();

  // 	for (int column = 0; column < columnCount; column++) {
  // 	  	  System.out.println((colnames.get(column)).toString());
  // 	  columnNames.addElement(colnames.get(column).toString());
  //     }
  //   //   // data of the table
  // 	Vector<Vector<Object>> data = new Vector<Vector<Object>>();
  // 	int rowCount = predata.count();
  // 	for (int row = 0; row < rowCount; row++) {
   
  // 	    Vector<Object> vector = new Vector<Object>();
	    
  // 	    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
  // 	  	System.out.println(((PersistentVector)predata.get(row)).get(columnIndex).toString());
  // 		vector.add(((PersistentVector)predata.get(row)).get(columnIndex).toString());
  // 		//vector.add(rs.getObject(columnIndex));
  // 	    }
  // 	    data.add(vector);
  // 	}
  // 	LOGGER.info("data: " + data);
  // 	return new DefaultTableModel(data, columnNames);
	
  //   //   //          data.stream().map(List::toArray).toArray(Object[][]::new), columnNames);

  // }


    public DefaultTableModel buildTableModel(Map<String, PersistentVector> hm) {
	 
	clojure.lang.PersistentVector colnames = hm.get(":colnames");
        clojure.lang.PersistentVector predata = hm.get(":data");
      	int columnCount = colnames.count();
	
	Vector<String> columnNames = new Vector<String>();

	for (int column = 0; column < columnCount; column++) {
	    //    System.out.println((colnames.get(column)).toString());
	  columnNames.addElement(colnames.get(column).toString());
      }
    //   // data of the table
	Vector<Vector<Object>> data = new Vector<Vector<Object>>();
	int rowCount = predata.count();
	for (int row = 0; row < rowCount; row++) {
	    Vector<Object> vector = new Vector<Object>();
	    
	    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
		//	System.out.println(((PersistentVector)predata.get(row)).get(columnIndex).toString());
		vector.add(((PersistentVector)predata.get(row)).get(columnIndex).toString());
		//vector.add(rs.getObject(columnIndex));
	    }
	    data.add(vector);
	}
	//LOGGER.info("data: " + data);
	return new DefaultTableModel(data, columnNames);
	
    //   //          data.stream().map(List::toArray).toArray(Object[][]::new), columnNames);

  }


    
}
