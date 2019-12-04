package lnrocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

/** */
public class DatabaseManager {
    Connection conn;
  // CustomTable table;
    DatabaseInserter dbInserter;
    DatabaseRetriever dbRetriever;
    DialogMainFrame dmf;
    String source; // The connection source e.g. local, heroku
    // Session session;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  // psql -U ln_admin -h 192.168.1.11 -d lndb


  /**
   * Use 'lndb' as the database name. Regular users will connect as ln_user and will have restricted
   * access (no delete etc.). Connect as ln_admin to get administrative privileges.
   */
  public DatabaseManager() {
      //LOGGER.info("in session: " + _s);
      IFn require = Clojure.var("clojure.core", "require");
    require.invoke(Clojure.read("lnrocks.db-retriever"));
    IFn setUser = Clojure.var("lnrocks.db-retriever", "set-user");
    IFn setUserID = Clojure.var("lnrocks.db-retriever", "set-user-id");
    IFn getUserID = Clojure.var("lnrocks.db-retriever", "get-user-id");
    IFn setAuthenticated = Clojure.var("lnrocks.db-retriever", "set-authenticated");
    
   
      Long insertKey = 0L;
      try {
	  //  Class.forName("org.postgresql.Driver");
	  IFn getSource = Clojure.var("lnrocks.db-retriever", "get-source");
	  IFn getDBuser = Clojure.var("lnrocks.db-retriever", "get-db-user");
	  IFn getDBpassword = Clojure.var("lnrocks.db-retriever", "get-db-password");
	  IFn getURL = Clojure.var("lnrocks.db-retriever", "get-connection-string");
   
	  String target = (String)getSource.invoke();
	  String url = (String)getURL.invoke(target);
	  Properties props = new Properties();
	  //  props.setProperty("user", (String)getDBuser.invoke());
	  //props.setProperty("password", (String)getDBpassword.invoke());

	  //conn = DriverManager.getConnection(url, props);	
      

      //This is the first initialization of  DatabaseRetriever, DatabaseInserter
      dbRetriever = new DatabaseRetriever(this);
      dbInserter = new DatabaseInserter(this);
       dmf = new DialogMainFrame(this);
    } catch (ClassNotFoundException e) {
      LOGGER.severe("Class not found: " + e);
    }
  }


  // public void updateSessionWithProject(String _project_sys_name) {
  //   int results = 0;
  //   String project_sys_name = _project_sys_name;
  //   IFn setProjectSysName = Clojure.var("lnrocks.db-retriever", "set-project-sys-name");
  
  //   setProjectSysName.invoke(project_sys_name);
  //   // LOGGER.info("Project sys name: " + project_sys_name);
  //   try {
  //     String query =
  //         new String("SELECT id FROM project WHERE project_sys_name = '" + project_sys_name + "';");
  //     Statement st = conn.createStatement();
  //     ResultSet rs = st.executeQuery(query);
  //     rs.next();
  //     results = rs.getInt("id");
  //     rs.close();
  //     st.close();
  //     // LOGGER.info("projectID: " + results);
  //     IFn setProjectID = Clojure.var("lnrocks.db-retriever", "set-project-id");
  //     setProjectID.invoke(results);

  //   } catch (SQLException sqle) {
  //     LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
  //   }
    
  // }
 
  // public DefaultTableModel buildTableModel(ResultSet _rs) {

  //   try {
  //     ResultSet rs = _rs;
  //     ResultSetMetaData metaData = rs.getMetaData();
  //     int columnCount = metaData.getColumnCount();

  //     Vector<Vector<Object>> data = new Vector<Vector<Object>>();
  //     Vector<String> columnNames = new Vector<String>();
  //     /*
  //     String[] columnNames = new String[columnCount];
  //     for (int column = 0; column < columnCount; column++) {
  //       columnNames[column] = metaData.getColumnName(column + 1);
  //     }
  //     */
  //     for (int column = 0; column < columnCount; column++) {
  //       columnNames.addElement(metaData.getColumnName(column + 1));
  //     }

  //     // data of the table
  //     while (rs.next()) {
  //       Vector<Object> vector = new Vector<Object>();

  //       for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
  //         vector.add(rs.getObject(columnIndex + 1));
  //       }
  //       data.add(vector);
  //     }
  //     // LOGGER.info("data: " + data);
  //     return new DefaultTableModel(data, columnNames);

  //     //          data.stream().map(List::toArray).toArray(Object[][]::new), columnNames);

  //   } catch (SQLException sqle) {
  //     LOGGER.severe("SQLException in buildTableModel: " + sqle);
  //   }

  //   return null;
  // }

  /**
   * ******************************************************************
   *
   * <p>Generic DB activities
   *
   * <p>****************************************************************
   */
  public void insertPreparedStatement(PreparedStatement _preparedStatement) {
    PreparedStatement preparedStatement = _preparedStatement;
    //  LOGGER.info(preparedStatement.toString());

    try {
      preparedStatement.executeUpdate();

    } catch (SQLException sqle) {
      LOGGER.warning("Failed to execute prepared statement: " + preparedStatement.toString());
      LOGGER.warning("Exception: " + sqle);
    }
  }

  /** TableModel Columns: PSID Name Descr Format  called from the PlateSet menu item "group" */
  public void groupPlateSets(JTable _table) {
    // 4 columns in the plate set table
    JTable plate_set_table = _table;
    TableModel tableModel = plate_set_table.getModel();
    int[] selection = plate_set_table.getSelectedRows();
    String[][] results = new String[selection.length][6];

    //  LOGGER.info("selection: " + selection.toString());
    ArrayList<String> plateSet = new ArrayList<String>();
    Set<String> plateFormatSet = new HashSet<String>();
   Set<String> plateLayoutSet = new HashSet<String>();

    for (int i = 0; i < selection.length; i++) {
      for (int j = 0; j < 6; j++) {
        results[i][j] = tableModel.getValueAt(selection[i], j).toString();
        // LOGGER.info("i: " + i + " j: " + j + " results[i][j]: " + results[i][j]);
      }
    }
    for (int k = 0; k < selection.length; k++) {
      plateSet.add(results[k][0]);
      // LOGGER.info("prjID: " + results[k][0]);

      plateFormatSet.add(results[k][2]);
      // LOGGER.info("pltformat: " + results[k][2]);
      plateLayoutSet.add(results[k][5]);
    }
    //LOGGER.info("Size of plateFormatSet: " + plateFormatSet.size());
    if (plateFormatSet.size() == 1 && plateLayoutSet.size() == 1 ) {
      HashMap<String, String> numberOfPlatesInPlateSets =
          dbRetriever.getNumberOfPlatesInPlateSets(plateSet);
      String format = new String();
      for (Iterator<String> it = plateFormatSet.iterator(); it.hasNext(); ) {
        format = it.next();
      }
      String layout = new String();
      for (Iterator<String> it2 = plateLayoutSet.iterator(); it2.hasNext(); ) {
        layout = it2.next();
      }
      
      new DialogGroupPlateSet(this, numberOfPlatesInPlateSets, format, plateSet, layout);
    } else {
      JOptionPane.showMessageDialog(
          dmf,
          "Plate sets to be grouped must be of the same formats\n and of the same layout!",
          "Error!",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Launched by the Plate menu item "group". Since by definition a plate set can only have one format of
   * plate, no need to check that there is only one format
   */
  public void groupPlates(CustomTable _table) {
    // 6 columns in the plate  table
    // plate_set_sys_name plate_sys_name Order type format barcode
    CustomTable plate_table = _table;
    TableModel tableModel = plate_table.getModel();
    int[] selection = plate_table.getSelectedRows();
    String[][] results = new String[selection.length][5];
    String numberOfPlates = Integer.valueOf(selection.length).toString();

    //  LOGGER.info("selection: " + selection.toString());
    Set<String> plateSet = new HashSet<String>();

    for (int i = 0; i < selection.length; i++) {
      for (int j = 0; j < 5; j++) {
        results[i][j] = tableModel.getValueAt(selection[i], j).toString();
	//LOGGER.info("i: " + i + " j: " + j + " results[i][j]: " + results[i][j]);
      }
    }
    //build a set of the plate sys names, the second column
    for (int k = 0; k < selection.length; k++) {
      plateSet.add(results[k][1]);
      //LOGGER.info("pltID: " + results[k][0]);

    }
    String format = new String();
    try{
    format = results[1][4];
    //LOGGER.info("format: " + results[1][4]);
    new DialogGroupPlates(this, plateSet, format);
    }catch(ArrayIndexOutOfBoundsException aiob){
	  JOptionPane.showMessageDialog(
          dmf,
          "Select multiple plates!",
          "Error!",
          JOptionPane.ERROR_MESSAGE);
	  
    }
    
  }

     /**
      * Collapse multiple plates by quadrant. 
      *
      *<p> Plate set table:  id|plate_set_name|descr| plate_set_sys_name | num_plates|
      *plate_format_id|plate_type_id|project_id |updated            
      */
    public void reformatPlateSet(CustomTable _table){
    CustomTable plate_set_table = _table;
    TableModel tableModel = plate_set_table.getModel();
    int[] selection = plate_set_table.getSelectedRows();
    if (selection.length > 1){
       JOptionPane.showMessageDialog(dmf,
    "Select one plate set.",
    "Error",
    JOptionPane.ERROR_MESSAGE);
    }else{
       
	String format = (String)tableModel.getValueAt(selection[0], 2).toString();
	    String[] plate_set_sys_name = new String[1];
	    plate_set_sys_name[0] = tableModel.getValueAt(selection[0], 0).toString();
	    Integer[] plate_set_id = this.getDatabaseRetriever().getIDsForSysNames(plate_set_sys_name, "plate_set", "plate_set_sys_name");
	    String descr = (String)tableModel.getValueAt(selection[0], 4);
	    int num_plates = (int)tableModel.getValueAt(selection[0], 3);
	    String plate_type = (String)tableModel.getValueAt(selection[0], 4);
	    int num_samples = this.getDatabaseRetriever().getNumberOfSamplesForPlateSetID(plate_set_id[0]);
	    int plate_layout_name_id = this.getDatabaseRetriever().getPlateLayoutNameIDForPlateSetID((int)plate_set_id[0]);
	    //LOGGER.info("plate_set_id[0]: " + plate_set_id[0]);
	    switch(format){
	    case "96":
			DialogReformatPlateSet drps = new DialogReformatPlateSet( this, (int)plate_set_id[0], plate_set_sys_name[0], descr, num_plates, num_samples, plate_type, format, plate_layout_name_id);
		
		    break;
	    case "384":	 drps = new DialogReformatPlateSet( this, (int)plate_set_id[0], plate_set_sys_name[0], descr, num_plates, num_samples, plate_type, format, plate_layout_name_id);
		
		    break;
	    case "1536":  JOptionPane.showMessageDialog(dmf,
    "1536 well plates can not be reformatted.",
    "Error", JOptionPane.ERROR_MESSAGE);
		    break;
		    
		    }	
    }
    }

    /*    
  public DialogMainFrame getDmf() {
    return this.dmf;
  }
    */

    /**
     * In DatabaseManager (instead of DatabaseRetriever) because this is an early query
     * prior to instantiation of DatabaseRetriever.
     */
  // public int getUserIDForUserName(String _user_name) {
  //   String user_name = _user_name;
  //   // int plate_set_id;

  //   try {
  //     PreparedStatement pstmt =
  //         conn.prepareStatement(
  //             "SELECT lnuser.id FROM lnuser WHERE lnuser_name = ?;");

  //     pstmt.setString(1, user_name);
  //     ResultSet rs = pstmt.executeQuery();
  //     rs.next();
  //     int lnuser_id = Integer.valueOf(rs.getString("id"));

  //     // LOGGER.info("result: " + plate_set_id);
  //     rs.close();
  //     pstmt.close();
  //     return lnuser_id;

  //   } catch (SQLException sqle) {
  //     LOGGER.severe("SQL exception getting plateset_id: " + sqle);
  //   }
  //   int dummy = -1;
  //   return dummy;
  // }

        /**
     * In DatabaseManager (instead of DatabaseRetriever) because this is an early query
     * prior to instantiation of DatabaseRetriever.
     */
  // public String getUserGroupForUserName(String _user_name) {
  //   String user_name = _user_name;
  //   // int plate_set_id;

  //   try {
  //     PreparedStatement pstmt =
  //         conn.prepareStatement(
  //             "SELECT lnuser_groups.usergroup FROM lnuser, lnuser_groups WHERE lnuser.lnuser_name = ? AND lnuser.usergroup=lnuser_groups.id;");

  //     pstmt.setString(1, user_name);
  //     ResultSet rs = pstmt.executeQuery();
  //     rs.next();
  //     String usergroup = rs.getString("usergroup");

  //     // LOGGER.info("result: " + plate_set_id);
  //     rs.close();
  //     pstmt.close();
  //     return usergroup;

  //   } catch (SQLException sqle) {
  //     LOGGER.severe("SQL exception getting plateset_id: " + sqle);
  //   }
  //   String dummy = "error";
  //   return dummy;
  // }

    public DialogMainFrame getDialogMainFrame(){
	return this.dmf;
    }
    
  public DatabaseInserter getDatabaseInserter() {
    return this.dbInserter;
  }

  public DatabaseRetriever getDatabaseRetriever() {
    return this.dbRetriever;
  }

  public Connection getConnection() {
    return this.conn;
  }

 
}
