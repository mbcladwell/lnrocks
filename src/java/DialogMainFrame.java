
package lnrocks;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

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

  private static Utilities utils;
    private DatabaseManager dbm;
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

 
  public DialogMainFrame(DatabaseManager _dbm ) throws SQLException {
      // session = _s;
      dbm = _dbm;
      require.invoke(Clojure.read("ln.codax-manager"));
      utils = new Utilities(this);
      this.setTitle("LIMS*Nucleus");
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      ImageIcon img = new ImageIcon(this.getClass().getResource("/images/mwplate.png"));
      this.setIconImage(img.getImage());
      //dbr = session.getDatabaseRetriever();
   
    /////////////////////////////////////////////
    // set up the project table
   
    cards = new JPanel(new CardLayout());
    card_layout = (CardLayout) cards.getLayout();
    project_card = new ProjectPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(0, DialogMainFrame.PROJECT));
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

      public PlateSetPanel getPlateSetPanel() {
    return plate_set_card;
  }


  public void showProjectTable() {
    project_card = new ProjectPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(0, DialogMainFrame.PROJECT));
    cards.add(project_card, "ProjectPanel");
    card_layout.show(cards, "ProjectPanel");
  }

    
  public void showPlateSetTable(String _project_sys_name) {
      int project_id = Integer.parseInt(_project_sys_name.substring(4));
     
      //  plate_set_card = new PlateSetPanel(dbm, dbm.getPlateSetTableData(_project_sys_name), _project_sys_name);
      plate_set_card = new PlateSetPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.PLATESET), _project_sys_name);

    cards.add(plate_set_card, "PlateSetPanel");
    card_layout.show(cards, "PlateSetPanel");
  }


  public void showPlateTable(String _plate_set_sys_name) {
      
      int plate_set_id = Integer.parseInt(_plate_set_sys_name.substring(3));
      
      plate_card = new PlatePanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(plate_set_id, DialogMainFrame.PLATE), _plate_set_sys_name);
    
    cards.add(plate_card, "PlatePanel");
    card_layout.show(cards, "PlatePanel");
  }

      public void showAllPlatesTable(String _project_sys_name) {
      
      int project_id = Integer.parseInt(_project_sys_name.substring(4));
      
      all_plates_card = new AllPlatesPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLPLATES), _project_sys_name);
    
    cards.add(all_plates_card, "AllPlatesPanel");
    card_layout.show(cards, "AllPlatesPanel");
  }


  public void showWellTable(String _plate_sys_name) {
      int plate_id = Integer.parseInt(_plate_sys_name.substring(4));
      System.out.println(plate_id);
      well_card = new WellPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(plate_id, DialogMainFrame.WELL));
      cards.add(well_card, "Well");
      card_layout.show(cards, "Well");
  }

      public void showAllWellsTable(String _project_sys_name) {
      int project_id = Integer.parseInt(_project_sys_name.substring(4));
      
      all_wells_card = new AllWellsPanel(dbm, dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLWELLS), _project_sys_name);
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

      
  public DatabaseManager getDatabaseManager() {
    return this.dbm;
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
}
