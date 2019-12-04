package lnrocks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class FilterPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private JTable table;
    private JScrollPane scrollPane;
    private DialogMainFrame dmf;
    private DatabaseManager dbm;
    private JPanel textPanel;
    private JButton clearButton;
    private JButton refreshButton;
    private int entity_type;
    private int id;
    private JTextField textField;
    //    private Session session;    
    private IFn require = Clojure.var("clojure.core", "require");

  /**
   * @param id the project/plateset/plate etc id for fetching the main table
 */
    public FilterPanel(DialogMainFrame _dmf, JTable _table, int _id, int _entity_type) {
	
    this.setLayout(new GridBagLayout());
    
    dmf = _dmf;
    //session = parent.getSession();
    require.invoke(Clojure.read("lnrocks.core"));
   
    table = _table;
    id = _id;
    entity_type = _entity_type;

    TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(table.getModel());
    table.setRowSorter(rowSorter);

    GridBagConstraints c = new GridBagConstraints();
    /*
    JLabel label = new JLabel("Filter:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    this.add(label, c);
    */
    refreshButton = new JButton("Refresh");
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    refreshButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      System.out.println("entity: " + entity_type);
	      IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
	      IFn getPlateSetSysName = Clojure.var("lnrocks.core", "get-plate-set-sys-name");
	      IFn getPlateSysName = Clojure.var("lnrocks.core", "get-plate-sys-name");
	      switch(entity_type){
	      case DialogMainFrame.PROJECT: dmf.showProjectTable();
		  break;
	      case DialogMainFrame.PLATESET: dmf.showPlateSetTable((String)getProjectSysName.invoke());
		  break;
	      case DialogMainFrame.PLATE: dmf.showPlateTable((String)getPlateSetSysName.invoke());
		  break;
		  //      case DialogMainFrame.WELL: dbm.getDialogMainFrame().showWellTable();
	      }
	      
	      //  JTable table2 =  dbm.getDatabaseRetriever().getDMFTableData(id, entity_type );
	      // TableModel model = table2.getModel();
	      // table.setModel(model);
          }
        });
    this.add(refreshButton, c);
 
    clearButton = new JButton("Filter:");
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    clearButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            textField.setText("");
            clearButton.setText("Filter:");
          }
        });
    this.add(clearButton, c);
    
    textField = new JTextField(60);
    c.gridx = 2;
    c.gridy = 0;
    c.weightx = 0.9;
    c.anchor = GridBagConstraints.LINE_START;
    c.insets = new Insets(5, 5, 2, 2);
    this.add(textField, c);

    textField
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {

              public void changedUpdate(DocumentEvent e) {
                String text = textField.getText();
                clearButton.setText("Clear:");

                if (text.trim().length() == 0) {
                  rowSorter.setRowFilter(null);
                } else {
                  rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
              }

              public void removeUpdate(DocumentEvent e) {
                String text = textField.getText();
                clearButton.setText("Clear:");

                if (text.trim().length() == 0) {
                  rowSorter.setRowFilter(null);
                } else {
                  rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
              }

              public void insertUpdate(DocumentEvent e) {
                String text = textField.getText();
                clearButton.setText("Clear:");

                if (text.trim().length() == 0) {
                  rowSorter.setRowFilter(null);
                } else {
                  rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
              }
            });

  }


}
