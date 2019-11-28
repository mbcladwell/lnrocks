package lnrocks;

import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class CustomTable extends JTable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  private DialogMainFrame dmf;
  private DefaultTableModel tableModel;
  private ListSelectionModel listSelectionModel;
  private SharedListSelectionHandler sharedListSelectionHandler;
  private Vector<Integer> selectedRows = new Vector<>();
  private TableColumnModel tableColumnModel;

  public CustomTable(DialogMainFrame _dmf, DefaultTableModel _tm) {
    super(_tm);

    dmf = _dmf;
    tableModel = _tm;
    tableColumnModel = this.getColumnModel();
    listSelectionModel = this.getSelectionModel();
    sharedListSelectionHandler = new SharedListSelectionHandler();
    listSelectionModel.addListSelectionListener(sharedListSelectionHandler);
    this.setSelectionModel(listSelectionModel);
    this.setRowSelectionAllowed(true);

    this.getColumnModel().getColumn(0).setMaxWidth(75);
    //this.getColumnModel().getColumn(1).setMaxWidth(150);
    //    this.getColumnModel().getColumn(2).setMaxWidth(100);

    this.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
  }

  public DefaultTableModel getTableModel() {
    return this.tableModel;
  }

  public String[][] getSelectedRowsAndHeaderAsStringArray() {

    int colCount = tableModel.getColumnCount();
    int rowCount = selectedRows.size();
    //    	LOGGER.info("colCount: " + colCount + " rowCount:  " + rowCount);
    
    String[][] results = new String[rowCount + 1][colCount];
    for (int i = 0; i < colCount; i++) {
      results[0][i] = this.getColumnName(i);
      //	LOGGER.info("ij: " + results[0][i]);
    }

    for (int i = 1; i <= rowCount; i++) { //start at 1; 0 holds the header
	for (int j = 0; j < colCount; j++) {
	    try{
		results[i][j] = tableModel.getValueAt(selectedRows.get(i-1), j).toString();  
		
	    }catch(NullPointerException e){
		
	    }

	    
	}
    }
	return results;
    }

    /**
     * Used to select all rows when querying a worklist
     */
    public void setSelectedRows( Vector<Integer> _selected_rows) {
	
	selectedRows = _selected_rows;
    }

  class SharedListSelectionHandler implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      listSelectionModel.removeListSelectionListener(sharedListSelectionHandler);

      boolean isAdjusting = e.getValueIsAdjusting();
      if (!isAdjusting) {
        selectedRows.clear();
        // Find out which indexes are selected.
        int minIndex = listSelectionModel.getMinSelectionIndex();
        int maxIndex = listSelectionModel.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) {
          if (listSelectionModel.isSelectedIndex(i)) {
            selectedRows.add(CustomTable.this.convertRowIndexToModel(i));
            //  LOGGER.info("Selected: " + CustomTable.this.convertRowIndexToModel(i));
          }
        }
      }

      listSelectionModel.addListSelectionListener(sharedListSelectionHandler);
    }
  }
}

// https://stackoverflow.com/questions/2668547/stackoverflowerror-being-caused-by-a-tablemodellistener

// https://stackoverflow.com/questions/10679425/multiple-row-selection-with-checkbox-in-jtable
