package lnrocks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
/**
 * parentPane BorderLayout holds other panes
 * pane2 GridBagLayout holds dropdowns
 * pane3  BorderLayout holds source table
 * pane4  BorderLayout holds destination table
 * pane5 BorderLayout holds layout table
 */


public class DialogImportLayoutViewer extends JDialog implements java.awt.event.ActionListener, DocumentListener {
  static JButton cancel_button;
  static JButton ok_button;
  static JLabel label;

    final DialogMainFrame dmf;
    // final Session session;
    private String owner;
  private JTable table;
  private JTable sourceTable;
  private JTable destTable;
    private JTextField nameField;
    private JTextField descriptionField;
    private JLabel num_controls_label;
    private JLabel num_unk_label;
    private JTextField control_location;
    
  private JScrollPane scrollPane;
  final Instant instant = Instant.now();
    
    private  JPanel parentPane;
    private  JPanel pane1;
    private  JPanel pane2;
    private  JPanel pane5;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private final  DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  // final EntityManager em;
    private static final long serialVersionUID = 1L;
    private MyModel tableModel;
    private Object[][] grid_data;
    private int format;
    private int num_controls;
    private int num_unknowns;
    private int num_edge;
    private IFn require = Clojure.var("clojure.core", "require");
     
    
    // private DefaultComboBoxModel<ComboItem> layout_names_list_model;


    
    public DialogImportLayoutViewer(DialogMainFrame dmf, ArrayList<String[]> _data) {
	dbm = _dbm;
	this.dmf = dmf;
	//   this.session = dmf.getSession();
 require.invoke(Clojure.read("lnrocks.core"));
    IFn getUser = Clojure.var("lnrocks.core", "get-user");
    
    owner = (String)getUser.invoke();

 ArrayList<String[]> data = _data;
	Object[][] temp_data = dmf.getUtilities().getObjectArrayForArrayList(data);
	Object[][] grid_data = dmf.getUtilities().getPlateLayoutGrid(temp_data);

	format = data.size()-1;  //the length of the imput ArrayList

	if( format != 96 && format != 384 && format != 1536 ){
	    JOptionPane.showMessageDialog(this,
					  "Found " + String.valueOf(format) + " rows of data.\nNeed 96, 384 or 1536.",
					  "Import error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	}
	
	num_controls = dmf.getUtilities().getNumberOfControls(temp_data);
	num_unknowns = dmf.getUtilities().getNumberOfUnknowns(temp_data);
	num_edge = dmf.getUtilities().getNumberOfEdge(temp_data);

	
    parentPane = new JPanel(new BorderLayout());
    parentPane.setBorder(BorderFactory.createRaisedBevelBorder());
 
    this.setTitle("Import Plate Layout");
    // c.gridwidth = 2;
   ////////////////////////////////////////////////////////////
    //Pane 1

    pane1 = new JPanel(new GridBagLayout());
    pane1.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
  


    label = new JLabel("Layout Name:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.insets = new Insets(5, 5, 2, 2);
    pane1.add(label, c);

    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    pane1.add(label, c);
   
    label = new JLabel("# controls:");
    c.gridx = 0;
    c.gridy = 4;
    c.anchor = GridBagConstraints.LINE_END;
    pane1.add(label, c);

        num_controls_label = new JLabel(String.valueOf(num_controls));   
    c.gridwidth = 1;
    c.gridx = 1;
    c.gridy = 4;
    c.anchor = GridBagConstraints.LINE_START;
    pane1.add(num_controls_label, c);

        label = new JLabel("       # unknowns:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 4;
    c.anchor = GridBagConstraints.LINE_END;
     pane1.add(label, c);

        num_unk_label = new JLabel(String.valueOf(num_unknowns));
    c.gridwidth = 1;
    c.gridx = 3;
    c.gridy = 4;
       c.anchor = GridBagConstraints.LINE_START;
       pane1.add(num_unk_label, c);

       label = new JLabel("       Format:");
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 4;
    c.gridy = 4;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane1.add(label, c);


    
    label = new JLabel(String.valueOf(format));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 5;
    c.gridy = 4;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane1.add(label, c);


    nameField = new JTextField(30);
    //nameField.setText(_name);
    c.gridwidth = 5;
    c.gridx = 1;
    c.gridy = 2;
    pane1.add(nameField, c);
    nameField.getDocument().addDocumentListener(this);

    descriptionField = new JTextField(30);
    descriptionField.setText("1S1T");
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    pane1.add(descriptionField, c);
    descriptionField.getDocument().addDocumentListener(this);

        label = new JLabel("Control Location:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane1.add(label, c);

    control_location = new JTextField(30);
    //descriptionField.setText(_description);
    c.gridx = 1;
    c.gridy = 5;
       c.gridwidth = 5;
 
    c.gridheight = 1;
    pane1.add(control_location, c);
    control_location.getDocument().addDocumentListener(this);



    
    ok_button = new JButton("Proceed with import");
    ok_button.setMnemonic(KeyEvent.VK_P);
    ok_button.setEnabled(false);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 6;
    c.gridy = 2;
    c.gridwidth = 1;
    c.gridheight = 1;
    ok_button.addActionListener(this);
    pane1.add(ok_button, c);
    ok_button.addActionListener(
        (new ActionListener() {
		 public void actionPerformed(ActionEvent e) {
		//temp_data is Object[][] of 2 columns, well, type, both integers
		     dbm.getDatabaseInserter().importPlateLayout(temp_data, nameField.getText(), descriptionField.getText(), control_location.getText(),  Integer.parseInt(num_controls_label.getText()), Integer.parseInt(num_unk_label.getText()), format, num_edge);
            dispose();
          }
        }));

    cancel_button = new JButton("Cancel");
    cancel_button.setMnemonic(KeyEvent.VK_C);
    cancel_button.setActionCommand("cancel");
    cancel_button.setEnabled(true);
    cancel_button.setForeground(Color.RED);
    c.gridx = 6;
    c.gridy = 3;
    pane1.add(cancel_button, c);
    cancel_button.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));

    ////////////////////////////////////////////////////////////
   ////////////////////////////////////////////////////////////
    //Pane 2   top pane with info

        pane2 = new JPanel(new GridBagLayout());
    pane2.setBorder(BorderFactory.createRaisedBevelBorder());

    label = new JLabel("Date:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);
    
    label = new JLabel(df.format(java.util.Date.from(instant)));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);


    
    ////////////////////////////////////////////////////////////
   ////////////////////////////////////////////////////////////
    //Pane 5

     pane5 = new JPanel(new BorderLayout());
    pane5.setBorder(BorderFactory.createRaisedBevelBorder());
      javax.swing.border.TitledBorder layoutBorder = BorderFactory.createTitledBorder("Layout: ");

    layoutBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    pane5.setBorder(layoutBorder);    
	tableModel = new MyModel(grid_data);
	
	//LOGGER.info("griddata length: " + gridData.length + "  " + gridData[0].length  );
	table = new JTable( tableModel);
	javax.swing.table.JTableHeader header = table.getTableHeader();
	header.setBackground(java.awt.Color.DARK_GRAY);
	header.setForeground(java.awt.Color.WHITE);
	table.setDefaultRenderer(String.class, new MyRenderer());
	scrollPane = new JScrollPane(table);
	JTable rowTable = new RowNumberTable(table);
	scrollPane.setRowHeaderView(rowTable);
	scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
			     rowTable.getTableHeader());
	pane5.add(scrollPane, BorderLayout.CENTER);
	table.setFillsViewportHeight(true);
	pane5.revalidate();
	pane5.repaint();
    
	//  this.refreshLayoutTable(1);
   
    this.getContentPane().add(parentPane, BorderLayout.CENTER);
    parentPane.add(pane5, BorderLayout.CENTER);
    parentPane.add(pane1, BorderLayout.SOUTH);
    parentPane.add(pane2, BorderLayout.NORTH);
    
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }


    public void actionPerformed(ActionEvent e) {

    }

    /*
    public void refreshLayoutTable(int _plate_layout_id){
	
	pane5.removeAll();
	
	CustomTable  table2 = null;
	switch(displayList.getSelectedIndex()){
	case 0:
	table2 = session.getDatabaseRetriever().getPlateLayout(_plate_layout_id);
	    break;
	case 1:
	table2 = session.getDatabaseRetriever().getSampleReplicatesLayout(_plate_layout_id);
	    break;
	case 2:
	table2 = session.getDatabaseRetriever().getTargetReplicatesLayout(_plate_layout_id);
	    break;

	    
	}

	gridData =  dmf.getUtilities().getPlateLayoutGrid(table2);
	
	tableModel = new MyModel(grid_data);
	
	//LOGGER.info("griddata length: " + gridData.length + "  " + gridData[0].length  );
	table = new JTable( tableModel);
	javax.swing.table.JTableHeader header = table.getTableHeader();
	header.setBackground(java.awt.Color.DARK_GRAY);
	header.setForeground(java.awt.Color.WHITE);
	table.setDefaultRenderer(String.class, new MyRenderer());
	scrollPane = new JScrollPane(table);
	JTable rowTable = new RowNumberTable(table);
	scrollPane.setRowHeaderView(rowTable);
	scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
			     rowTable.getTableHeader());
	pane5.add(scrollPane, BorderLayout.CENTER);
	table.setFillsViewportHeight(true);
	pane5.revalidate();
	pane5.repaint();
    
    }
    */
    private static class MyRenderer extends DefaultTableCellRenderer {
     private static final long serialVersionUID = 1L;
	
     //Color backgroundColor = getBackground();

        @Override
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            MyModel model = (MyModel) table.getModel();
	    //LOGGER.info("6,11: " + (String)model.getValueAt(row,column));
	    switch((String)model.getValueAt(row,column)){
	    case "unknown":
		c.setBackground(java.awt.Color.WHITE);
		break;
	    case "blank":
		c.setBackground(java.awt.Color.LIGHT_GRAY);
		break;
	    case "positive":
		c.setBackground(java.awt.Color.GREEN);
		break;
	    case "negative":
		c.setBackground(java.awt.Color.RED);
		break;
	    case "edge":
		c.setBackground(new java.awt.Color(51,204,255)); //LIGHT_BLUE
		break;
	    case "1":
		c.setBackground(java.awt.Color.BLACK);
		break;
	    case "2":
		c.setBackground(java.awt.Color.WHITE);
		break;
	    case "3":
		c.setBackground(java.awt.Color.BLUE);
		break;
	    case "4":
		c.setBackground(new java.awt.Color(51,204,255));
		break;
	    case "a":
		c.setBackground(java.awt.Color.BLACK);
		break;
	    case "b":
		c.setBackground(java.awt.Color.WHITE);
		break;
	    case "c":
		c.setBackground(java.awt.Color.BLUE);
		break;
	    case "d":
		c.setBackground(new java.awt.Color(51,204,255));
		break;
	    }
	    
            return c;
        }
    }

      private static class MyModel extends AbstractTableModel {

	  //private final List<Row> list = new ArrayList<Row>();
	  private static final long serialVersionUID = 1L;
	  private static final String[] COLUMN_NAMES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48"};
	  Object[][] data;
	  
        public MyModel(Object[][] _data) {
	    this.data = _data;
	     fireTableDataChanged();
            //list.add(new Row("One", true));
            //list.add(new Row("Two", false));
            //list.add(new Row("Three", false));
        }


	  public void setBackgroundColor(int row, int col){
	      
	  }
        public boolean getState(int row) {
            //return list.get(row).state.booleanValue();
	    return false;
        }

        public void setState(int row, boolean state) {
            //list.get(row).state = state;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return data[0].length;
        }

	  public String getColumnName(int column){
         return COLUMN_NAMES[column];
    }
	  
        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
	    //  list.get(row).name = (String) aValue;
            //fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        private static class Row {

            private String name;
            private Boolean state;

            public Row(String name, Boolean state) {
                this.name = name;
                this.state = state;
            }
        }
    }

      public void insertUpdate(DocumentEvent e) {
	  if(nameField.getText().length() >0 && descriptionField.getText().length() >0 && control_location.getText().length() >0){
	      ok_button.setEnabled(true);
	  }else{
	      ok_button.setEnabled(false);
	  }
    }
    public void removeUpdate(DocumentEvent e) {
	  if(nameField.getText().length() >0 && descriptionField.getText().length() >0 && control_location.getText().length() >0 ){
	      ok_button.setEnabled(true);
	  }else{
	      ok_button.setEnabled(false);
	  }

    }
    public void changedUpdate(DocumentEvent e) {
        //Plain text components do not fire these events
    }


    
}
