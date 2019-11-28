package lnrocks;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Utilities {

  private DialogMainFrame dmf;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private File file;
  private static final String newline = "\n";
  private JFileChooser fc;

  public Utilities(DialogMainFrame _dmf) {
    dmf = _dmf;
  }
  /** Returns selected rows only */
  public Object[][] getTableDataWithHeader(CustomTable _table) {
    CustomTable table = _table;

    DefaultTableModel tm = table.getTableModel();

    int nRow = tm.getRowCount() + 1, nCol = tm.getColumnCount();
    Object[][] tableData = new Object[nRow][nCol];

    for (int i = 0; i < nCol; i++) {
      tableData[0][i] = table.getColumnName(i);
    }

    for (int i = 1; i < nRow; i++)
      for (int j = 0; j < nCol; j++) tableData[i][j] = tm.getValueAt(i - 1, j);
    return tableData;
  }

  /**
   * Take a columnar data file and load it into an ArrayList of String[]. Header is included. This
   * is called by OK button of DialogAddPlateSetData.
   */
  public ArrayList<String[]> loadDataFile(String _fileName) {
    String fileName = _fileName;
    Path path = Paths.get(fileName);
    ArrayList<String[]> table = new ArrayList<String[]>();

    try (Stream<String> lines = Files.lines(path)) {
      for (String line : (Iterable<String>) lines::iterator) {
        if (line != null && !line.isEmpty()) {
          String[] values = line.split("\t");

          // LOGGER.info("values: " + values);
          table.add(values);
        }
      }
    } catch (IOException ioe) {
      LOGGER.severe("IOException: " + ioe);
    }

    return table;
  }

  public int[] getIntArrayForIntegerSet(Set<Integer> input) {
    int count = input.size();
    Integer[] inputArray = input.toArray(new Integer[count]);
    int result[] = new int[count];

    for (int i = 0; i < count; i++) {
      result[i] = inputArray[i];
    }
    return result;
  }

  public String[] getStringArrayForStringSet(Set<String> input) {
    LOGGER.info("input: " + input);
    int count = input.size();
    String[] inputArray = input.toArray(new String[count]);
    String result[] = new String[count];

    for (int i = 0; i < count; i++) {
      result[i] = inputArray[i];
      LOGGER.info("result: " + result[i]);
    }
    return result;
  }

    public Object[][] getPlateLayoutGrid(CustomTable _custom_table){
	CustomTable custom_table = _custom_table;
    DefaultTableModel tm = custom_table.getTableModel();
    int row = 0;
    int col = 0;
    int nRow = tm.getRowCount();
    LOGGER.info("nRow: " + nRow);
    //96 well plate  tableData[row][col]
    switch(nRow){
    case 96:
    row = 8; col=12;
    break;
case 384:
    row = 16; col=24;
    break;
case 1536:
    row = 32; col=48;
    break;    
    }
        Object[][] tableData = new Object[row][col];


    for (int i = 0; i < nRow; i++) {
	//LOGGER.info("tdata i: " + i + " " + tm.getValueAt(i,1) );
	//System.out.println("i: " + i + " array[" + ((i)%8) +"][" + (int)Math.floor((i)/8)+ "] = " + tm.getValueAt(i,1) );
       
       	tableData[((i)%row)][(int)Math.floor((i)/row)] = tm.getValueAt(i,1);
    }

    return tableData;	
    }


 public Object[][] getPlateLayoutGrid(Object[][] _import_data){
     Object[][] import_data = _import_data;
    
    int row = 0;
    int col = 0;
    int nRow = import_data.length;
    //LOGGER.info("nRow: " + nRow);
    //96 well plate  tableData[row][col]
    switch(nRow){
    case 97:
    row = 8; col=12;
    break;
case 385:
    row = 16; col=24;
    break;
case 1537:
    row = 32; col=48;
    break;    
    }
        String[][] tableData = new String[row][col];


    for (int i = 1; i < nRow; i++) {
	//LOGGER.info("tdata i: " + i + " " + tm.getValueAt(i,1) );
	//System.out.println("i: " + i + " array[" + ((i)%8) +"][" + (int)Math.floor((i)/8)+ "] = " + import_data[i][1] );
	switch(Integer.parseInt((String)import_data[i][1])){
	case 1:
       	tableData[((i-1)%row)][(int)Math.floor((i-1)/row)] = "unknown";
	    break;
	case 2:
       	tableData[((i-1)%row)][(int)Math.floor((i-1)/row)] = "positive";
	    break;
	case 3:
       	tableData[((i-1)%row)][(int)Math.floor((i-1)/row)] = "negative";
	    break;
	case 4:
       	tableData[((i-1)%row)][(int)Math.floor((i-1)/row)] = "blank";
	    break;
	case 5:
       	tableData[((i-1)%row)][(int)Math.floor((i-1)/row)] = "edge";
	    break;
	}
    }

    return (Object[][])tableData;	
    }



    
    
    /**
     * Converts ArrayList of String[] to Object[][].
     */
    public Object[][] getObjectArrayForArrayList(ArrayList<String[]> _array_list){
	ArrayList<String[]> array_list = _array_list;
	int nrow = array_list.size();
	int ncol = array_list.get(1).length;
	Object[][] results = new Object[nrow][ncol];
	
	int counter = 0;
	for (String[] row : array_list) {
	    for(int i=0; i < ncol; i++){
		results[counter][i] = row[i];
		//LOGGER.info("results  counter(row): " + counter + " column " + i + " element " + results[counter][i] + "\n");
	
	}
    counter++;
	}

	return results;
    }

    /**
     * Convert a columnar table with well identifiers to a column/row table object.  The Object array is filled by columns.  Header will be stripped.  Column_name is the column to be converted to a plate layout.
     * @param int _format 96, 384, or 1536
@param String column_name column to be processed
     */
    public Object[][] convertTableToPlate(Object[][] _input,  String _column_name){
       
   
	String column_name = _column_name;
	int column_of_interest = 0;
	Object[][] input = _input;
	
	int row = 0;
	int col = 0;
	int nRow = input.length-1;
	    
	// LOGGER.info("nRow: " + nRow);
    //96 well plate  tableData[row][col]
    switch(nRow){
    case 96:
    row = 8; col=12;
    break;
case 384:
    row = 16; col=24;
    break;
case 1536:
    row = 32; col=48;
    break;    
    }
        Object[][] output = new Object[row][col];

	//which column to process?
	for(int i=0; i < input[0].length; i++){
	    if(input[0][i].equals(column_name)){
		column_of_interest = i;
		//LOGGER.info("column of interest: " + i);
		break;
	    }
	}

    for (int i = 1; i < nRow; i++) {
	//LOGGER.info("tdata i: " + i + " " + tm.getValueAt(i,1) );
	System.out.println("i: " + i + " array[" + ((i)%8) +"][" + (int)Math.floor((i)/8)+ "] = " + input[i][column_of_interest]);
       	//tableData[((i)%row)][(int)Math.floor((i)/row)] = tm.getValueAt(i,1);
	
	output[((i)%row)][(int)Math.floor((i)/row)] = input[i][column_of_interest];
	//LOGGER.info("Object row: " + i + " " + tm.getValueAt(i,1) );
    }

    return output;	
       
	
    }


    /**
     * @param default_table_model table with selected rows
     * returns a string array of selected data
     */
     public String[][] getSelectedRowsAndHeaderAsStringArray(JTable _table) {
     JTable table = _table;
     DefaultTableModel model = (DefaultTableModel) table.getModel();
    int colCount = model.getColumnCount();
    int rowCount = table.getSelectedRowCount();
    	LOGGER.info("rowCount: " + rowCount);
    int[] selected_rows = table.getSelectedRows();
    
    String[][] results = new String[rowCount + 1][colCount];
    for (int i = 0; i < colCount; i++) {
		LOGGER.info("ij: " + results[0][i]);
      results[0][i] = table.getColumnName(i);
    }

    for (int i = 1; i <= rowCount; i++) { //start at 1; 0 holds the header
      for (int j = 0; j < colCount; j++) {
	  if(model.getValueAt(selected_rows[i-1], j) != null){ //accessions might be null
	      results[i][j] = model.getValueAt(selected_rows[i-1], j).toString();
	  }
      }
    }
    return results;
  }

    /**
     * @param data  [well][type] both ints
     * 1: unknown; 2,3,4 are controls; 5 is edge
     */
    
    public int getNumberOfControls(Object[][] _data){
	Object[][] data = _data;
	int len = data.length;
	int counter = 0;
	for(int i =1; i < len; i++){
	    if( Integer.parseInt((String)data[i][1]) == 2 || Integer.parseInt((String)data[i][1]) == 3 || Integer.parseInt((String)data[i][1]) == 4 ) counter++;
	}
	    
	return counter;	
    }
    

    /**
     * @param data  [well][type] both ints
     * 1: unknown; 2,3,4 are controls; 5 is edge
     */
    public int getNumberOfUnknowns(Object[][] _data){
	Object[][] data = _data;
	int len = data.length;
	int counter = 0;
	
	for(int i =1; i < len; i++){
	    if( Integer.parseInt((String)data[i][1]) == 1 ) counter++;
	}
	    
	return counter;	
    }

    /**
     * @param data  [well][type] both ints
     * 1: unknown; 2,3,4 are controls; 5 is edge
     */
    public int getNumberOfEdge(Object[][] _data){
	Object[][] data = _data;
	int len = data.length;
	int counter = 0;
	
	for(int i =1; i < len; i++){
	    if( Integer.parseInt((String)data[i][1]) == 5 ) counter++;
	}
	    
	return counter;	
    }


    
}
  
