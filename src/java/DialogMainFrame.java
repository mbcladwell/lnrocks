
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
//    private DatabaseManager dbm;
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

//      public PlateSetPanel getPlateSetPanel() {
 //   return plate_set_card;
 // }


  public void showProjectTable() {
      IFn getAllProjects = Clojure.var("lnrocks.core", "get-all-projects");
      
      project_card = new ProjectPanel(this, (CustomTable)getAllProjects.invoke());
      cards.add(project_card, "ProjectPanel");
      card_layout.show(cards, "ProjectPanel");
  }

    
 public void showPlateSetTable(String _project_sys_name) {
     int project_id = Integer.parseInt(_project_sys_name.substring(4));
      IFn getPlateSetsForProject = Clojure.var("lnrocks.core", "get-plate-sets-for-project");
      LOGGER.info("prj-id: " + project_id);
      plate_set_card = new PlateSetPanel(this, (CustomTable)getPlateSetsForProject.invoke(project_id),  _project_sys_name);
    
    cards.add(plate_set_card, "PlateSetPanel");
    card_layout.show(cards, "PlateSetPanel");

 }


    public void showPlateTable(String _plate_set_sys_name) {
      
     int plate_set_id = Integer.parseInt(_plate_set_sys_name.substring(3));
      
   //   plate_card = new PlatePanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(plate_set_id, DialogMainFrame.PLATE), _plate_set_sys_name);
    
   // cards.add(plate_card, "PlatePanel");
   // card_layout.show(cards, "PlatePanel");
 }

  //    public void showAllPlatesTable(String _project_sys_name) {
      
    //  int project_id = Integer.parseInt(_project_sys_name.substring(4));
      
  //    all_plates_card = new AllPlatesPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLPLATES), _project_sys_name);
    
  //  cards.add(all_plates_card, "AllPlatesPanel");
  //  card_layout.show(cards, "AllPlatesPanel");
 // }


  // public void showWellTable(String _plate_sys_name) {
  //     int plate_id = Integer.parseInt(_plate_sys_name.substring(4));
  //     System.out.println(plate_id);
  //     well_card = new WellPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(plate_id, DialogMainFrame.WELL));
  //     cards.add(well_card, "Well");
  //     card_layout.show(cards, "Well");
  // }

  //     public void showAllWellsTable(String _project_sys_name) {
  //     int project_id = Integer.parseInt(_project_sys_name.substring(4));
      
  //     all_wells_card = new AllWellsPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLWELLS), _project_sys_name);
  //     cards.add(all_wells_card, "AllWells");
  //     card_layout.show(cards, "AllWells");
  // }

    /**
     * The "flip" methods are used with the up button to return to a previous card
     */

  public void flipToProjectTable() {
    card_layout.show(cards, "ProjectPanel");
  }
    
  //   public void flipToPlateSet() {
  //   card_layout.show(cards, "PlateSetPanel");
  // }


  // public void flipToPlate() {
  //   card_layout.show(cards, "PlatePanel");
  // }
    
  // public void flipToWell() {
  //   card_layout.show(cards, "Well");
  // }

      
  // public DatabaseManager getDatabaseManager() {
  //   return this.dbm;
  // }
    

    
  public Utilities getUtilities() {
    return utils;
  }

    public void setMainFrameTitle(String s){
	if(s==""){this.setTitle("LIMS*Nucleus");}else{
	    this.setTitle("LIMS*Nucleus::" + s);}
    }
  public void updateProjectPanel() {
      
  }

    public DefaultTableModel buildTableModel(Map<String, PersistentVector> hm) {
	 
	PersistentVector colnames = hm.get(":colnames");
        PersistentVector predata = hm.get(":data");
	int columnCount = colnames.count();
	
	Vector<String> columnNames = new Vector<String>();

	for (int column = 0; column < columnCount; column++) {
	  //	  System.out.println((colnames.get(column)).toString());
	  columnNames.addElement(colnames.get(column).toString());
      }
    //   // data of the table
      Vector<Vector<Object>> data = new Vector<Vector<Object>>();
 	 int rowCount = predata.count();
      for (int row = 0; row < rowCount; row++) {
   
        Vector<Object> vector = new Vector<Object>();

         for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
	     vector.add(((PersistentVector)predata.get(row)).get(columnIndex).toString());
	       //vector.add(rs.getObject(columnIndex));
        }
         data.add(vector);
      }
      // LOGGER.info("data: " + data);
       return new DefaultTableModel(data, columnNames);

    //   //          data.stream().map(List::toArray).toArray(Object[][]::new), columnNames);

  }

}
