package lnrocks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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


public class LayoutViewer extends JDialog implements java.awt.event.ActionListener {
  static JButton button;
  static JButton helpButton;
  static JButton repsButton;
  static JLabel label;
  static JComboBox<Integer> formatList;
  static JComboBox<String> displayList;
    //static JComboBox<ComboItem> layoutList;
  static JButton okButton;
  static JButton cancelButton;
  final DialogMainFrame dmf;
    // final Session session;
    private String owner;
  private JTable table;
  private JTable sourceTable;
  private JTable destTable;
    
  private JScrollPane scrollPane;
  private JScrollPane sourceScrollPane;
  private JScrollPane destScrollPane;
    
    private  JPanel parentPane;
    private  JPanel pane2;
    private  JPanel pane3;
    private  JPanel pane4;
    private  JPanel pane5;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  // final EntityManager em;
  private static final long serialVersionUID = 1L;
  private MyModel tableModel;
    private ComboItem [] layoutNames;
    private Object[][] gridData;
    private DatabaseManager dbm; 
    // private DefaultComboBoxModel<ComboItem> layout_names_list_model;
    private IFn require = Clojure.var("clojure.core", "require");
    private IFn openHelpPage = Clojure.var("ln.session", "open-help-page");


    
  public LayoutViewer(DatabaseManager _dbm) {
      dbm = _dbm;
      this.dmf = dbm.getDialogMainFrame();
      // this.session = dmf.getSession();
    require.invoke(Clojure.read("ln.codax-manager"));
       IFn getUser = Clojure.var("ln.codax-manager", "get-user");
  
       owner = (String)getUser.invoke();
    //layoutNames = session.getDatabaseManager().getDatabaseRetriever().getPlateLayoutNames(96);
    //    layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutNames );
    
    parentPane = new JPanel(new BorderLayout());
    parentPane.setBorder(BorderFactory.createRaisedBevelBorder());
 
    this.setTitle("Plate Layout Viewer");
    // c.gridwidth = 2;

////////////////////////////////////////////////////////////
    //Pane 2
    
    JPanel pane2 = new JPanel(new GridBagLayout());
    pane2.setBorder(BorderFactory.createRaisedBevelBorder());
    GridBagConstraints c = new GridBagConstraints();

    
    label = new JLabel("Format:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    pane2.add(label, c);
 
    Integer[] formats = {96, 384, 1536};

    formatList = new JComboBox<Integer>(formats);
    formatList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    formatList.addActionListener(this);
    pane2.add(formatList, c);
 
    label = new JLabel("Display:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    pane2.add(label, c);
 
    String[] displayOptions = {"samples", "sample replicates", "target replicates"};

    displayList = new JComboBox<String>(displayOptions);
    displayList.setSelectedIndex(0);
    c.gridx = 3;
    c.gridy = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    displayList.addActionListener(this);
    pane2.add(displayList, c);
 
    helpButton = new JButton("Layout Help");
    c.gridx = 4;
    c.gridy = 1;
    c.gridheight = 1;
    helpButton.addActionListener(this);
    pane2.add(helpButton, c);

    repsButton = new JButton("Replicates Help");
    c.gridx = 5;
    c.gridy = 1;
    c.gridheight = 1;
    repsButton.addActionListener(this);
    pane2.add(repsButton, c);

    

    ////////////////////////////////////////////////////////////
    //Pane 3
    pane3  = new JPanel(new BorderLayout());
    pane3.setBorder(BorderFactory.createRaisedBevelBorder());
   javax.swing.border.TitledBorder sourceLayoutBorder = BorderFactory.createTitledBorder("Source:");
    sourceLayoutBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    pane3.setBorder(sourceLayoutBorder);
    sourceTable = dbm.getDatabaseRetriever().getSourceForLayout(96);
    sourceTable.getSelectionModel().addListSelectionListener(						     
	  new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
		  ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		  if(!e.getValueIsAdjusting() & sourceTable.getSelectedRow()>=0){
		      //LOGGER.info("source: " + e);
			int row = sourceTable.getSelectedRow();
			
			int source_layout_id = Integer.parseInt(( (String)sourceTable.getModel().getValueAt(row,0)).substring(4));
			//LOGGER.info("source_layout_id: " + source_layout_id);
			destTable.setModel(dbm
					   .getDatabaseRetriever()
					   .getDestForLayout(source_layout_id).getModel());
			refreshLayoutTable(source_layout_id);

		  }
	      }
	  });
    sourceScrollPane = new JScrollPane(sourceTable);
    sourceTable.setFillsViewportHeight(true);
    pane3.add( sourceScrollPane, BorderLayout.CENTER);


    
    
    ////////////////////////////////////////////////////////////
    //Pane 4
  
     pane4 = new JPanel(new BorderLayout());
    pane4.setBorder(BorderFactory.createRaisedBevelBorder());
      javax.swing.border.TitledBorder destLayoutBorder = BorderFactory.createTitledBorder("Destination: ");

    destLayoutBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    pane4.setBorder(destLayoutBorder);
    destTable = dbm.getDatabaseRetriever().getDestForLayout(1);
    destTable.getSelectionModel().addListSelectionListener(						     
	  new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
		  if(!e.getValueIsAdjusting() & destTable.getSelectedRow()>=0){
		  ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		   	LOGGER.info("dest source: " + e);
			int row = destTable.getSelectedRow();
			
			int dest_layout_id = Integer.parseInt(( (String)destTable.getModel().getValueAt(row,0)).substring(4));
			LOGGER.info("dest_source_layout_id: " + dest_layout_id);
			refreshLayoutTable(dest_layout_id);
		  }
	      }
	  });
    destScrollPane = new JScrollPane(destTable);
    destTable.setFillsViewportHeight(true);
    pane4.add( destScrollPane, BorderLayout.CENTER);


   ////////////////////////////////////////////////////////////
    //Pane 5

     pane5 = new JPanel(new BorderLayout());
    pane5.setBorder(BorderFactory.createRaisedBevelBorder());
      javax.swing.border.TitledBorder layoutBorder = BorderFactory.createTitledBorder("Layout: ");

    layoutBorder.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    pane5.setBorder(layoutBorder);    
    
    this.refreshLayoutTable(1);
   
    this.getContentPane().add(parentPane, BorderLayout.CENTER);
    parentPane.add(pane2, BorderLayout.NORTH);
    parentPane.add(pane3, BorderLayout.WEST);
    pane3.setPreferredSize(new Dimension(500,200));    
    parentPane.add(pane4, BorderLayout.EAST);
    pane4.setPreferredSize(new Dimension(500,200));    
    parentPane.add(pane5, BorderLayout.SOUTH);
    pane5.setPreferredSize(new Dimension(500,600));    
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }


    public void actionPerformed(ActionEvent e) {

    if (e.getSource() == formatList) {
      
	sourceTable.setModel(dbm
			     .getDatabaseRetriever()
			     .getSourceForLayout((int)formatList.getSelectedItem()).getModel());
      
 	
    }

    if (e.getSource() == helpButton) {
	      openHelpPage.invoke( "layouts");
     }

     if (e.getSource() == repsButton) {
	      openHelpPage.invoke( "replication");

    }

    
    }
    
    public void refreshLayoutTable(int _plate_layout_id){
	pane5.removeAll();
	CustomTable  table2 = null;
	switch(displayList.getSelectedIndex()){
	case 0:
	table2 = dbm.getDatabaseRetriever().getPlateLayout(_plate_layout_id);
	    break;
	case 1:
	table2 = dbm.getDatabaseRetriever().getSampleReplicatesLayout(_plate_layout_id);
	    break;
	case 2:
	table2 = dbm.getDatabaseRetriever().getTargetReplicatesLayout(_plate_layout_id);
	    break;

	    
	}
	gridData =  dmf.getUtilities().getPlateLayoutGrid(table2);
	tableModel = new MyModel(gridData);
	
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
	    //switch((String)model.getValueAt(row,column)){
	    switch(model.getValueAt(row,column).toString()){
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
		c.setForeground(java.awt.Color.WHITE);
		break;
	    case "2":
		c.setBackground(java.awt.Color.WHITE);
		c.setForeground(java.awt.Color.BLACK);
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

}
