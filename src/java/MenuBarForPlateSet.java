package lnrocks;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import clojure.java.api.Clojure;
import clojure.lang.IFn;



public class MenuBarForPlateSet extends JMenuBar {

  DialogMainFrame dmf;
   // 
    CustomTable plate_set_table;
    // Session session;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public MenuBarForPlateSet(DialogMainFrame _dmf, CustomTable _plate_set_table){
    
      dmf = _dmf;
    plate_set_table = _plate_set_table;
    //session = dmf.getSession();
       IFn require = Clojure.var("clojure.core", "require");
    require.invoke(Clojure.read("lnrocks.core"));

    JMenu menu = new JMenu("Plate Set");
    menu.setMnemonic(KeyEvent.VK_P);
    menu.getAccessibleContext().setAccessibleDescription("Menu items related to plate sets");
    this.add(menu);

    // a group of JMenuItems
    JMenuItem menuItem = new JMenuItem("Add plate set", KeyEvent.VK_A);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Launch the Add Plate Set dialog.");
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            new DialogAddPlateSet(dmf);
          }
        });
    menu.add(menuItem);

    menuItem = new JMenuItem("Edit plate set ");
    menuItem.setMnemonic(KeyEvent.VK_E);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
               try{
		  int rowIndex = plate_set_table.getSelectedRow();
		  String plate_set_sys_name = plate_set_table.getValueAt(rowIndex, 0).toString();
		  int plate_set_id = Integer.valueOf(plate_set_sys_name.substring(3));
		  String name = plate_set_table.getValueAt(rowIndex, 1).toString();
		  IFn getPlateSetOwnerID = Clojure.var("lnrocks.core", "get-plate-set-owner-id");
		  int plate_set_owner_id = (int)getPlateSetOwnerID.invoke(plate_set_id);
		  String description = plate_set_table.getValueAt(rowIndex, 6).toString();
		  IFn getUserID = Clojure.var("lnrocks.core", "get-user-id");
   
		  if ( plate_set_owner_id == ((Long)getUserID.invoke()).intValue()) {
		      new DialogEditPlateSet(dmf, plate_set_sys_name, name, description);
	      } else {
                JOptionPane.showMessageDialog(
                    dmf,
                    "Only the owner can modify a plate set.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
		    }   
		  
            } catch(ArrayIndexOutOfBoundsException aioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a plate set!", "Error", JOptionPane.ERROR_MESSAGE);
            }catch(IndexOutOfBoundsException ioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a plate set!", "Error", JOptionPane.ERROR_MESSAGE);
            }
	 	
          }
        });
    menu.add(menuItem);

    JMenu utilitiesMenu = new JMenu("Utilities");
    menu.setMnemonic(KeyEvent.VK_U);
    this.add(utilitiesMenu);

    menuItem = new JMenuItem("Group");
    menuItem.setMnemonic(KeyEvent.VK_G);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            //dbm.groupPlateSets(plate_set_table);
          }
        });
    utilitiesMenu.add(menuItem);

        menuItem = new JMenuItem("Reformat");
    menuItem.setMnemonic(KeyEvent.VK_R);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
           // dbm.reformatPlateSet(plate_set_table);
          }
        });
    utilitiesMenu.add(menuItem);

    menuItem = new JMenuItem("Import assay data");
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	    if(!plate_set_table.getSelectionModel().isSelectionEmpty()){
		Object[][] results = plate_set_table.getSelectedRowsAndHeaderAsStringArray();	   
		String plate_set_sys_name = (String) results[1][0];
		int  plate_set_id = Integer.parseInt(plate_set_sys_name.substring(3));
		int format_id = Integer.parseInt((String)results[1][2]);
		int plate_num = Integer.parseInt((String)results[1][3]);
		
		//new DialogAddPlateSetData(
		//			  dmf, plate_set_sys_name, plate_set_id, format_id, plate_num);
            }
	    else{
	      JOptionPane.showMessageDialog(dmf, "Select a Plate Set to populate with data!");	      
	    }
          }
        });
    utilitiesMenu.add(menuItem);

      menuItem = new JMenuItem("Import Accessions");
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.addActionListener(
        new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if(!plate_set_table.getSelectionModel().isSelectionEmpty()){
		    Object[][] results = plate_set_table.getSelectedRowsAndHeaderAsStringArray();	   
		    String plate_set_sys_name = (String) results[1][0];
		    int  plate_set_id = Integer.parseInt(plate_set_sys_name.substring(3));
		    //dbm.getDatabaseInserter().importAccessionsByPlateSet(plate_set_id);
		}else{
		    JOptionPane.showMessageDialog(dmf, "Select a Plate Set for which to populate with accession IDs!");	      
		} 	   
	    }
        });
    utilitiesMenu.add(menuItem);


      menuItem = new JMenuItem("Import Barcodes");
    menuItem.setMnemonic(KeyEvent.VK_B);
    menuItem.addActionListener(
        new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if(!plate_set_table.getSelectionModel().isSelectionEmpty()){
		    Object[][] results = plate_set_table.getSelectedRowsAndHeaderAsStringArray();	   
		    String plate_set_sys_name = (String) results[1][0];
		    int  plate_set_id = Integer.parseInt(plate_set_sys_name.substring(3));
		   // dbm.getDatabaseInserter().importBarcodesByPlateSet(plate_set_id);
		}else{
		    JOptionPane.showMessageDialog(dmf, "Select a Plate Set for which to populate with accession IDs!");	      
		} 	   
	    }
        });
    utilitiesMenu.add(menuItem);


    
    
    menuItem = new JMenuItem("Worklist");
    menuItem.setMnemonic(KeyEvent.VK_W);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      if(!plate_set_table.getSelectionModel().isSelectionEmpty()){
		  Object[][] results = plate_set_table.getSelectedRowsAndHeaderAsStringArray();
  
		try{
	       	int worklist_id = Integer.parseInt((String)results[1][7]);
               IFn getWorklist = Clojure.var("lnrocks.core", "get-worklist");
		
		Object[][] worklist = (Object[][])getWorklist.invoke(worklist_id);
		POIUtilities poi = new POIUtilities(dmf);
		poi.writeJTableToSpreadsheet("Plate Sets", worklist);
		try{
		Desktop d = Desktop.getDesktop();
		d.open(new File("./Writesheet.xlsx"));
		}
		catch (IOException ioe) {
		}
		}catch(NumberFormatException nfe){
		    JOptionPane.showMessageDialog(dmf, "Plate Set must have an associated worklist!");   
		}
	     
	    }
	    else{
	      JOptionPane.showMessageDialog(dmf, "Select a Plate Set with an associated worklist!");	      
	    }
          }
        });
    utilitiesMenu.add(menuItem);

     menu = new JMenu("Export");
     utilitiesMenu.add(menu);    
    
    menuItem = new JMenuItem("Selected rows this table", KeyEvent.VK_S);
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Export as .csv.");
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
		    Object[][] results = dmf.getUtilities().getSelectedRowsAndHeaderAsStringArray(plate_set_table);
		    if(results.length>1){
			//   LOGGER.info("hit list table: " + results);
			POIUtilities poi = new POIUtilities(dmf);
			poi.writeJTableToSpreadsheet("Plate Sets", results);
			try {
			    Desktop d = Desktop.getDesktop();
			    d.open(new File("./Writesheet.xlsx"));
			} catch (IOException ioe) {
			}	 
		    }else{
			JOptionPane.showMessageDialog(dmf, "Select one or more rows!");	
		    }   

          }
        });
    menu.add(menuItem);

    
    menuItem = new JMenuItem("Underlying data", KeyEvent.VK_U);
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Export as .csv.");   
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
		if(!plate_set_table.getSelectionModel().isSelectionEmpty()){

               IFn getPlateSetData = Clojure.var("lnrocks.core", "get-plate-set-data");
    		    
		    Object[][] results = dmf.getUtilities().getSelectedRowsAndHeaderAsStringArray(plate_set_table);
		    if(results.length>1){
			String[] plate_set_ids = new String[results.length];
			try{
			    
			    for(int i=0; i < results.length-1; i++){
			  plate_set_ids[i] =  plate_set_table.getModel().getValueAt(i, 0).toString().substring(3);
			  LOGGER.info("psid: " + plate_set_ids[i] );
			  }
   
			    Object[][] plate_set_data = (Object[][])getPlateSetData.invoke(plate_set_ids);

			    POIUtilities poi = new POIUtilities(dmf);
			    
			    poi.writeJTableToSpreadsheet("Plate Set Information", plate_set_data);
			
			    Desktop d = Desktop.getDesktop();
			    d.open(new File("./Writesheet.xlsx"));
			}catch(IOException ioe){
			    JOptionPane.showMessageDialog(dmf, "IOException!: " + ioe);   
			}    
		    }else{
			JOptionPane.showMessageDialog(dmf, "Select one or more  Plate Sets!");	
		    }
		}
	  
          }
        });
    menu.add(menuItem);


    
    JButton downbutton = new JButton();
    try {
      ImageIcon down =
          new ImageIcon(
              this.getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif"));
      downbutton.setIcon(down);
    } catch (Exception ex) {
      System.out.println(ex + " ddown.PNG image not found");
    }
    downbutton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            try {
		//int i = plate_set_table.getSelectedRow();
		//String plate_set_sys_name = (String) plate_set_table.getValueAt(i, 0);
	      String results[][] = plate_set_table.getSelectedRowsAndHeaderAsStringArray();
	      String plate_set_sys_name = results[1][1];
	       System.out.println("plate_set_sys_name: " + plate_set_sys_name);
	      IFn setPlateSetSysName = Clojure.var("lnrocks.core", "set-plate-set-sys-name");
              setPlateSetSysName.invoke(plate_set_sys_name);
	      int plate_set_id = Integer.parseInt(plate_set_sys_name.substring(3));
	      IFn setPlateSetID = Clojure.var("lnrocks.core", "set-plate-set-id");
	      setPlateSetID.invoke(plate_set_id);
	      System.out.println("plate_set_id: " + plate_set_id);
	      
              dmf.showPlateTable(plate_set_sys_name);
            } catch (ArrayIndexOutOfBoundsException s) {
			JOptionPane.showMessageDialog(dmf,
					      "Select a row!","Error",JOptionPane.ERROR_MESSAGE);
          
            } catch (IndexOutOfBoundsException s) {
		JOptionPane.showMessageDialog(dmf,
					      "Select a row!","Error",JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    this.add(downbutton);

    JButton upbutton = new JButton();

    try {
      ImageIcon up =
          new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
      upbutton.setIcon(up);
    } catch (Exception ex) {
      System.out.println(ex);
    }
    upbutton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dmf.flipToProjectTable();
	    	    	      dmf.setMainFrameTitle("");

          }
        });
        this.add(upbutton);
    //  menu = new ViewerMenu(d,f);
    // this.add(menu);
   
    this.add(Box.createHorizontalGlue());

    menu = new HelpMenu();
    this.add(menu);
  }
}
