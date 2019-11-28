package lnrocks;

import java.util.*;
import java.util.logging.*;
import javax.swing.event.*;
import javax.swing.table.*;

class CustomTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  Object[][] data;
  String[] columnNames;

  public CustomTableModel(Object[][] _data, String[] _columnNames) {
    data = _data;
    columnNames = _columnNames;
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return data.length;
  }

  public String getColumnName(int col) {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col) {
    return data[row][col];
  }

  /*
   * JTable uses this method to determine the default renderer/
   * editor for each cell.  If we didn't implement this method,
   * then the last column would contain text ("true"/"false"),
   * rather than a check box.
   */
  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  public boolean isCellEditable(int row, int col) {
    // Note that the data/cell address is constant,
    // no matter where the cell appears onscreen.
    return false;
  }
}
