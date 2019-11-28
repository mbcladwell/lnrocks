package lnrocks;

import java.sql.Array;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

/** */
public class DatabaseRetriever {
  DatabaseManager dbm;
    DialogMainFrame dmf;
  Connection conn;
  JTable table;
    //    Session session;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  /** */
  public DatabaseRetriever(DatabaseManager _dbm) {
    this.dbm = _dbm;
    this.conn = dbm.getConnection();
    //session = dbm.getSession();
    dmf = dbm.getDialogMainFrame();
    require.invoke(Clojure.read("ln.codax-manager"));
    
  }

  /**
   * ******************************************************************
   *
   * <p>Project related
   *
   * <p>****************************************************************
   */

    /**
     * This is the table retriever for the main entity tables project, plate set, plate, well
     * @param _id the primary key for the table
     * @param _the desired table as an int
     */
    public CustomTable getDMFTableData(int _id, int _desired_table) {
    	int id = _id;
	int desired_table = _desired_table;
	String sql_statement = new String();
	//System.out.println("desired_table: " + desired_table);
    try {

      switch(desired_table){
      case DialogMainFrame.PROJECT:
	  sql_statement = "SELECT project_sys_name AS \"ProjectID\", project_name As \"Name\", lnuser_name AS \"Owner\", descr AS \"Description\" FROM project, lnuser, lnsession  WHERE project.lnsession_id=lnsession.id AND lnuser.id=lnsession.lnuser_id ORDER BY project.id DESC;";
	  break;

	 
      case DialogMainFrame.PLATESET:
 
	  sql_statement = "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate_set_name As \"Name\", format AS \"Format\", num_plates AS \"# plates\" , plate_type.plate_type_name AS \"Type\", plate_layout_name.name AS \"Layout\", plate_set.descr AS \"Description\", rearray_pairs.ID AS \"Worklist\" FROM  plate_format, plate_type, plate_layout_name, plate_set FULL outer JOIN rearray_pairs ON plate_set.id= rearray_pairs.dest WHERE plate_format.id = plate_set.plate_format_id AND plate_set.plate_layout_name_id = plate_layout_name.id  AND plate_set.plate_type_id = plate_type.id  AND project_id = ? ORDER BY plate_set.id DESC;";
	  break;
      case DialogMainFrame.PLATE:
	  sql_statement = "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate.plate_sys_name AS \"PlateID\", plate_plate_set.plate_order AS \"Order\",  plate_type.plate_type_name As \"Type\", plate_format.format AS \"Format\", plate.barcode AS \"Barcode ID\" FROM plate_set, plate, plate_type, plate_format, plate_plate_set WHERE plate_plate_set.plate_set_id = ? AND plate.plate_type_id = plate_type.id AND plate_plate_set.plate_id = plate.id AND plate_plate_set.plate_set_id = plate_set.id  AND plate_format.id = plate.plate_format_id ORDER BY plate_plate_set.plate_order DESC;";
	  break;
      case DialogMainFrame.WELL:
	  sql_statement = "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate.plate_sys_name AS \"PlateID\", well_numbers.well_name AS \"Well\", well.by_col AS \"Well_NUM\", sample.sample_sys_name AS \"Sample\", sample.accs_id as \"Accession\" FROM plate_plate_set, plate_set, plate, sample, well_sample, well JOIN well_numbers ON ( well.by_col= well_numbers.by_col)  WHERE plate.id = well.plate_id AND well_sample.well_id=well.id AND well_sample.sample_id=sample.id AND well.plate_id = ?  AND plate_plate_set.plate_id = plate.id AND plate_plate_set.plate_set_id = plate_set.ID AND  well_numbers.plate_format = (SELECT plate_format_id  FROM plate_set WHERE plate_set.ID =  (SELECT plate_set_id FROM plate_plate_set WHERE plate_id = plate.ID LIMIT 1) ) ORDER BY plate.id DESC, well.by_col DESC;";
	  break;
      case DialogMainFrame.ALLPLATES:
	  sql_statement = "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate.plate_sys_name AS \"PlateID\", plate_plate_set.plate_order AS \"Order\",  plate_type.plate_type_name As \"Type\", plate_format.format AS \"Format\", plate.barcode AS \"Barcode ID\" FROM plate_set, plate, plate_type, plate_format, plate_plate_set WHERE plate_set.project_id = ? AND plate.plate_type_id = plate_type.id AND plate_plate_set.plate_id = plate.id AND plate_plate_set.plate_set_id = plate_set.id  AND plate_format.id = plate.plate_format_id ORDER BY plate_plate_set.plate_set_id, plate_plate_set.plate_order DESC;";
	  break;
      case DialogMainFrame.ALLWELLS:
	  sql_statement = "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate.plate_sys_name AS \"PlateID\", well_numbers.well_name AS \"Well\", well.by_col AS \"Well_NUM\", sample.sample_sys_name AS \"Sample\", sample.accs_id as \"Accession\" FROM  plate_plate_set, plate_set, plate, sample, well_sample, well JOIN well_numbers ON ( well.by_col= well_numbers.by_col)  WHERE plate.id = well.plate_id AND well_sample.well_id=well.id AND well_sample.sample_id=sample.id AND plate_set.project_id = ? AND plate_plate_set.plate_id = plate.id AND plate_plate_set.plate_set_id = plate_set.ID AND  well_numbers.plate_format = (SELECT plate_format_id  FROM plate_set WHERE plate_set.ID =  (SELECT plate_set_id FROM plate_plate_set WHERE plate_id = plate.ID LIMIT 1) ) ORDER BY plate_set.id, plate.id, well.by_col DESC;";
	  break;
	  
      }
      
      java.sql.PreparedStatement pstmt =  conn.prepareStatement(sql_statement);
      if(desired_table != DialogMainFrame.PROJECT){
	  pstmt.setInt(1, id);
      }

      ResultSet rs =  pstmt.executeQuery();
      CustomTable table = new CustomTable(dmf, buildTableModel(rs));

      rs.close();
      pstmt.close();
      return table;
    } catch (SQLException sqle) {
    }
    return null;
  }

  public String getDescriptionForProject(String _project_sys_name) {
    String result = new String();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT descr  FROM  project WHERE project_sys_name =  ?;");

      pstmt.setString(1, _project_sys_name);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getString("descr");
      // LOGGER.info("Description: " + result);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting description: " + sqle);
    }
    return result;
  }

  public int getProjectIDForProjectSysName(String _project_sys_name) {
    String project_sys_name = _project_sys_name;
    // int plate_set_id;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT project.id FROM   project WHERE project_sys_name = ?;");

      pstmt.setString(1, project_sys_name);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int project_id = Integer.valueOf(rs.getString("id"));

      // LOGGER.info("result: " + plate_set_id);
      rs.close();
      pstmt.close();
      return project_id;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting project_id: " + sqle);
    }
    int dummy = -1;
    return dummy;
  }

    public ComboItem[] getAllProjects(){
	    ComboItem[] results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
 
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT id, project_sys_name from project;");

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }

      // LOGGER.info("Description: " + results);
      rs.close();
      pstmt.close();
      results = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return results;

    }

    
  /**
   * ******************************************************************
   *
   * <p>Plate Set related ********************************************
   *
   * <p>****************************************************************
   */

 public CustomTable getPlateSetTableData(int _project_id) {
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT plate_set.plate_set_sys_name AS \"PlateSetID\", plate_set_name As \"Name\", format AS \"Format\", num_plates AS \"# plates\" , plate_type.plate_type_name AS \"Type\", plate_layout_name.name AS \"Layout\"   , plate_set.descr AS \"Description\" FROM plate_set, plate_format, plate_type, plate_layout_name WHERE plate_format.id = plate_set.plate_format_id AND plate_set.plate_layout_name_id = plate_layout_name.id  AND plate_set.plate_type_id = plate_type.id AND project_id =  ? ORDER BY plate_set.id DESC;");

      pstmt.setInt(1, _project_id);
      ResultSet rs = pstmt.executeQuery();

      CustomTable table = new CustomTable(dmf, buildTableModel(rs));
      rs.close();
      pstmt.close();
      return table;
    } catch (SQLException sqle) {

    }
    return null;
  }

    
  public String getPlateSetSysNameForPlateSysName(String _plate_sys_name) {
    String result = new String();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT plate_set.plate_set_sys_name  FROM  plate_set, plate, plate_plate_set WHERE plate_plate_set.plate_set_id = plate_set.id AND plate_plate_set.plate_id = plate.id AND plate_sys_name =  ?;");

      pstmt.setString(1, _plate_sys_name);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getString("plate_set_sys_name");
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_sys_name from plate_sys_name: " + sqle);
    }
    return result;
  }

  public String getDescriptionForPlateSet(String _plateset_sys_name) {
    String result = new String();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT descr  FROM  plate_set WHERE plate_set_sys_name =  ?;");

      pstmt.setString(1, _plateset_sys_name);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getString("descr");
      // LOGGER.info("Description: " + result);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting description: " + sqle);
    }
    return result;
  }

  public int getPlateSetIDForPlateSetSysName(String _plateset_sys_name) {
    String plateset_sys_name = _plateset_sys_name;
    // int plate_set_id;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT plate_set.id FROM   plate_set WHERE plate_set_sys_name = ?;");

      pstmt.setString(1, plateset_sys_name);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int plate_set_id = Integer.valueOf(rs.getString("id"));

      // LOGGER.info("result: " + plate_set_id);
      rs.close();
      pstmt.close();
      return plate_set_id;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_id: " + sqle);
    }
    int dummy = -1;
    return dummy;
  }

  /**
   * Return a key/value HashMap with the number of plates in each plate set. Used to inform user
   * when grouping plate sets. @Set projectSet a set that contains plate_set IDs to be iterated
   * over.
   */
  public HashMap<String, String> getNumberOfPlatesInPlateSets(ArrayList<String> _plate_setSet) {
    ArrayList<String> plate_setSet = _plate_setSet;
    HashMap<String, String> plate_setPlateCount = new HashMap<String, String>();
    String result;
    for (String s : plate_setSet) {
      try {
        PreparedStatement pstmt =
            conn.prepareStatement(
                "SELECT count(*) AS exact_count FROM (SELECT plate.plate_sys_name  FROM  plate, plate_set, plate_plate_set WHERE plate_plate_set.plate_set_id = plate_set.id AND plate_plate_set.plate_id = plate.id AND plate_set_sys_name = ?) AS count;");

        pstmt.setString(1, s);
        ResultSet rs = pstmt.executeQuery();
        rs.next();
        result = rs.getString("exact_count");
        plate_setPlateCount.put(s, result);
        // LOGGER.info("s: " + s + " result: " + result);
        rs.close();
        pstmt.close();

      } catch (SQLException sqle) {
        LOGGER.severe("SQL exception getting plate count: " + sqle);
      }
    }
    return plate_setPlateCount;
  }

  public Set<Integer> getAllPlateIDsForPlateSetID(int _plate_set_id) {
    int plate_set_id = _plate_set_id;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT plate_id  FROM  plate_plate_set WHERE plate_plate_set.plate_set_id = ?;");

      pstmt.setInt(1, plate_set_id);
      ResultSet rs = pstmt.executeQuery();
      Set<Integer> all_plate_ids = new HashSet<>();

      while (rs.next()) {
        all_plate_ids.add(rs.getInt(1));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }

      rs.close();
      pstmt.close();
      return all_plate_ids;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate count: " + sqle);
    }

    return null;
  }

  /**
   * Needed when grouping plate sets. Calls public Set<Integer> getAllPlateIDsForPlateSetID(int
   * _plate_set_id) to get the plate IDs for each plate.
   *
   * @param HashMap<String, String> holds the map: "plate_set_sys_name"="number of plates"
   */
  public Set<Integer> getAllPlateIDsForMultiplePlateSetSysNames(
      HashMap<String, String> _plate_set_sys_names) {
    // for each plate_set_sys_name get the plate_set.id
    HashMap<String, String> plate_set_sys_names = _plate_set_sys_names;
    Set<Integer> plate_set_IDs = new TreeSet<Integer>();
    //LOGGER.info("plate_set_sys_names: " + plate_set_sys_names);

    Iterator it = plate_set_sys_names.entrySet().iterator();
    while (it.hasNext()) {
      HashMap.Entry pair = (HashMap.Entry) it.next();
      //  LOGGER.info("id: " + this.getIDForSysName((String) pair.getKey(), "plate_set"));

      plate_set_IDs.add(this.getIDForSysName((String) pair.getKey(), "plate_set"));
      it.remove(); // avoids a ConcurrentModificationException
    }
    Set<Integer> plate_IDs = new TreeSet<Integer>();
    for (int i : plate_set_IDs) {
      // get the plate_ids for one plate_set
      Set<Integer> one_plate_sets_plate_ids = this.getAllPlateIDsForPlateSetID(i);
      for (int j : one_plate_sets_plate_ids) {

        plate_IDs.add(j);
      }
    }
    return plate_IDs;
  }


    
  /**
   * ******************************************************************
   *
   * <p>Plate related
   *
   * <p>****************************************************************
   */
  public ComboItem[] getPlateTypes() {
    ComboItem[] results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
 
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT id, plate_type_name from plate_type;");

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }

      // LOGGER.info("Description: " + results);
      rs.close();
      pstmt.close();
      results = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return results;
  }


  public ComboItem[] getUserGroups() {
    ComboItem[] results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
 
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT id, usergroup from lnuser_groups;");

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }

      // LOGGER.info("Description: " + results);
      rs.close();
      pstmt.close();
      results = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return results;
  }

    
  /** To reduce traffic, hardcode as the formats are unlikely to change */
    /*
  public int getPlateFormatID(String _format) {
    int results = 0;
    switch (_format) {
      case "96":
        results = 96;
        break;
      case "384":
        results = 2;
        break;
      case "1536":
        results = 3;
        break;
    }
    return results;
  }
    */
    
  public int getPlateTypeID(String _type) {
    int results = 0;

    try {
      String query =
          new String("SELECT id FROM plate_type WHERE plate_type_name = '" + _type + "';");
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      rs.next();
      results = rs.getInt("id");
      //LOGGER.info("ID: " + results);

      rs.close();
      st.close();
    } catch (SQLException sqle) {
      LOGGER.warning("Failed to properly prepare  prepared statement: " + sqle);
    }
    return results;
  }

  public int getIDForPlateType(String _plate_type) {
    String plate_type = _plate_type;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT plate_type.id FROM   plate_type WHERE plate_type_name = ?;");

      pstmt.setString(1, plate_type);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int plateTypeID = Integer.valueOf(rs.getString("id"));

      // LOGGER.info("result: " + plateTypeID);
      rs.close();
      pstmt.close();
      return plateTypeID;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_id: " + sqle);
    }
    int dummy = -1;
    return dummy;
  }

    
  public int getIDForSysName(String _sys_name, String _entity) {
    String sys_name = _sys_name;
    String entity = _entity;

    String custom_statement = new String();

    int id = 0;
    //  "SELECT plate_set.id FROM   plate_set WHERE plate_set_sys_name = ?;");

    switch (entity) {
      case "project":
        custom_statement = "SELECT project.id FROM project WHERE project_sys_name =?;";
        break;
      case "plate_set":
        custom_statement = "SELECT plate_set.id FROM plate_set WHERE plate_set_sys_name =?;";
        break;
      case "plate":
        custom_statement = "SELECT plate.id FROM plate WHERE plate_sys_name =?;";
        break;
      case "sample":
        custom_statement = "SELECT sample.id FROM sample WHERE sample_sys_name =?;";
        break;
      case "":
        custom_statement = "SELECT p FROM p WHERE _sys_name =?;";
        break;
    }

    try {
      PreparedStatement pstmt = conn.prepareStatement(custom_statement);
      pstmt.setString(1, sys_name);
      //  LOGGER.info("prepared statement: " + pstmt);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      id = rs.getInt("id");

      // LOGGER.info("result: " + id);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_id: " + sqle);
    }
    return id;
  }

  /**
   * @param _sys_names array of system_names
   * @param _table table to be queried
   * @param _column name of the sys_name column e.g. plate_sys_name, plate_set_sys_name
   */
  public Integer[] getIDsForSysNames(String[] _sys_names, String _table, String _column) {
    String[] sys_names = _sys_names;
    String table = _table;
    String column = _column;
    Integer[] sys_ids = new Integer[sys_names.length];

    String sqlstring = "SELECT get_ids_for_sys_names (?, ?, ?);";
    // LOGGER.info("SQL at getIDsForSysNames: " + sqlstring);

    try {
      PreparedStatement preparedStatement =
          conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setArray(1, conn.createArrayOf("VARCHAR", sys_names));
      preparedStatement.setString(2, table);
      preparedStatement.setString(3, column);

      preparedStatement.execute(); // executeUpdate expects no returns!!!

      ResultSet resultSet = preparedStatement.getResultSet();
      resultSet.next();
      sys_ids = (Integer[]) (resultSet.getArray("get_ids_for_sys_names")).getArray();

      // LOGGER.info("resultset: " + result);

    } catch (SQLException sqle) {
      LOGGER.warning("SQLE at getIDsForSysNames: " + sqle);
    }

    return sys_ids;
  }

  public int getIDforLayoutName(String _layout_name) {
    String layout_name = _layout_name;
    int id = 0;

    String sqlstring = "SELECT id FROM plate_layout_name WHERE name = ?;";
    //   LOGGER.info("SQL : " + sqlstring);

    try {
      PreparedStatement preparedStatement =
          conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, layout_name);

      preparedStatement.executeQuery(); // executeUpdate expects no returns!!!

      ResultSet resultSet = preparedStatement.getResultSet();
      resultSet.next();
      id = resultSet.getInt("id");

      // LOGGER.info("resultset: " + result);

    } catch (SQLException sqle) {
      LOGGER.warning("SQLE at getIDforLayoutName: " + sqle);
    }

    return id;
  }

    
  public ComboItem[] getAssayTypes() { 
    ComboItem[] results = null;
     ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
 
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("select id, assay_type_name from assay_type;");

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
	  combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
      }

      rs.close();
      pstmt.close();
         results = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return results;
  }

  public ComboItem[] getPlateLayoutNames(int format_id) {
    ComboItem[] output = null;
    Array results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "select id, name from plate_layout_name WHERE plate_format_id = ?;");
      pstmt.setInt(1, format_id);

      ResultSet rs = pstmt.executeQuery();
      //rs.next();
 while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }


      rs.close();
      pstmt.close();
      output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return (ComboItem[])output;
  }


    /**
     * Called from DialogRearrayHitList.  Need only source layouts for a given format;
     * If the destination plate is an assay plate (plate_type_id=1), replicate info must be provided
     */   
    public ComboItem[] getSourcePlateLayoutNames(int format_id, int plate_type_id) {
    ComboItem[] output = null;
    Array results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "select id, name, descr from plate_layout_name WHERE plate_format_id = ? AND source_dest='source';");
      pstmt.setInt(1, format_id);

      ResultSet rs = pstmt.executeQuery();
      //rs.next();
 while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
     if(plate_type_id==1){
	 combo_items.add(new ComboItem(rs.getInt(1), new String(rs.getString(2) + ";" + rs.getString(3) )));	 
     }else{
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
     }
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }


      rs.close();
      pstmt.close();
      output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return (ComboItem[])output;
  }

    /**
     * Get the layout name and id for the plate set reformat dialog box
     */
  public ComboItem getPlateLayoutNameAndID(int _plate_layout_name_id) {
    ComboItem output = null;
    Array results = null;
    //LOGGER.info("plate_layout_name_id: " + _plate_layout_name_id);
    // ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "select id, sys_name, name, descr from plate_layout_name WHERE plate_layout_name.id = ?;");
      pstmt.setInt(1, _plate_layout_name_id);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      // while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
     output =new ComboItem(rs.getInt(1), new String(rs.getString(2) + ";" + rs.getString(3)+";" +rs.getString(4)    ));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
     //}


      rs.close();
      pstmt.close();
      //output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return output;
  }

    /**
     * Called from DialogReformatPlateSet
     * @param _source_id
     * @param _source_reps   not needed
     * @param _target_reps   not needed
     */
  
    public ComboItem[] getLayoutDestinationsForSourceID(int _source_id, int  _source_reps, int _target_reps) {
    ComboItem[] output = null;
    Array results = null;
    int source_id = _source_id;
    int source_reps = _source_reps;
    int target_reps = _target_reps;
    String replication = String.valueOf(source_reps) + "S" + String.valueOf(target_reps) + "T";
    //LOGGER.info(replication);
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
        "select id, sys_name, name, descr FROM plate_layout_name, layout_source_dest WHERE layout_source_dest.src = ? AND layout_source_dest.dest = plate_layout_name.id AND plate_layout_name.descr=?;");
      				
      //        "select id, sys_name, name, descr FROM plate_layout_name, layout_source_dest WHERE layout_source_dest.src = ? AND layout_source_dest.dest = plate_layout_name.id AND plate_layout_name.replicates = ? AND plate_layout_name.targets = ?;");
      
      pstmt.setInt(1, source_id);
      pstmt.setString(2, replication);
      // pstmt.setInt(3, target_reps);
      
      ResultSet rs = pstmt.executeQuery();
      
 while (rs.next()) {   
     combo_items.add(new ComboItem(rs.getInt(1), new String(rs.getString(2) + ";" + rs.getString(3) + ";" + rs.getString(4)  )));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }

      rs.close();
      pstmt.close();
      output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return (ComboItem[])output;
  }

    /**
     * Provides sample annotation for colorization
     */
  public CustomTable getPlateLayout(int _plate_layout_name_id) {
      CustomTable table = null;;
int plate_layout_name_id = _plate_layout_name_id;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT well_by_col, well_type.name  FROM plate_layout, well_type WHERE plate_layout_name_id = ? AND plate_layout.well_type_id=well_type.id ORDER BY well_by_col;");

      pstmt.setInt(1, plate_layout_name_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      //LOGGER.info("Got plate table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
    }
    return table;
  }


      public CustomTable getSampleReplicatesLayout(int _plate_layout_name_id) {
      CustomTable table = null;
      int plate_layout_name_id = _plate_layout_name_id;
      try {
	  PreparedStatement pstmt =
	      conn.prepareStatement(
				    "SELECT well_by_col, replicates  FROM plate_layout WHERE plate_layout_name_id = ? ORDER BY well_by_col;");

	  pstmt.setInt(1, plate_layout_name_id);
	  ResultSet rs = pstmt.executeQuery();
	  
      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      // LOGGER.info("Got plate table " + table);
      rs.close();
      pstmt.close();
    

      } catch (SQLException sqle) {
	  LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
      }
      return table;
      }

        
      public CustomTable getTargetReplicatesLayout(int _plate_layout_name_id) {
      CustomTable table = null;
int plate_layout_name_id = _plate_layout_name_id;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT well_by_col, target  FROM plate_layout WHERE plate_layout_name_id = ? ORDER BY well_by_col;");

      pstmt.setInt(1, plate_layout_name_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      //  LOGGER.info("Got plate table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
    }
    return table;
  }

    
    public int getPlateLayoutNameIDForPlateSetID(int _plate_set_id){

	int plate_set_id = _plate_set_id;
	int plate_layout_name_id = 0;

	String sqlstring = "SELECT plate_layout_name_id FROM plate_set WHERE id = ?;";
	//	LOGGER.info("SQL : " + sqlstring);

	try {
	    PreparedStatement preparedStatement =
		conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
	    preparedStatement.setInt(1, plate_set_id);

	    preparedStatement.executeQuery(); // executeUpdate expects no returns!!!

	    ResultSet resultSet = preparedStatement.getResultSet();
	    resultSet.next();
	    plate_layout_name_id = resultSet.getInt("plate_layout_name_id");

	    // LOGGER.info("resultset: " + result);

	} catch (SQLException sqle) {
	    LOGGER.warning("SQLE at getPlateLayoutNameIDforPlateSetID: " + sqle);
	}

	return plate_layout_name_id;
    }


    

    public String[][] getWorklist(int _worklist_id){

	int worklist_id = _worklist_id;
	CustomTable ct;
	String sqlstring = "SELECT sample_id, source_plate, source_well, dest_plate, dest_well FROM worklists WHERE rearray_pairs_id = ?;";
	//LOGGER.info("SQL : " + sqlstring);

	try {
	    PreparedStatement preparedStatement =
		conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
	    preparedStatement.setInt(1, worklist_id);
	    preparedStatement.executeQuery(); // executeUpdate expects no returns!!!
	    ResultSet rs = preparedStatement.getResultSet();
	     ResultSetMetaData metaData = rs.getMetaData();

    // names of columns
    Vector<String> columnNames = new Vector<String>();
    int columnCount = metaData.getColumnCount();
    for (int column = 1; column <= columnCount; column++) {
        columnNames.add(metaData.getColumnName(column));
    }

    //set up a row counter which will generate a vector of selected row indices
    //used to select all rows for export to a spreadsheet
    Integer row_counter = 0;
    Vector<Integer> selected_rows = new Vector<Integer>();
    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    while (rs.next()) {
        Vector<Object> vector = new Vector<Object>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
	selected_rows.add(row_counter);
	row_counter = row_counter + 1;
    }

    ct = new CustomTable( dmf, new DefaultTableModel(data, columnNames));
    ct.setSelectedRows(selected_rows);

    return ct.getSelectedRowsAndHeaderAsStringArray();
    
	    // LOGGER.info("resultset: " + result);

	} catch (SQLException sqle) {
	    LOGGER.warning("SQLE at getPlateLayoutNameIDforPlateSetID: " + sqle);
	}
	return null;
    }


    /**
     *  String sql_statement = new String("INSERT INTO temp_accs_id (plate_order, by_col, accs_id) VALUES ");
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
     */

    /**
     * Called from 
     */
   public String[][] getAssayRunData(int  _assay_run_id){

	int assay_run_id = _assay_run_id;
	CustomTable ct;
	String sqlstring = "Select * from get_all_data_for_assay_run(?)";

	//LOGGER.info("SQL : " + sqlstring);

	try {
	    PreparedStatement preparedStatement =
		conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
	    preparedStatement.setInt(1, assay_run_id);
	    preparedStatement.executeQuery(); // executeUpdate expects no returns!!!
	    ResultSet rs = preparedStatement.getResultSet();
	     ResultSetMetaData metaData = rs.getMetaData();

    // names of columns
	     String[] columnNamespre = {"AssayRun","PlateSet","Plate","PlateOrder","Well","WellType","WellNum","Response","Bk_Sub","Norm","NormPos","pEnhanced","Sample","accession","Target"};
	     Vector<String> columnNames = new Vector<String>(Arrays.asList(columnNamespre));
    int columnCount = metaData.getColumnCount();
    /*    for (int column = 1; column <= columnCount; column++) {
        columnNames.add(metaData.getColumnName(column));
	}*/

    //set up a row counter which will generate a vector of selected row indices
    //used to select all rows for export to a spreadsheet
    Integer row_counter = 0;
    Vector<Integer> selected_rows = new Vector<Integer>();
    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    while (rs.next()) {
        Vector<Object> vector = new Vector<Object>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
	selected_rows.add(row_counter);
	row_counter = row_counter + 1;
    }

    ct = new CustomTable( dmf, new DefaultTableModel(data, columnNames));
    ct.setSelectedRows(selected_rows);

    return ct.getSelectedRowsAndHeaderAsStringArray();
    
	    // LOGGER.info("resultset: " + result);

	} catch (SQLException sqle) {
	    LOGGER.warning("SQLE at getPlateLayoutNameIDforPlateSetID: " + sqle);
	}
	return null;
    }

    /**
     * Called from MenuBarForPlateSet; provides underlying data for multi selection
     */
   public String[][] getPlateSetData(String[] _plate_set_id){

	String plate_set_id[] = _plate_set_id;
	CustomTable ct;
	String sqlstring_pre = "SELECT plate_set.plate_set_sys_name as \"Plate Set\", plate.plate_sys_name as \"Plate\", well.by_col as \"Well\", sample.sample_sys_name as \"Sample\", sample.accs_id as \"Accession\"  FROM  plate_plate_set, plate_set, plate, well, well_sample, sample WHERE  plate_plate_set.plate_set_id=plate_set.id AND plate_plate_set.plate_id=plate.ID and plate.id=well.plate_id and well_sample.well_id=well.id and well_sample.sample_id=sample.id  and plate_set.id IN (";

	String sqlstring_mid  = new String();

	for (int i =0; i < plate_set_id.length; i++){
	    sqlstring_mid = sqlstring_mid +  plate_set_id[i] + ",";	    
	}
       
	sqlstring_mid = sqlstring_mid.substring(0,sqlstring_mid.length()-1);
	String sqlstring_post = ") order by plate_set.plate_set_sys_name, plate.plate_sys_name, well.by_col;";
	String sqlstring = sqlstring_pre + sqlstring_mid + sqlstring_post;
	LOGGER.info("SQL : " + sqlstring);

	try {
	    PreparedStatement preparedStatement =
		conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
	    //preparedStatement.setInt(1, plate_set_id);
	    preparedStatement.executeQuery(); // executeUpdate expects no returns!!!
	    ResultSet rs = preparedStatement.getResultSet();
	     ResultSetMetaData metaData = rs.getMetaData();

    // names of columns
    Vector<String> columnNames = new Vector<String>();
    int columnCount = metaData.getColumnCount();
    for (int column = 1; column <= columnCount; column++) {
        columnNames.add(metaData.getColumnName(column));
    }

    //set up a row counter which will generate a vector of selected row indices
    //used to select all rows for export to a spreadsheet
    Integer row_counter = 0;
    Vector<Integer> selected_rows = new Vector<Integer>();
    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    while (rs.next()) {
        Vector<Object> vector = new Vector<Object>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
	selected_rows.add(row_counter);
	row_counter = row_counter + 1;
    }

    ct = new CustomTable( dmf, new DefaultTableModel(data, columnNames));
    ct.setSelectedRows(selected_rows);

    return ct.getSelectedRowsAndHeaderAsStringArray();
    
	    // LOGGER.info("resultset: " + result);

	} catch (SQLException sqle) {
	    LOGGER.warning("SQLE at getPlateLayoutNameIDforPlateSetID: " + sqle);
	}
	return null;
    }


    /*  Before converting to handling int array
    
    public String[][] getAssayRunData(int _assay_run_id){

	int assay_run_id = _assay_run_id;
	CustomTable ct;
	String sqlstring = "SELECT plate_set.plate_set_sys_name as \"Plate SET\", plate.plate_sys_name as \"Plate\", assay_result.well, assay_result.response, assay_result.bkgrnd_sub,   assay_result.norm,  assay_result.norm_pos  FROM assay_result, assay_run, plate_plate_set, plate_set, plate WHERE assay_run.plate_set_id=plate_plate_set.plate_set_id and assay_result.assay_run_id=assay_run.id AND plate_plate_set.plate_order=assay_result.plate_order AND plate_plate_set.plate_set_id=plate_set.id AND plate_plate_set.plate_id=plate.ID and assay_run.id = ?;";
	//LOGGER.info("SQL : " + sqlstring);

	try {
	    PreparedStatement preparedStatement =
		conn.prepareStatement(sqlstring, Statement.RETURN_GENERATED_KEYS);
	    preparedStatement.setInt(1, assay_run_id);
	    preparedStatement.executeQuery(); // executeUpdate expects no returns!!!
	    ResultSet rs = preparedStatement.getResultSet();
	     ResultSetMetaData metaData = rs.getMetaData();

    // names of columns
    Vector<String> columnNames = new Vector<String>();
    int columnCount = metaData.getColumnCount();
    for (int column = 1; column <= columnCount; column++) {
        columnNames.add(metaData.getColumnName(column));
    }

    //set up a row counter which will generate a vector of selected row indices
    //used to select all rows for export to a spreadsheet
    Integer row_counter = 0;
    Vector<Integer> selected_rows = new Vector<Integer>();
    // data of the table
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    while (rs.next()) {
        Vector<Object> vector = new Vector<Object>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            vector.add(rs.getObject(columnIndex));
        }
        data.add(vector);
	selected_rows.add(row_counter);
	row_counter = row_counter + 1;
    }

    ct = new CustomTable( dmf, new DefaultTableModel(data, columnNames));
    ct.setSelectedRows(selected_rows);

    return ct.getSelectedRowsAndHeaderAsStringArray();
    
	    // LOGGER.info("resultset: " + result);

	} catch (SQLException sqle) {
	    LOGGER.warning("SQLE at getPlateLayoutNameIDforPlateSetID: " + sqle);
	}
	return null;
    }

    */
    
    
    
  public int getAssayIDForAssayType(String _assay_name) {
    int result = 0;
    String assay_name = _assay_name;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement("select id from assay_type WHERE assay_type_name = ?;");
      pstmt.setString(1, assay_name);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt("id");
      // LOGGER.info("Description: " + results);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting assay_type_id: " + sqle);
    }
    return result;
  }

  public int getPlateLayoutIDForPlateLayoutName(String _plate_layout_name) {
    int result = 0;
    String plate_layout_name = _plate_layout_name;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement("select id from plate_layout_name WHERE name = ?;");
      pstmt.setString(1, plate_layout_name);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt("id");
      // LOGGER.info("resuklt plate layout name: " + result);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting assay_type_id: " + sqle);
    }
    return result;
  }
    /**
     * pgPL/SQL: get_number_samples_for_psid( _psid INTEGER )
     */

    public int getNumberOfSamplesForPlateSetID(int _plate_set_id){
	int result = 0;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT  get_number_samples_for_psid(?);");
      pstmt.setInt(1, _plate_set_id);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt(1);
      LOGGER.info("number of samples: " + result);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting number of samples: " + sqle);
    }
    return result;
	
    }

      public ComboItem[] getPlateFormats() {
    ComboItem[] output = null;
    Array results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "select id, format from plate_format;");
    

      ResultSet rs = pstmt.executeQuery();
      //rs.next();
 while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }


      rs.close();
      pstmt.close();
      output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plate types: " + sqle);
    }
    return (ComboItem[])output;
  }

      public ComboItem[] getHitListsForProject(int _project_id) {
	  int project_id = _project_id;
    ComboItem[] output = null;
    Array results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    try {
      PreparedStatement pstmt =
          conn.prepareStatement( "SELECT hit_list.id, hit_list.hitlist_sys_name   FROM assay_run, plate_set, hit_list WHERE hit_list.assay_run_id= assay_run.id AND assay_run.plate_set_id= plate_set.ID AND plate_set.project_id=?;");

      pstmt.setInt(1, project_id);

      ResultSet rs = pstmt.executeQuery();
      //rs.next();
 while (rs.next()) {
     //all_plate_ids.add(rs.getInt(1));
	combo_items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        // LOGGER.info("A plate set ID: " + rs.getInt(1));
      }


      rs.close();
      pstmt.close();
      output = combo_items.toArray(new ComboItem[combo_items.size()]);

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting hit lists: " + sqle);
    }
    return (ComboItem[])output;
  }

    
    public int getNumberOfReplicatesForPlateLayout(int _selected_layout_id){
	int selected_layout_id = _selected_layout_id;
	int result = 0;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT  replicates FROM plate_layout_name WHERE plate_layout_name.id=?;");
      pstmt.setInt(1, selected_layout_id);

      ResultSet rs = pstmt.executeQuery();
      rs.next();
      result = rs.getInt(1);
      LOGGER.info("number of replicates for layout name: " + result);
      rs.close();
      pstmt.close();

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting number of samples: " + sqle);
    }
    return result;
	
    }

    /**
     * Called from Scatterplot.java
     * Called from DatabaseInserter.associateDataWithPlateSet()
     */
    public CustomTable  getDataForScatterPlot(int _assay_run_id){
	CustomTable table;
	int assay_run_id = _assay_run_id;
	//ArrayList result = new ArrayList();
 try {
      PreparedStatement pstmt =
          conn.prepareStatement("SELECT * FROM get_scatter_plot_data(?);");
      pstmt.setInt(1, _assay_run_id);

      ResultSet rs = pstmt.executeQuery();
     
      table = new CustomTable(dmf, dbm.buildTableModel(rs));
	
           rs.close();
      pstmt.close();
	return table;
      
    
    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting number of samples: " + sqle);
    }
 
 return null;
    }


  public CustomTable getAssayRuns(int _project_id) {
      CustomTable table = null;;
int project_id = _project_id;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(

				"SELECT assay_run.assay_run_sys_name AS \"Assay Run\", assay_run.assay_run_name AS \"Name\", assay_run.descr AS \"Description\", assay_type.assay_type_name AS \"Assay Type\", plate_set.plate_set_sys_name AS \"Plate Set\"  FROM assay_run, plate_set, assay_type WHERE assay_run.assay_type_id=assay_type.id AND assay_run.plate_set_id= plate_set.ID AND plate_set.project_id=?;");

      pstmt.setInt(1, project_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      //LOGGER.info("Got assay run table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
    }
    return table;
  }
   
  public CustomTable getHitLists(int _project_id) {
      CustomTable table = null;;
int project_id = _project_id;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
      "SELECT hit_list.hitlist_sys_name AS \"Hit List\", hit_list.hitlist_name AS \"Name\", hit_list.descr AS \"Description\", assay_run.assay_run_sys_name AS \"Assay Run\", hit_list.n AS \"Count\"  FROM assay_run, plate_set, hit_list WHERE hit_list.assay_run_id= assay_run.id AND assay_run.plate_set_id= plate_set.ID AND plate_set.project_id=?;");

      
      pstmt.setInt(1, project_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      //LOGGER.info("Got assay run table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
    }
    return table;
  }

    public CustomTable getHitListsForAssayRun(int _assay_run_id) {
      CustomTable table = null;;
int assay_run_id = _assay_run_id;
    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
      "SELECT hit_list.hitlist_sys_name AS \"Hit List\", hit_list.hitlist_name AS \"Name\", hit_list.descr AS \"Description\", assay_run.assay_run_sys_name AS \"Assay Run\", hit_list.n AS \"Count\"  FROM assay_run, plate_set, hit_list WHERE hit_list.assay_run_id= assay_run.id AND assay_run.plate_set_id= plate_set.ID AND assay_run.id=?;");

      
      pstmt.setInt(1, assay_run_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      //LOGGER.info("Got assay run table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout table: " + sqle);
    }
    return table;
  }





    
    /**
     * Get the source data for LayoutViewer
     */
  public CustomTable getSourceForLayout(int _format) {
      int format = _format;
      CustomTable table = null;;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
     
      "select sys_name AS \"ID\",plate_format_id AS \"Format\", NAME AS \"Description\", descr AS \"Reps\",   use_edge AS \"Edge\", num_controls AS \"Controls\", control_loc AS \"Location\" from plate_layout_name WHERE source_dest = 'source' AND plate_format_id=?;");
 
      pstmt.setInt(1, format);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      // LOGGER.info("Got layout sources table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout sources table: " + sqle);
    }
    return table;
  }

      public CustomTable getDestForLayout(int _source_layout_id) {
	  int source_layout_id = _source_layout_id;
      CustomTable table = null;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
"select plate_layout_name.sys_name AS \"ID\",plate_layout_name.plate_format_id AS \"Format\", plate_layout_name.name AS \"Description\", plate_layout_name.descr AS \"Reps\",   plate_layout_name.use_edge AS \"Edge\", plate_layout_name.num_controls AS \"Controls\", plate_layout_name.control_loc AS \"Location\" from plate_layout_name, layout_source_dest WHERE layout_source_dest.src= ? AND layout_source_dest.dest = plate_layout_name.id;");
 
      pstmt.setInt(1, source_layout_id);
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      // LOGGER.info("Got layout sources table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve plate_layout sources table: " + sqle);
    }
    return table;
  }

    /**
     * Determine the number of hits (sample IDs) per plate set
     * called from HitListViewer.java
     */
    public CustomTable getHitCountPerPlateSet(int _project_id, int _hit_list_id) {
	  int project_id = _project_id;
	  int hit_list_id = _hit_list_id;
      CustomTable table = null;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
"SELECT MAX(plate_set.id) as \"ID\", plate_set.plate_set_sys_name as \"Plate Set\", MAX(plate_type.plate_type_name) as \"Type\", MAX(plate_set.plate_format_id) as \"Format\", COUNT(sample.ID) as \"Count\" FROM plate_set, plate_plate_set, plate_type, plate, well, well_sample, sample WHERE plate_plate_set.plate_set_id=plate_set.ID AND plate_plate_set.plate_id=plate.id AND plate_set.plate_type_id = plate_type.id   and well.plate_id=plate.ID AND well_sample.well_id=well.ID AND well_sample.sample_id= sample.ID  AND sample.id  IN (SELECT  sample.id FROM hit_list, hit_sample, plate_set, assay_run, sample WHERE hit_sample.hitlist_id=hit_list.id  AND hit_sample.sample_id=sample.id  and assay_run.plate_set_id=plate_set.id AND   hit_list.assay_run_id=assay_run.id   AND  hit_sample.hitlist_id IN (SELECT hit_list.ID FROM hit_list, assay_run WHERE hit_list.assay_run_id=assay_run.ID AND hit_list.id= ? and assay_run.ID IN (SELECT assay_run.ID FROM assay_run WHERE assay_run.plate_set_id IN (SELECT plate_set.ID FROM plate_set WHERE plate_set.project_id=?)))) GROUP BY plate_set.plate_set_sys_name;");
 
      pstmt.setInt(1, hit_list_id);
      pstmt.setInt(2, project_id);
      
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      // LOGGER.info("Got layout sources table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve hit list counts table: " + sqle);
    }
    return table;
  }

    /**
     * Content for the samples list in the Hit List Viewer
     */
        public CustomTable getSamplesForHitList( int _hit_list_id) {
	
	  int hit_list_id = _hit_list_id;
      CustomTable table = null;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
"SELECT hit_sample.hitlist_id AS \"Hit List\", sample.ID AS \"Sample\", sample.sample_sys_name AS \"Sample Name\", sample.accs_id AS \"Accession\" FROM hit_sample, sample WHERE hit_sample.hitlist_id=? AND hit_sample.sample_id=sample.id;");
 
      pstmt.setInt(1, hit_list_id);
      
      ResultSet rs = pstmt.executeQuery();

      table = new CustomTable(dmf, dbm.buildTableModel(rs));
      // LOGGER.info("Got layout sources table " + table);
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve hit list counts table: " + sqle);
    }
    return table;
  }

    public int getNumberOfPlatesForPlateSetID(int _plate_set_id){

	int plate_set_id = _plate_set_id;
	int num_plates = 0;
	  try {
      PreparedStatement pstmt =
          conn.prepareStatement(
"SELECT num_plates FROM plate_set WHERE plate_set.ID = ?;");
 
      pstmt.setInt(1, plate_set_id);
      
      ResultSet rs = pstmt.executeQuery();

          rs.next();
      num_plates = rs.getInt(1);
 
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve hit list counts table: " + sqle);
    }
    return num_plates;
	
    }

        public int getFormatForPlateSetID(int _plate_set_id){

	int plate_set_id = _plate_set_id;
	int format_id = 0;
	  try {
      PreparedStatement pstmt =
          conn.prepareStatement(
"SELECT plate_format_id FROM plate_set WHERE plate_set.ID = ?;");
 
      pstmt.setInt(1, plate_set_id);
      
      ResultSet rs = pstmt.executeQuery();

          rs.next();
      format_id = rs.getInt(1);
 
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve hit list counts table: " + sqle);
    }
    return format_id;
	
    }

    public int getUnknownCountForLayoutID(int _layout_id){
	int layout_id = _layout_id;
	int unknown_count = 0;
	  try {
      PreparedStatement pstmt =
          conn.prepareStatement(
				"SELECT unknown_n FROM plate_layout_name WHERE plate_layout_name.ID = ?;");
 
      pstmt.setInt(1, layout_id);
      
      ResultSet rs = pstmt.executeQuery();

          rs.next();
      unknown_count = rs.getInt(1);
 
      rs.close();
      pstmt.close();
    

    } catch (SQLException sqle) {
 LOGGER.severe("Failed to retrieve unknown count: " + sqle);
    }
    return unknown_count;

	
    }

      public DefaultTableModel buildTableModel(ResultSet _rs) {

    try {
      ResultSet rs = _rs;
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();

      Vector<Vector<Object>> data = new Vector<Vector<Object>>();
      Vector<String> columnNames = new Vector<String>();
      /*
      String[] columnNames = new String[columnCount];
      for (int column = 0; column < columnCount; column++) {
        columnNames[column] = metaData.getColumnName(column + 1);
      }
      */
      for (int column = 0; column < columnCount; column++) {
        columnNames.addElement(metaData.getColumnName(column + 1));
      }

      // data of the table
      while (rs.next()) {
        Vector<Object> vector = new Vector<Object>();

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
          vector.add(rs.getObject(columnIndex + 1));
        }
        data.add(vector);
      }
      // LOGGER.info("data: " + data);
      return new DefaultTableModel(data, columnNames);

      //          data.stream().map(List::toArray).toArray(Object[][]::new), columnNames);

    } catch (SQLException sqle) {
      LOGGER.severe("SQLException in buildTableModel: " + sqle);
    }

    return null;
  }


  public int getUserIDForSessionID(int _session_id) {
    int session_id = _session_id;

    try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT lnuser_id FROM lnsession WHERE lnsession.id = ?;");

      pstmt.setInt(1, session_id);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int user_id = Integer.valueOf(rs.getString("id"));

      // LOGGER.info("result: " + plateTypeID);
      rs.close();
      pstmt.close();
      return user_id;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_id: " + sqle);
    }
    int dummy = -1;
    return dummy;
  }

    public int getPlateSetOwnerID( int _plate_set_id){
	
	int plate_set_id = _plate_set_id;
	
	try {
      PreparedStatement pstmt =
          conn.prepareStatement(
              "SELECT lnuser_id FROM lnuser, lnsession, plate_set WHERE plate_set.lnsession_id = lnsession.id AND lnsession.lnuser_id = lnuser.id AND  plate_set.id = ?;");

      pstmt.setInt(1, plate_set_id);
      ResultSet rs = pstmt.executeQuery();
      rs.next();
      int owner_id = Integer.valueOf(rs.getString("lnuser_id"));

      // LOGGER.info("result: " + plateTypeID);
      rs.close();
      pstmt.close();
      return owner_id;

    } catch (SQLException sqle) {
      LOGGER.severe("SQL exception getting plateset_id: " + sqle);
    }
    int dummy = -1;
    return dummy;

	
    }
    
}
