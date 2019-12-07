package lnrocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

/** 
 * executeUpdate expects no returns!!!
*/
public class DatabaseInserter {
  DatabaseManager dbm;
  DialogMainFrame dmf;
  // private DatabaseRetriever dbr;
  Connection conn;
  JTable table;
  Utilities utils;
    int session_id; 
    //  Session session;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  /** */
  public DatabaseInserter(DialogMainFrame dmf) {
    this.dbm = _dbm;
    this.conn = dbm.getConnection();
    // this.dbr = dbm.getDatabaseRetriever();
    //session = dbm.getSession();
    this.dmf = dmf;
    require.invoke(Clojure.read("ln.db-inserter"));
    require.invoke(Clojure.read("lnrocks.core"));
    IFn getSessionID = Clojure.var("lnrocks.core", "get-session-id");
    require.invoke(Clojure.read("ln.db-retriever"));
    
    session_id = ((Long)getSessionID.invoke()).intValue();
    // this.utils = dmf.getUtilities();
    //this.session = dmf.getSession();
  }

  public void insertProject(String _name, String _description, int _lnuser_id) {

    String insertSql = "SELECT new_project(?, ?, ?);";
    PreparedStatement insertPs;

    try {
      insertPs = conn.prepareStatement(insertSql);
      insertPs.setString(1, _description);
      insertPs.setString(2, _name);
      insertPs.setInt(3, _lnuser_id);
      int i = insertPs.executeUpdate();
   
      //      insertPreparedStatement(insertPs);
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
  }

  public void updateProject(String _name, String _description, String _project_sys_name) {

    String sqlstring = "UPDATE project SET project_name = ?, descr = ? WHERE project_sys_name = ?;";
    PreparedStatement preparedStatement;
    try {
      preparedStatement = conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, _name);
      preparedStatement.setString(2, _description);
      preparedStatement.setString(3, _project_sys_name);
      preparedStatement.executeUpdate();
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
  }

  public void updatePlateSet(String _name, String _description, String _plate_set_sys_name) {

    String sqlstring = "UPDATE plate_set SET plate_set_name = ?, descr = ? WHERE plate_set_sys_name = ?;";
    PreparedStatement preparedStatement;
    try {
      preparedStatement = conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, _name);
      preparedStatement.setString(2, _description);
      preparedStatement.setString(3, _plate_set_sys_name);
      preparedStatement.executeUpdate();
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
  }

    
  public void insertPreparedStatement(PreparedStatement _preparedStatement) {
    PreparedStatement preparedStatement = _preparedStatement;
    //LOGGER.info(preparedStatement.toString());

    try {
      preparedStatement.executeUpdate();

    } catch (SQLException sqle) {
      LOGGER.warning("Failed to execute prepared statement: " + preparedStatement.toString());
      LOGGER.warning("Exception: " + sqle);
    }
  }

  public static DefaultTableModel buildTableModel(ResultSet _rs) throws SQLException {

    ResultSet rs = _rs;
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    Vector<String> columnNames = new Vector<String>();
    for (int column = 1; column <= columnCount; column++) {
      columnNames.add(metaData.getColumnName(column));
    }

    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    while (rs.next()) {
      Vector<Object> vector = new Vector<Object>();
      for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
        vector.add(rs.getObject(columnIndex));
      }
      data.add(vector);
    }

    return new DefaultTableModel(data, columnNames);
  }

  // public void insertPlateSet2(
  //     String _name,
  //     String _description,
  //     String _num_plates,
  //     String _plate_size_id,
  //     String _plate_type_id,
  //     String _project_id,
  //     String _withSamples) {
  //     int new_plate_set_id;

  //   try {
   
  //     String insertSql = "SELECT new_plate_set ( ?, ?, ?, ?, ?, ?, ?, ?);";
  //     PreparedStatement insertPs =
  //         conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
  //     insertPs.setString(1, _description);
  //     insertPs.setString(2, _name);
  //     insertPs.setString(3, _num_plates);
  //     insertPs.setString(4, _plate_size_id);
  //     insertPs.setString(5, _plate_type_id);
  //     insertPs.setString(6, _project_id);
  //     insertPs.setInt(7,  session_id);
  //     insertPs.setString(8, _withSamples);
  //     LOGGER.info(insertPs.toString());
  //     insertPs.executeUpdate();
  //     //ResultSet resultSet = insertPs.getResultSet();
  //     //resultSet.next();
  //     //new_plate_set_id = resultSet.getInt("new_plate_set");
     
  //   } catch (SQLException sqle) {
  // 	LOGGER.warning("SQLE at inserting new plate set: " + sqle);
  //   }
    
  // }

    /**
     * Modification of insertPlateSet using integers and returning ps_id
     */
public int insertPlateSet(
      String _name,
      String _description,
      int _num_plates,
      int _plate_format_id,
      int _plate_type_id,
      int _project_id,
      int _plate_layout_name_id,
      boolean _withSamples) {
    
      int new_plate_set_id=0;
      //SELECT new_plate_set('d1', 'n1', 2,96,1,1,1,1,true);
    try {
	LOGGER.info("in dbi");

      String insertSql = "SELECT new_plate_set( ?, ?, ?, ?, ?, ?, ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
      insertPs.setString(1, _description);
      insertPs.setString(2, _name);
      insertPs.setInt(3, _num_plates);
      insertPs.setInt(4, _plate_format_id);
      insertPs.setInt(5, _plate_type_id);
      insertPs.setInt(6, _project_id);
      insertPs.setInt(7, _plate_layout_name_id);
      insertPs.setInt(8, session_id);
      insertPs.setBoolean(9, _withSamples);

      LOGGER.info(insertPs.toString());
      insertPs.execute();
      ResultSet resultSet = insertPs.getResultSet();
      resultSet.next();
      new_plate_set_id = resultSet.getInt("new_plate_set");
     
    } catch (SQLException sqle) {
	LOGGER.warning("SQLE at inserting new plate set: " + sqle);
    }
    return new_plate_set_id;
  }


    
  /**
   * called from DialogGroupPlateSet; Performs the creation of a new plate set from existing plate
   * sets. The HashMap contains the pair plateset_sys_name:number of plates. A dedicated Postgres
   * function "new_plate_set_from_group" will create the plateset and return the id without making
   * any plates, nor will it associate the new plate_set.id with plates.   Dialog assures that all 
   * plates are the same layout.
   *
   *<p>Note that the method groupPlatesIntoPlateSet is for grouping individual plates
   */
  public void groupPlateSetsIntoNewPlateSet(
      String _description,
      String _name,
      HashMap<String, String> _plate_set_num_plates,
      String _plate_format,
      String _plate_type,
      int _project_id,
      //int _plate_layout_name_id,
      ArrayList<String> _plate_sys_names) {

    String description = _description;
    String name = _name;
    HashMap<String, String> plate_set_num_plates = _plate_set_num_plates;
    String plate_format = _plate_format;
    String plate_type = _plate_type;
    int project_id = _project_id;
    int format_id = 0;
    int new_plate_set_id = 0;
    //int plate_layout_name_id = _plate_layout_name_id;
    ArrayList<String> plate_sys_names = _plate_sys_names;
  
    // determine total number of plates in new plate set
    int total_num_plates = 0;
    String a_plate_set_sys_name = ""; //get representative plate_set_sys_name to determine layout

    Iterator<Map.Entry<String,String>> it = plate_set_num_plates.entrySet().iterator();
    while (it.hasNext()) {
	HashMap.Entry<String, String> pair = (HashMap.Entry<String, String>) it.next();
      total_num_plates = total_num_plates + Integer.parseInt((String) pair.getValue());
      a_plate_set_sys_name = (String)pair.getKey();
      // it.remove(); // avoids a ConcurrentModificationException
    }
    // LOGGER.info("total: " + total_num_plates);

    // determine format id
    //LOGGER.info("format: " + plate_format);
    format_id = Integer.parseInt(plate_format);
    //    format_id = dbr.getPlateFormatID(plate_format);
    // determine type id
    int plateTypeID = dbm.getDatabaseRetriever().getIDForPlateType(plate_type);   
    IFn getLayoutForPlateSetSysName = Clojure.var("ln.db-retriever", "get-layout-id-for-plate-set-sys-name");
    int plate_layout_name_id = (int)getLayoutForPlateSetSysName.invoke(a_plate_set_sys_name);
    // determine plate.ids for plate_sys_names
    // use   public Integer[] getIDsForSysNames(String[] _sys_names, String _table, String _column)
    // {
    // from DatabaseRetriever
    //LOGGER.info("plate_sys_names: " + plate_sys_names);
    //LOGGER.info("hashsetplate_sys_names: " + new HashSet<String>(plate_sys_names));

    //LOGGER.info(
    //  "set: "
    //      + session.getDialogMainFrame().getUtilities().getStringArrayForStringSet(new HashSet<String>(plate_sys_names)));
    Integer[] plate_ids =
        dbm.getDatabaseRetriever()
            .getIDsForSysNames(
			       dmf.getUtilities().getStringArrayForStringSet(new HashSet<String>(plate_sys_names)),
                "plate",
                "plate_sys_name");

    // insert new plate set
    // INSERT INTO plate_set(descr, plate_set_name, num_plates, plate_size_id, plate_type_id,
    // project_id)

    //2019-09-23 remove layout and figure it out in the db
    String sqlstring = "SELECT new_plate_set_from_group (?, ?, ?, ?, ?, ?, ?, ?);";

    try {
      PreparedStatement preparedStatement =
          conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, description);
      preparedStatement.setString(2, name);
      preparedStatement.setInt(3, total_num_plates);
      preparedStatement.setInt(4, format_id);
      preparedStatement.setInt(5, plateTypeID);
      preparedStatement.setInt(6, project_id);
      preparedStatement.setInt(7, plate_layout_name_id);
      preparedStatement.setInt(8, session_id);
     
      //      preparedStatement.setArray(7, conn.createArrayOf("VARCHAR",
      // (plate_sys_names.toArray())));

      preparedStatement.execute(); // executeUpdate expects no returns!!!

      ResultSet resultSet = preparedStatement.getResultSet();
      resultSet.next();
      new_plate_set_id = resultSet.getInt("new_plate_set_from_group");
      // LOGGER.info("resultset: " + result);

    } catch (SQLException sqle) {
      LOGGER.warning("SQLE at inserting plate set from group: " + sqle);
    }

    // associate old (existing) plates with new plate set id
    //

    Set<Integer> all_plate_ids = new HashSet<Integer>();
    Iterator<Map.Entry<String,String>> it2 = plate_set_num_plates.entrySet().iterator();
    while (it2.hasNext()) {
	HashMap.Entry<String,String> pair = (HashMap.Entry<String,String>) it2.next();
	int plate_set_id =
          dbm.getDatabaseRetriever().getPlateSetIDForPlateSetSysName((String) pair.getKey());
	all_plate_ids.addAll(dbm.getDatabaseRetriever().getAllPlateIDsForPlateSetID(plate_set_id));
	it2.remove(); // avoids a ConcurrentModificationException
    }

    LOGGER.info("keys: " + plate_ids);

    this.associatePlateIDsWithPlateSetID(all_plate_ids, new_plate_set_id);
     IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
   
     dmf.showPlateSetTable((String)getProjectSysName.invoke());
  }

  /** Called from DialogGroupPlates from the plate panel/menubar */
  public void groupPlatesIntoPlateSet(
      String _description,
      String _name,
      Set<String> _plates,
      String _format,
      String _type,
      int _projectID,
      int _plate_layout_name_id) {
    String description = _description;
    String name = _name;
    Set<String> plates = _plates;
    String format = _format;
    String type = _type;
    int projectID = _projectID;
    int new_plate_set_id = 0;
    int plate_layout_name_id = _plate_layout_name_id;
    // ResultSet resultSet;
    // PreparedStatement preparedStatement;

    int format_id = Integer.parseInt(format);
      

    int plateTypeID = dbm.getDatabaseRetriever().getIDForPlateType(type);
    int num_plates = plates.size();

    String sqlstring = "SELECT new_plate_set_from_group (?, ?, ?, ?, ?, ?, ?, ?);";

    try {
      PreparedStatement preparedStatement =
          conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, description);
      preparedStatement.setString(2, name);
      preparedStatement.setInt(3, num_plates);
      preparedStatement.setInt(4, format_id);
      preparedStatement.setInt(5, plateTypeID);
      preparedStatement.setInt(6, projectID);
      preparedStatement.setInt(7, plate_layout_name_id);
      preparedStatement.setInt(8, session_id);
      
      //SELECT new_plate_set_from_group('desc', 'name1', 4, 96, 1, 1, 1, 1);
      preparedStatement.execute(); // executeUpdate expects no returns!!!

      ResultSet resultSet = preparedStatement.getResultSet();
      resultSet.next();
      new_plate_set_id = resultSet.getInt("new_plate_set_from_group");
      LOGGER.info(" new_plate_set_id: " + new_plate_set_id);

    } catch (SQLException sqle) {
      LOGGER.warning("SQLE at inserting plate set from group: " + sqle);
    }

    // associate old plates with new plate set id
    Set<Integer> plate_ids = new HashSet<Integer>();
    for (String temp : plates) {
      plate_ids.add(dbm.getDatabaseRetriever().getIDForSysName(temp, "plate"));
    }

   System.out.println("finished dbi.groupPlatesIntoPlateSet;  keys: " + plate_ids);

    this.associatePlateIDsWithPlateSetID(plate_ids, new_plate_set_id);
     IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
   
    dmf.showPlateSetTable((String)getProjectSysName.invoke());
  }

  public void associatePlateIDsWithPlateSetID(Set<Integer> _plateIDs, int _plate_set_id) {
    Set<Integer> plateIDs = _plateIDs;
    int plate_set_id = _plate_set_id;
    Integer[] plate_ids =
        Arrays.stream(dmf.getUtilities().getIntArrayForIntegerSet(plateIDs))
            .boxed()
            .toArray(Integer[]::new);

    String sqlString = "SELECT assoc_plate_ids_with_plate_set_id(?,?)";
    //SELECT assoc_plate_ids_with_plate_set_id(ARRAY[11,12,13],13);
    try {
      PreparedStatement preparedStatement = conn.prepareStatement(sqlString);
      preparedStatement.setArray(1, conn.createArrayOf("INTEGER", plate_ids));
      preparedStatement.setInt(2, plate_set_id);
      preparedStatement.execute(); // executeUpdate expects no returns!!!
    System.out.println("In dbi.assocPlateIdsWithPlateSetID;  insertSql: " + preparedStatement);
 
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
  }
  /* Method signature in DialogAddPlateSetData
      dbi.associateDataWithPlateSet(
          nameField.getText(),
          descrField.getText(),
          plate_set_sys_name,
          (ComboItem) assayTypes.getSelectedItem().getKey(),
          (ComboItem) plateLayouts.getSelectedItem().getKey(),
          session.getDialogMainFrame().getUtilities().loadDataFile(fileField.getText()),
          checkBox.isSelected()
          (ComboItem) algoritmList.getSelectedItem().getKey());
  */

  /** Called from DialogAddPlateSetData */
  public void associateDataWithPlateSet(
      String _assayName,
      String _descr,
      String _plate_set_sys_name,
      int _format_id,
      int _assay_type_id,
      int _plate_layout_name_id,
      ArrayList<String[]> _table,
      boolean _auto_select_hits,
      int _hit_selection_algorithm,
      int _top_n_number) {

    String assayName = _assayName;
    String descr = _descr;
    int format_id = _format_id;
    String[] plate_set_sys_name = new String[1];
    plate_set_sys_name[0] = _plate_set_sys_name;

    int assay_type_id = _assay_type_id;
    int plate_layout_name_id = _plate_layout_name_id;
    ArrayList<String[]> table = _table;
    boolean auto_select_hits = _auto_select_hits;
    int hit_selection_algorithm = _hit_selection_algorithm;
    int top_n_number = _top_n_number;


    
Integer[] plate_set_id =
        dbm.getDatabaseRetriever()
            .getIDsForSysNames(plate_set_sys_name, "plate_set", "plate_set_sys_name");

 int num_of_plate_ids = dbm.getDatabaseRetriever().getAllPlateIDsForPlateSetID(plate_set_id[0]).size();
//check that there are the correct number of rows in the table
if(num_of_plate_ids*format_id!=table.size()-1){
    	JOptionPane.showMessageDialog(dmf, new String("Expecting " + String.valueOf(num_of_plate_ids*format_id) + " rows but found " + (table.size()-1) + " rows." ), "Import Error", JOptionPane.ERROR_MESSAGE);
	return;
}

    int assay_run_id =
        createAssayRun(assayName, descr, assay_type_id, plate_set_id[0], plate_layout_name_id);


    //System.out.println("assay_run_id: " + assay_run_id);
    //System.out.println("table: " + table);
    
    // read in data file an populate assay_result with data;
    // only continue if successful
    // if (table.get(0)[0] == "plate" & table.get(0)[1] == "plate" & table.get(0)[2] == "plate") {
    String sql_statement = new String("INSERT INTO assay_result (assay_run_id, plate_order, well, response) VALUES ");

    table.remove(0); // get rid of the header
    for (String[] row : table) {
      sql_statement =
          sql_statement
	  + "("
	  + assay_run_id
	  + ", "
	  + Integer.parseInt(row[0])
	  + ", "
	  + Integer.parseInt(row[1])
	  + ", "
	  + Double.parseDouble(row[2])
	  + "), ";
    }

    String insertSql = sql_statement.substring(0, sql_statement.length() - 2) + ";";
    //System.out.println(insertSql);
    PreparedStatement insertPs;
    try {
      insertPs = conn.prepareStatement(insertSql);
      insertPreparedStatement(insertPs);
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
      JOptionPane.showMessageDialog(
          dmf, "Problems parsing data file!.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    
    
    /**
     *
     * <p>Diagnostic query
     *
     * <p>SELECT temp_data.plate, temp_data.well, temp_data.response ,plate_plate_set.plate_order ,
     * plate_set.plate_set_sys_name , plate.plate_sys_name, well.id, well.well_name,
     * sample.sample_sys_name FROM temp_data, plate_plate_set, plate_set, plate, well,sample,
     * well_sample, well_numbers WHERE temp_data.plate = plate_plate_set.plate_order AND
     * plate_plate_set.plate_id = plate.id AND well.plate_id = plate.id AND well_sample.well_id =
     * well.id AND well_sample.sample_id = sample.id AND plate_plate_set.plate_set_id = plate_set.id
     * AND plate_plate_set.plate_set_id = 21 AND temp_data.well = well_numbers.by_col AND
     * well_numbers.well_name = well.well_name AND well_numbers.plate_format = 96 ORDER BY
     * plate_plate_set.plate_order, well_numbers.by_col;
     */
    // table assay_result: sample_id, response, assay_run_id
    // table temp_data: plate, well, response, bkgrnd_sub, norm, norm_pos
    //                                plate is the plate number
    //                                norm is normalized to max signal of unknowns
    //                                norm_pos is normalised setting mean of positives to 1
    // table assay_run: id, plate_set_id, plate_layout_name_id
    // plate_layout:  plate_layout_name_id, well_by_col, well_type_id
    // plate:  id
    // well: plate_id id
    // sample: id
    // well_sample:  well_id  sample_id
    // well_numbers: format  well_name  by_col


    //here I need to call process_assay_run_data(_assay_run_id integer) to normalize and background subtract
    //normalized data is in existing columns in assay_result
    
    String sql1 =
        "SELECT process_assay_run_data( " + assay_run_id + ");";

    //LOGGER.info("insertSql: " + sql1);
    PreparedStatement insertPs2;
    try {
      insertPs2 = conn.prepareStatement(sql1);
      insertPreparedStatement(insertPs2);
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }

    //Now I need to select hits if requested by user.  I have the assay_run_id, and the algorithm for hit selection.
    // stored procedure: new_hit_list(_name VARCHAR, _descr VARCHAR, _num_hits INTEGER, _assay_run_id INTEGER,  _lnsession_id INTEGER, hit_list integer[])
    // DialogNewHitList(DialogMainFrame _session.getDialogMainFrame(), int  _assay_run_id, double[][] _selected_response, int _num_hits)
    // table = session.getDatabaseRetriever().getDataForScatterPlot(assay_run_id);
    // 	norm_response = new ResponseWrangler(table, ResponseWrangler.NORM);
   //    double[][]  sortedResponse [response] [well] [type_id] [sample_id];
    // selected_response.getHitsAboveThreshold(threshold))
 
    
    if(auto_select_hits){

	ResponseWrangler rw = new ResponseWrangler(dbm.getDatabaseRetriever().getDataForScatterPlot(assay_run_id),ResponseWrangler.NORM);
	double[][] sorted_response = rw.getSortedResponse();
	int number_of_hits = 0;
       
	
	switch(hit_selection_algorithm){
	case 1: //Top N
	    number_of_hits = top_n_number;
	    break;
	case 2: // mean(background) + 2SD
    
	    number_of_hits =  rw.getHitsAboveThreshold(rw.getMean_neg_2_sd() );
	    break;
	case 3:  // mean(background) + 3SD
	    number_of_hits =  rw.getHitsAboveThreshold(rw.getMean_neg_3_sd() );
	    break;
	case 4:  // >0% enhanced
	    number_of_hits =  rw.getHitsAboveThreshold(rw.getMean_pos() );
	    break;
	    
	}
	DialogNewHitList dnhl = new DialogNewHitList(dbm, assay_run_id, sorted_response, number_of_hits);
	
    }
  
    
  }

    
  public int createAssayRun(
      String _assayName,
      String _descr,
      int _assay_type_id,
      int _plate_set_id,
      int _plate_layout_name_id) {

    String assayName = _assayName;
    String descr = _descr;

    int plate_set_id = _plate_set_id;
    int assay_type_id = _assay_type_id;
    int plate_layout_name_id = _plate_layout_name_id;

    int new_assay_run_id = 0;

    String sqlstring = "SELECT new_assay_run(?, ?, ?, ?, ?, ?);";
    // LOGGER.info("sql: " + sqlstring);

    try {
      PreparedStatement preparedStatement =
          conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, assayName);
      preparedStatement.setString(2, descr);
      preparedStatement.setInt(3, assay_type_id);
      preparedStatement.setInt(4, plate_set_id);
      preparedStatement.setInt(5, plate_layout_name_id);
      preparedStatement.setInt(6, session_id);
      

      preparedStatement.execute(); // executeUpdate expects no returns!!!

      ResultSet resultSet = preparedStatement.getResultSet();
      resultSet.next();
      new_assay_run_id = resultSet.getInt("new_assay_run");
      // LOGGER.info("resultset: " + result);

    } catch (SQLException sqle) {
      LOGGER.warning("SQLE at inserting plate set from group: " + sqle);
    }
    // LOGGER.info("new assay id: " + new_assay_run_id);
    return new_assay_run_id;
  }

    /**
     * Called from AdminMenu
     */
    public void deleteProject(int _prj_id){
	int prj_id = _prj_id;
    String sqlstring = "delete from project  WHERE project.id = ?;";
    PreparedStatement preparedStatement;
    try {
      preparedStatement = conn.prepareStatement(sqlstring);
      preparedStatement.setInt(1, prj_id);
      preparedStatement.executeUpdate();
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
    dmf.showProjectTable();
  }


     
    /**
     * Called from DialogReformatPlateSet OK action listener
     */
    public void reformatPlateSet(int _source_plate_set_id, DialogMainFrame _dmf,  String _dest_plate_set_name, int _source_plate_num,
				 String _dest_descr, int _dest_plate_format_id, int _dest_plate_type_id,  int _dest_plate_layout_name_id, int _n_reps_source){
	int source_plate_set_id = _source_plate_set_id;
	DialogMainFrame dmf = _dmf;
	String dest_plate_set_name = _dest_plate_set_name;
	String dest_descr = _dest_descr;
	int dest_plate_format_id = _dest_plate_format_id;
	int dest_plate_type_id= _dest_plate_type_id;
	int dest_plate_layout_name_id = _dest_plate_layout_name_id;
	int source_plate_num = _source_plate_num;
	int n_reps_source = _n_reps_source;
       
	int dest_plate_num = (int)Math.ceil(source_plate_num*n_reps_source/4.0);
	IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");
   	int project_id = ((Long)getProjectID.invoke()).intValue();
	int dest_plate_set_id=0;

      // method signature:  reformat_plate_set(source_plate_set_id INTEGER, source_num_plates INTEGER, n_reps_source INTEGER, dest_descr VARCHAR(30), dest_plate_set_name VARCHAR(30), dest_num_plates INTEGER, dest_plate_format_id INTEGER, dest_plate_type_id INTEGER, project_id INTEGER, dest_plate_layout_name_id INTEGER )
      
      
    try {
      String insertSql = "SELECT reformat_plate_set( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
      insertPs.setInt(1, source_plate_set_id);
      insertPs.setInt(2, source_plate_num);
      insertPs.setInt(3, n_reps_source);
      
      insertPs.setString(4, dest_descr);
      insertPs.setString(5, dest_plate_set_name);
      insertPs.setInt(6, dest_plate_num);
      insertPs.setInt(7, dest_plate_format_id);
      insertPs.setInt(8, dest_plate_type_id);
      insertPs.setInt(9, project_id);
      insertPs.setInt(10, dest_plate_layout_name_id);
      insertPs.setInt(11, session_id);
      

      LOGGER.info(insertPs.toString());
      insertPs.execute();
      ResultSet resultSet = insertPs.getResultSet();
      resultSet.next();
      dest_plate_set_id = resultSet.getInt("reformat_plate_set");
       IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
   
      dmf.showPlateSetTable((String)getProjectSysName.invoke());
    } catch (SQLException sqle) {
	LOGGER.warning("SQLE at reformat plate set: " + sqle);
    }
      
	
	
	LOGGER.info("new_(reformatted)plate_set_id: " + dest_plate_set_id);
      
}
    
    public void insertUser(String _name, String _tags, String _password, int _group_id){

	    String sqlString = "SELECT new_user(?,?, ?, ?)";
    // LOGGER.info("insertSql: " + insertSql);
    try {
      PreparedStatement preparedStatement = conn.prepareStatement(sqlString);
      preparedStatement.setString(1, _name);
      preparedStatement.setString(2, _tags);
      preparedStatement.setString(3, _password);
      preparedStatement.setInt(4, _group_id);
      preparedStatement.execute(); // executeUpdate expects no returns!!!

    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
	
    }

    public void insertPlateLayout(String _name, String _descr,  String _file_name){
	String name = _name;
	String descr = _descr;
	int format = 0;
	ArrayList<String[]> data = dmf.getUtilities().loadDataFile(_file_name);

	Object[][]  dataObject = dmf.getUtilities().getObjectArrayForArrayList(data); 

	switch(data.size()-1){
	case 96:
	    format = 96;
	    // ImportLayoutViewer ilv = new ImportLayoutViewer(dmf, dataObject);   
	    break;
	case 384:
	    format = 384;
	    
	    break;
	case 1536:
	    format = 1536;
	    
	    break;
	default:
	    JOptionPane.showMessageDialog( dmf, "Expecting 96, 384, or 1536 lines of data. Found " + (data.size()-1) +  "!", "Error", JOptionPane.ERROR_MESSAGE);	    
	}
	      
	    String sqlString = "SELECT new_plate_layout(?,?, ?, ?)";
    // LOGGER.info("insertSql: " + insertSql);
    try {
      PreparedStatement preparedStatement = conn.prepareStatement(sqlString);
      preparedStatement.setString(1, name);
      preparedStatement.setString(2, descr);
      preparedStatement.setInt(3, format);
      preparedStatement.setArray(4, conn.createArrayOf("VARCHAR", (data.toArray())));
      preparedStatement.execute(); // executeUpdate expects no returns!!!

    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
    }

    /**
     * @param sorted_response  [response] [well] [type_id] [sample_id]
     * the number of hits are "unknown" hits so must screen for type_id == 1
     * an object array must be passed to the stored procedure
     */
      public void insertHitList(String _name,
				String _description,
				int _num_hits,
				int  _assay_run_id,
				double[][] sorted_response) {
	  
      int new_hit_list_id;
      
      Object[] hit_list = new Object[_num_hits];
      int counter = 0;
      for(int i = 0; i < sorted_response.length; i++){
	  if(sorted_response[i][2]== 1 && counter < _num_hits){
	  hit_list[counter] = (Object)Math.round(sorted_response[i][3]);
	  counter++;
      }
	  //System.out.println("i: " + i + " " + hit_list[i]);
      }
      
      
    try {
      String insertSql = "SELECT new_hit_list ( ?, ?, ?, ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
      insertPs.setString(1, _name);
      insertPs.setString(2, _description);
      insertPs.setInt(3, _num_hits);
      insertPs.setInt(4, _assay_run_id);
      insertPs.setInt(5, session_id);
      insertPs.setArray(6, conn.createArrayOf("INTEGER", hit_list));
   
      //LOGGER.info(insertPs.toString());
      insertPs.executeUpdate();
      //ResultSet resultSet = insertPs.getResultSet();
      //resultSet.next();
      //new_plate_set_id = resultSet.getInt("new_plate_set");
     
    } catch (SQLException sqle) {
	LOGGER.warning("SQLE at inserting new plate set: " + sqle);
    }
    
  }

    /**
     * This method preps variable for displaying the dialog box
     */
    public void importAccessionsByPlateSet(int _plate_set_id){
	int plate_set_id = _plate_set_id;
	String plate_set_sys_name = new String("PS-" + String.valueOf(plate_set_id));
	int plate_num = dbm.getDatabaseRetriever().getNumberOfPlatesForPlateSetID(plate_set_id);
	int format_id = dbm.getDatabaseRetriever().getFormatForPlateSetID(plate_set_id);
	   
		new DialogImportPlateSetAccessionIDs(dbm, plate_set_sys_name, plate_set_id, format_id, plate_num);
	
	
    }

    /**
     * Loads table and make the association
     *
     * accessions looks like:
     * plate	well	accs.id
     * 1	1	AMRVK5473H
     * 1	2	KMNCX9294W
     * 1	3	EHRXZ2102Z
     * 1	4	COZHR7852Q
     * 1	5	FJVNR6433Q
     */
    public void associateAccessionsWithPlateSet(int  _plate_set_id, int _format, ArrayList<String[]> _accessions){

	int plate_set_id = _plate_set_id;
	int format = _format;
	ArrayList<String[]> accessions = _accessions;
	
	    // read in data file an populate assay_result with data;
    // only continue if successful
    String sql_statement = new String("INSERT INTO temp_accs_id (plate_order, by_col, accs_id) VALUES ");
    //LOGGER.info(accessions.get(0)[0] + " " + accessions.get(0)[1] + " " +accessions.get(0)[2] );	
    if (accessions.get(0)[0].equals("plate") & accessions.get(0)[1].equals("well") & accessions.get(0)[2].equals("accs.id")) {

    accessions.remove(0); // get rid of the header
    for (String[] row : accessions) {
      sql_statement =
          sql_statement
	  + "("
	  + Integer.parseInt(row[0])
	  + ", "
	  + Integer.parseInt(row[1])
	  + ", '"
	  + row[2]
	  + "'), ";
    }
    }else{
    JOptionPane.showMessageDialog(
				  dmf, "Expecting the headers \"plate\", \"well\", and \"accs.id\", but found\n" + accessions.get(0)[0] + ", " +  accessions.get(0)[1] +  ", and " + accessions.get(0)[2] + "." , "Error", JOptionPane.ERROR_MESSAGE);
    return;
  	
    }

    String insertSql = "SELECT process_access_ids(?,?);";
    //LOGGER.info("sqlstatement: " + insertSql);
    PreparedStatement insertPs;
    try {
      insertPs = conn.prepareStatement(insertSql);
          insertPs.setInt(1, plate_set_id);
      insertPs.setString(2, sql_statement.substring(0, sql_statement.length() - 2));
  
      insertPreparedStatement(insertPs);
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
      JOptionPane.showMessageDialog(
          dmf, "Problems parsing accesion ids file!.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    

    }



    /**
     * This method preps variable for displaying the dialog box
     */
    public void importBarcodesByPlateSet(int _plate_set_id){
	int plate_set_id = _plate_set_id;
	String plate_set_sys_name = new String("PS-" + String.valueOf(plate_set_id));
	int plate_num = dbm.getDatabaseRetriever().getNumberOfPlatesForPlateSetID(plate_set_id);
	//	int format_id = dbm.getDatabaseRetriever().getFormatForPlateSetID(plate_set_id);
	   
		new DialogImportPlateSetBarcodeIDs(dbm, plate_set_sys_name, plate_set_id,  plate_num);	
    }


    
  public void insertRearrayedPlateSet(
      String _name,
      String _description,
      String _num_plates,
      int _plate_format_id,
      int _plate_type_id,
      int _plate_layout_id,
      int _hit_list_id,
      int _source_plate_set_id) {

      int source_plate_set_id = _source_plate_set_id;
      int hit_list_id = _hit_list_id;
      int dest_plate_set_id =0;
    try {
	 IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");
	 //fails as long   
	 int project_id = ((Long)getProjectID.invoke()).intValue();
      int plate_format_id = _plate_format_id;
      int plate_type_id = _plate_type_id;
      int plate_layout_id = _plate_layout_id;
      // new_plate_set(_descr VARCHAR(30),_plate_set_name VARCHAR(30), _num_plates INTEGER, _plate_format_id INTEGER, _plate_type_id INTEGER, _project_id INTEGER, _plate_layout_name_id INTEGER, _lnsession_id INTEGER, _with_samples boolean)         

      String insertSql1 = "SELECT new_plate_set ( ?, ?, ?, ?, ?, ?, ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql1, Statement.RETURN_GENERATED_KEYS);
      insertPs.setString(1, _description);
      insertPs.setString(2, _name);
      insertPs.setInt(3, Integer.valueOf(_num_plates));
      insertPs.setInt(4, plate_format_id);
      insertPs.setInt(5, plate_type_id);
      insertPs.setInt(6, project_id);
      insertPs.setInt(7, plate_layout_id);
      insertPs.setInt(8, session_id);
      insertPs.setBoolean(9, false);      
      // LOGGER.info(insertPs.toString());
      insertPs.execute();  //executeUpdate() expects no returns
      ResultSet resultSet = insertPs.getResultSet();
      resultSet.next();
      dest_plate_set_id = resultSet.getInt("new_plate_set");

      //LOGGER.info("dest_plate_set_id: " + dest_plate_set_id);

       insertPs.close();
      //  SELECT new_plate_set ( 'descrip', 'myname', '10', '96', 'assay', 0, 't')
    } catch (SQLException sqle) {
      LOGGER.severe("Failed to create plate set: " + sqle);
    }

    //LOGGER.info("source_plate_set_id: " + source_plate_set_id);
    //LOGGER.info("dest_plate_set_id: " + dest_plate_set_id);
    //LOGGER.info("hit_list_id: " + hit_list_id);
    
    //plate set has been registered, new plates/empty wells created
    //now copy samples into newly created (empty) plate
    //  SELECT rearray_transfer_samples(source_plate_set, dest_plate_set, hit_list)
  try {
      String insertSql2 = "SELECT rearray_transfer_samples( ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql2, Statement.RETURN_GENERATED_KEYS);
      insertPs.setInt(1, source_plate_set_id);
      insertPs.setInt(2, dest_plate_set_id);
      insertPs.setInt(3, hit_list_id);
   int rowsAffected   = insertPs.executeUpdate();
       ResultSet rsKey = insertPs.getGeneratedKeys();
       rsKey.next();
       insertPs.close();
     
  } catch (SQLException sqle) {
      LOGGER.severe("Failed to create plate set: " + sqle);
    }
    
  //refresh the plate set table so worklists are recognized

  
  }
    /**
     * @param _data
     * batch sql https://www.mkyong.com/jdbc/jdbc-preparedstatement-example-batch-update/
     */
    public void importPlateLayout(Object[][] _data, String _name, String _descr, String _control_location, int _n_controls, int _n_unknowns, int _format, int _n_edge){
	Object[][] data = _data;
	String name = _name;
	String descr = _descr;
	String control_location = _control_location;
	int n_controls = _n_controls;
	int n_unknowns = _n_unknowns;
	int format = _format;
	int n_edge = _n_edge;

	String sql_statement1="TRUNCATE TABLE import_plate_layout;";
	
	String sql_statement2_pre = "INSERT INTO import_plate_layout (well_by_col, well_type_id, replicates, target) VALUES ";
	for (int i = 1; i < data.length; i++) {
      sql_statement2_pre =
          sql_statement2_pre
	  + "("
	  + Integer.parseInt((String)data[i][0])
	  + ", "
	  + Integer.parseInt((String)data[i][1])
	  + ", 1, 1), ";
    }

	String sql_statement2 = sql_statement2_pre.substring(0, sql_statement2_pre.length() - 2) + ";";

	String sql_statement3 = "SELECT create_layout_records(?,?,?,?,?,?,?);";
    //LOGGER.info(insertSql);
    PreparedStatement insertPs;
    try {
	conn.setAutoCommit(false);
      insertPs = conn.prepareStatement(sql_statement1);
      insertPreparedStatement(insertPs);
      //insertPs.addBatch();
	
      insertPs = conn.prepareStatement(sql_statement2);
      insertPreparedStatement(insertPs);
      insertPs = conn.prepareStatement(sql_statement3);
      insertPs.setString(1, name);
      insertPs.setString(2, descr);
      insertPs.setString(3, control_location);
      insertPs.setInt(4, n_controls);
      insertPs.setInt(5, n_unknowns);
      insertPs.setInt(6, format);
      insertPs.setInt(7, n_edge);
      insertPreparedStatement(insertPs);
      //insertPs.addBatch();
      //insertPs.executeBatch();
      //insertPreparedStatement(insertPs);
      conn.commit();
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
      JOptionPane.showMessageDialog(
          dmf, "Problems parsing data file!.", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

	
    }

    /**
     * Insert a hit list read from a file.  The file text and contains one sample id per line
     * Sample id can be SPL-100 or 100.  Method will determine which and behave appropriately.
     * Expecting a column header, i.e. the first line of the input file is ignored.
     */
    public void insertHitListFromFile(int _assay_run_id, Vector<String> _s_ids_pre){
	int assay_run_id = _assay_run_id;
	Vector<String> s_ids_pre = _s_ids_pre;
	//convert to an int[] array all hits
	int[] s_ids = new int[s_ids_pre.size()];
	Iterator value = s_ids_pre.iterator();
	int counter = 0;
	if(s_ids_pre.get(0).substring(0,3).equals("SPL")){
	    while (value.hasNext()) {
		s_ids[counter] = (int)Integer.parseInt(((String)value.next()).substring(4));
		counter = counter +1;
        } 
	    
	}else{
	    while (value.hasNext()) {
		s_ids[counter] = (int)Integer.parseInt((String)value.next());
		counter = counter +1;
             
        }
	    
	}
	new DialogNewHitListFromFile(dbm, assay_run_id, s_ids);
    }

    /**
     * Follow up to insertHitListFromFile responding to DialogNewHitListFromFile
     */
    public void insertHitListFromFile2(String _name, String _description, int _num_hits, int _assay_run_id, int[] _hit_list){

	Integer[] hit_list = Arrays.stream( _hit_list ).boxed().toArray( Integer[]::new );
	//Object[] hit_list = (Integer[])_hit_list;
	      try {
      String insertSql = "SELECT new_hit_list ( ?, ?, ?, ?, ?, ?);";
      PreparedStatement insertPs =
          conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
      insertPs.setString(1, _name);
      insertPs.setString(2, _description);
      insertPs.setInt(3, _num_hits);
      insertPs.setInt(4, _assay_run_id);
      insertPs.setInt(5, session_id);
      
      insertPs.setArray(6, conn.createArrayOf("INTEGER", hit_list));
   
      LOGGER.info(insertPs.toString());
      insertPs.executeUpdate();
      //ResultSet resultSet = insertPs.getResultSet();
      //resultSet.next();
      //new_plate_set_id = resultSet.getInt("new_plate_set");
     
    } catch (SQLException sqle) {
	LOGGER.warning("SQLE at inserting new plate set: " + sqle);
    }


      }

      /**
   * Incoming variables: ( 'plate set name' 'description' '10' '96' 'assay')
   *
   * <p>Method signature in Postgres: CREATE OR REPLACE FUNCTION new_plate_set(_descr
   * VARCHAR(30),_plate_set_name VARCHAR(30), _num_plates INTEGER, _plate_format_id INTEGER,
   * _plate_type_id INTEGER, _project_id INTEGER, _with_samples boolean)
   */
 //  public void insertPlateSet(
 //      String _name,
 //      String _description,
 //      String _num_plates,
 //      String _plate_format_id,
 //      int _plate_type_id,
 //      int _plate_layout_id) {

 //    try {
 // 	 IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");
   
 // 	 int project_id = ((Long)getProjectID.invoke()).intValue();
 //      int plate_format_id = Integer.parseInt(_plate_format_id);
 //      int plate_type_id = _plate_type_id;
 //      int plate_layout_id = _plate_layout_id;
         

 //      String insertSql = "SELECT new_plate_set ( ?, ?, ?, ?, ?, ?, ?, ?);";
 //      PreparedStatement insertPs =
 //          conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
 //      insertPs.setString(1, _description);
 //      insertPs.setString(2, _name);
 //      insertPs.setInt(3, Integer.valueOf(_num_plates));
 //      insertPs.setInt(4, plate_format_id);
 //      insertPs.setInt(5, plate_type_id);
 //      insertPs.setInt(6, project_id);
 //      insertPs.setInt(7, plate_layout_id);    
 //      insertPs.setBoolean(8, true);

 //      LOGGER.info(insertPs.toString());
 //      int rowsAffected   = insertPs.executeUpdate();
 //       ResultSet rsKey = insertPs.getGeneratedKeys();
 //       rsKey.next();
 //       int new_ps_id = rsKey.getInt(1);
 //       insertPs.close();
 //      //  SELECT new_plate_set ( 'descrip', 'myname', '10', '96', 'assay', 0, 't')
 //    } catch (SQLException sqle) {
 //      LOGGER.severe("Failed to create plate set: " + sqle);
 //    }
 // IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
   
 //    dmf.showPlateSetTable((String)getProjectSysName.invoke());    
 //  }

}
