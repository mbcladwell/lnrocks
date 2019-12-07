package lnrocks;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class AssayRunViewer extends JDialog implements java.awt.event.ActionListener {
  static JButton hitListFromFile;
  static JButton exportAssayRun;
  static JButton viewAssayRun;
  static JButton exportHitListTable;
  static JButton viewHitList;
  static JButton refreshHitListsButton;
    
  static JLabel label;
  static JComboBox<ComboItem> projectList;
  final DialogMainFrame dmf;
    private DatabaseManager dbm;
    //    final Session session;
    private int project_id;
    private String owner;
  private JTable assay_runs_table;
  private JTable hit_lists_table;
  private JScrollPane assay_runs_scroll_pane;
    private JScrollPane hit_lists_scroll_pane;
    private  JPanel parent_pane;
    private  JPanel assay_runs_pane;
    private  JPanel hit_lists_pane;
    private JPanel arButtons;
    private JPanel hlButtons;
    private JPopupMenu popup;
    
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  // final EntityManager em;
  private static final long serialVersionUID = 1L;
        private IFn require = Clojure.var("clojure.core", "require");

    
  public AssayRunViewer(DialogMainFrame dmf) {
      dbm = _dbm;
    this.setTitle("Assay Run Viewer");
    this.dmf = dmf;
    require.invoke(Clojure.read("lnrocks.core"));

    //    this.session = dmf.getSession();
     IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");
     //cannot be (Integer)    ...intValue()
     // project is long, others are int??
     System.out.println((getProjectID.invoke()).getClass());
     //System.out.println(((Integer)getProjectID.invoke()).getClass());
     //     System.out.println(((Long)getProjectID.invoke()).getClass());
     
     project_id = ((Long)getProjectID.invoke()).intValue();
      IFn getUser = Clojure.var("lnrocks.core", "get-user");
   
    owner = (String)getUser.invoke();

    parent_pane = new JPanel(new BorderLayout());

    assay_runs_pane = new JPanel(new BorderLayout());
    assay_runs_pane.setBorder(BorderFactory.createRaisedBevelBorder());
    javax.swing.border.TitledBorder assay_runs_pane_border = BorderFactory.createTitledBorder("Assay Runs:");
    assay_runs_pane_border.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    assay_runs_pane.setBorder(assay_runs_pane_border);

    //this failed as Integer
    //getAssayRuns requires and int
    assay_runs_table = dbm.getDatabaseRetriever().getAssayRuns(((Long)getProjectID.invoke()).intValue());
  assay_runs_table.getSelectionModel().addListSelectionListener(						     
	  new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
		  ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		  if(!e.getValueIsAdjusting() & assay_runs_table.getSelectedRow()>=0){
		      //LOGGER.info("source: " + e);
			int row = assay_runs_table.getSelectedRow();
			int assay_run_id = Integer.parseInt(( (String)assay_runs_table.getModel().getValueAt(row,0)).substring(3));

			//LOGGER.info("source_layout_id: " + source_layout_id);
			hit_lists_table.setModel(dbm
					   .getDatabaseRetriever()
						.getHitListsForAssayRun(assay_run_id).getModel());
			//
			//refreshLayoutTable(source_layout_id);

		  }
	      }
	  });



    
    assay_runs_scroll_pane = new JScrollPane(assay_runs_table);
    assay_runs_table.setFillsViewportHeight(true);
    assay_runs_pane.add(assay_runs_scroll_pane, BorderLayout.CENTER);

    GridLayout buttonLayout = new GridLayout(1,4,5,5);
    projectList = new JComboBox<ComboItem>(dbm.getDatabaseRetriever().getAllProjects());
    for(int i=0; i < projectList.getItemCount(); i++){
	if(((ComboItem)projectList.getItemAt(i)).getKey() == project_id){
		projectList.setSelectedIndex(i);
	    }
    }

    
    //projectList.setSelectedIndex(9);
    projectList.addActionListener(this);
    exportAssayRun = new JButton("Export");
    exportAssayRun.addActionListener(this);   
    hitListFromFile = new JButton("New Hit List from file");
    hitListFromFile.addActionListener(this);
    viewAssayRun = new JButton("Plot");
    viewAssayRun.addActionListener(this);
    arButtons = new JPanel(buttonLayout);
    arButtons.add(projectList);
    arButtons.add(exportAssayRun);
    arButtons.add(hitListFromFile);
    arButtons.add(viewAssayRun);
    assay_runs_pane.add(arButtons, BorderLayout.SOUTH);    

    hit_lists_pane  = new JPanel(new BorderLayout());
    hit_lists_pane.setBorder(BorderFactory.createRaisedBevelBorder());
    javax.swing.border.TitledBorder hit_lists_pane_border = BorderFactory.createTitledBorder("Hit Lists:");
    hit_lists_pane_border.setTitlePosition(javax.swing.border.TitledBorder.TOP);
    hit_lists_pane.setBorder(hit_lists_pane_border);
    //failed as Long
    // getHitLists requires int
    hit_lists_table = dbm.getDatabaseRetriever().getHitLists(((Long)getProjectID.invoke()).intValue());

    hit_lists_scroll_pane = new JScrollPane(hit_lists_table);
    hit_lists_table.setFillsViewportHeight(true);
    hit_lists_pane.add(hit_lists_scroll_pane, BorderLayout.CENTER);

    hlButtons = new JPanel(buttonLayout);
    refreshHitListsButton = new JButton("Refresh List");
    refreshHitListsButton.addActionListener(this);
   
    exportHitListTable = new JButton("Export Table");
    exportHitListTable.addActionListener(this);
   viewHitList = new JButton("View");
     viewHitList.addActionListener(this);
   hlButtons.add(refreshHitListsButton);
    
    hlButtons.add(exportHitListTable);
    hlButtons.add(viewHitList);
    hit_lists_pane.add(hlButtons, BorderLayout.SOUTH);


    this.getContentPane().add(parent_pane, BorderLayout.CENTER);
    parent_pane.add(assay_runs_pane, BorderLayout.WEST);
    parent_pane.add(hit_lists_pane, BorderLayout.EAST);
    
    GridBagConstraints c = new GridBagConstraints();
   
    
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

    public void actionPerformed(ActionEvent e) {
    if (e.getSource() == exportAssayRun) {
	popup = new JPopupMenu();      
	JMenuItem menuItem = new JMenuItem("Selected rows this table");            
	menuItem.setMnemonic(KeyEvent.VK_R);
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object[][] results = dmf.getUtilities().getSelectedRowsAndHeaderAsStringArray(assay_runs_table);
		    if(results.length>1){
			   LOGGER.info("hit list table: " + results);
			POIUtilities poi = new POIUtilities(dbm);
			poi.writeJTableToSpreadsheet("Assay Runs", results);
			try {
			    Desktop d = Desktop.getDesktop();
			    d.open(new File("./Writesheet.xlsx"));
			} catch (IOException ioe) {
			}	 
		    }else{
			JOptionPane.showMessageDialog(dmf, "Select one or more  Assay Runs!");	
		    }   
		}
	    });
    popup.add(menuItem);    
    menuItem = new JMenuItem("Underlying data");		   
    menuItem.setMnemonic(KeyEvent.VK_D);
    menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		if(!assay_runs_table.getSelectionModel().isSelectionEmpty()){
		    try{
			int row = assay_runs_table.getSelectedRow();
			int assay_run_id = Integer.parseInt(( (String)assay_runs_table.getModel().getValueAt(row,0)).substring(3));

			/*
		    Object[][] results = dmf.getUtilities().getSelectedRowsAndHeaderAsStringArray(assay_runs_table);
		    if(results.length>1){
			String[] assay_run_ids = new String[results.length];
			try{
			    //this plans ahead to accept an array, but all must be same format
			    //stick to one for now
			    for(int i=0; i < results.length-1; i++){
				int modelRow =  assay_runs_table.convertRowIndexToModel(i);
			    assay_run_ids[i] =  assay_runs_table.getModel().getValueAt(modelRow, 0).toString().substring(3);
			 LOGGER.info("modelRow: " + modelRow);
			   LOGGER.info("full string: " + assay_runs_table.getModel().getValueAt(modelRow, 0).toString());
			  
			    LOGGER.info("assay_run_ids[i]; i: " + i + ": " + assay_run_ids[i]);
			 
			    }
			    //just work with the first one
			    int assay_run_id = Integer.parseInt(assay_run_ids[0]);
			*/
			    Object[][] assay_run_data = dbm.getDatabaseRetriever().getAssayRunData(assay_run_id);
			    POIUtilities poi = new POIUtilities(dbm);
			    
			    poi.writeJTableToSpreadsheet("Assay Run Data", assay_run_data);
			    //poi.writeJTableToSpreadsheet("Assay Run Data for " + assay_runs_sys_name, assay_run_data);
		
			    Desktop d = Desktop.getDesktop();
			    d.open(new File("./Writesheet.xlsx"));
			}catch(IOException ioe){
			    JOptionPane.showMessageDialog(dmf, "Assay Run has no data!");   
			}    
		    }else{
			JOptionPane.showMessageDialog(dmf, "Select one or more  Assay Runs!");	
		    }
		}
	    }
        );
    popup.add(menuItem);
    popup.show(exportAssayRun, 0, 0);
    popup.setVisible(true);
    }

    if (e.getSource() == hitListFromFile) {
	if(!assay_runs_table.getSelectionModel().isSelectionEmpty()){
		 
	    TableModel arModel = assay_runs_table.getModel();
	    int row = assay_runs_table.getSelectedRow();
	    String assay_runs_sys_name =  assay_runs_table.getModel().getValueAt(row, 0).toString();
	    int  assay_runs_id = Integer.parseInt(assay_runs_sys_name.substring(3));

	    JFileChooser fileChooser = new JFileChooser();
	    int returnVal = fileChooser.showOpenDialog(dmf);
	
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
		java.io.File file = fileChooser.getSelectedFile();
		Vector<String> s_ids = new Vector<String>();
		BufferedReader reader;
		try {
		    reader = new BufferedReader(new FileReader(file));
		    String line = reader.readLine();
		    line = reader.readLine(); //skip the first header line
		    while (line != null & !line.equals("")) {
			s_ids.add(line);
			// read next line
			line = reader.readLine();
		    }
		    reader.close();
		    dbm.getDatabaseInserter().insertHitListFromFile(assay_runs_id, s_ids);
		} catch (IOException ioe) {
		    ioe.printStackTrace();
		}
        // This is where a real application would open the file.
	    }
	} else{
	    JOptionPane.showMessageDialog(dmf, "Select an Assay Run!");	      
	}	

    }
    
    if (e.getSource() == viewAssayRun) {
	if(!assay_runs_table.getSelectionModel().isSelectionEmpty()){
		 
	    TableModel arModel = assay_runs_table.getModel();
	    int row = assay_runs_table.getSelectedRow();
	    String assay_runs_sys_name =  assay_runs_table.getModel().getValueAt(row, 0).toString();
	    int  assay_runs_id = Integer.parseInt(assay_runs_sys_name.substring(3));
	    new ScatterPlot(dbm, assay_runs_id);
	}
	else{
	    JOptionPane.showMessageDialog(dmf, "Select an Assay Run!");	      
	}	
    }
    
    if (e.getSource() == exportHitListTable) {
	
	Object[][] results = dmf.getUtilities().getSelectedRowsAndHeaderAsStringArray(hit_lists_table);
	if(results.length>1){
	//   LOGGER.info("hit list table: " + results);
	       POIUtilities poi = new POIUtilities(dbm);
            poi.writeJTableToSpreadsheet("Hit Lists", results);
            try {
              Desktop d = Desktop.getDesktop();
              d.open(new File("./Writesheet.xlsx"));
            } catch (IOException ioe) {
            }	 
	
	}else{
	    JOptionPane.showMessageDialog(dmf, "Select one or more  Hit Lists!");	
	}
    	
    }
    if (e.getSource() == viewHitList) {
  if(!hit_lists_table.getSelectionModel().isSelectionEmpty()){
	 TableModel arModel = hit_lists_table.getModel();
		 int row = hit_lists_table.getSelectedRow();
		 String hit_list_sys_name =  hit_lists_table.getModel().getValueAt(row, 0).toString();
		 int  hit_list_id = Integer.parseInt(hit_list_sys_name.substring(3));
		 new HitListViewer( dbm, hit_list_id);}
  else{
	      JOptionPane.showMessageDialog(dmf, "Select a Hit List!");	      
	    }

	
    }
        if (e.getSource() == refreshHitListsButton) {
	    refreshHitListsTable();
    }


    if (e.getSource() == projectList) {
	if(projectList.getSelectedIndex() > -1){
	    project_id  = ((ComboItem)projectList.getSelectedItem()).getKey();
	    IFn setProjectID = Clojure.var("lnrocks.core", "set-project-id");

	    setProjectID.invoke(project_id);
	    IFn setProjectSysName = Clojure.var("lnrocks.core", "set-project-sys-name");
    
	    setProjectSysName.invoke(((ComboItem)projectList.getSelectedItem()).toString());
	    this.refreshTables(); 
	}
    }  
  }

    public void refreshTables(){
       
	CustomTable arTable = dbm.getDatabaseRetriever().getAssayRuns(project_id);
	TableModel arModel = arTable.getModel();
	assay_runs_table.setModel(arModel);	

		//LOGGER.info("project: " + project_id);
	CustomTable hlTable = dbm.getDatabaseRetriever().getHitLists(project_id);
	TableModel hlModel = hlTable.getModel();
	hit_lists_table.setModel(hlModel);
	
    }

    public void refreshHitListsTable(){
	int selected_project_id = ((ComboItem)projectList.getSelectedItem()).getKey();
	CustomTable hlTable = dbm.getDatabaseRetriever().getHitLists(selected_project_id);
	TableModel hlModel = hlTable.getModel();
	hit_lists_table.setModel(hlModel);


    }
	

 
}
