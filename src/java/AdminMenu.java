
package lnrocks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import clojure.java.api.Clojure;
import clojure.lang.IFn;


public class AdminMenu extends JMenu {

    private DialogMainFrame dmf;
    private CustomTable project_table;
    static JTextField fileField;
    private ArrayList<String[]> imported_layout;;    
    private JFileChooser fileChooser;
    private JMenu projectMenu;
    //    private Session session;
    // 
    private Utilities utils;
    private IFn require = Clojure.var("clojure.core", "require");
    
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public AdminMenu(DialogMainFrame _dmf, CustomTable _project_table) {
      
      dmf = _dmf;
      utils = new Utilities(dmf);
    project_table = _project_table;
    //session = dmf.getSession();
    require.invoke(Clojure.read("lnrocks.core"));
    
    this.setText("Admin");
    this.setMnemonic(KeyEvent.VK_A);
    this.getAccessibleContext().setAccessibleDescription("Administrative activities");

    JMenuItem menuItem = new JMenuItem("Add user", KeyEvent.VK_U);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      new DialogAddUser(dmf);           
          }
        });
    this.add(menuItem);

    projectMenu = new JMenu("Project");
    this.add(projectMenu);
    
    menuItem = new JMenuItem("Add", KeyEvent.VK_A);
    menuItem.getAccessibleContext().setAccessibleDescription("Launch the Add Project dialog.");
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      new DialogAddProject(dmf);
   
          }
        });
    
    projectMenu.add(menuItem);

    menuItem = new JMenuItem("Edit", KeyEvent.VK_E);
	   menuItem.getAccessibleContext().setAccessibleDescription("Launch the Edit Project dialog.");
    menuItem.putClientProperty("mf", dmf);
 
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      try{
		  int rowIndex = project_table.getSelectedRow();
		  String projectid = project_table.getValueAt(rowIndex, 0).toString();
		  String name = project_table.getValueAt(rowIndex, 1).toString();
		  String owner = project_table.getValueAt(rowIndex, 2).toString();
		  String description = project_table.getValueAt(rowIndex, 3).toString();
		      IFn getUser = Clojure.var("lnrocks.core", "get-user");
 
		      if (owner.equals((String)getUser.invoke())) {
		      new DialogEditProject(dmf, projectid, name, description);
	      } else {
                JOptionPane.showMessageDialog(
                    dmf,
                    "Only the owner can modify a project.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
              }
            } catch(ArrayIndexOutOfBoundsException aioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a project!", "Error", JOptionPane.ERROR_MESSAGE);
            }catch(IndexOutOfBoundsException ioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a project!", "Error", JOptionPane.ERROR_MESSAGE);
            }
	 	
	  }
	});
    
    projectMenu.add(menuItem);


    menuItem = new JMenuItem("Delete", KeyEvent.VK_D);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      try{
		  IFn getUser = Clojure.var("lnrocks.core", "get-user");
 
		  int rowIndex = project_table.getSelectedRow();
		  String projectid = project_table.getValueAt(rowIndex, 0).toString();
		  String name = project_table.getValueAt(rowIndex, 1).toString();
		  String owner = project_table.getValueAt(rowIndex, 2).toString();
		  String description = project_table.getValueAt(rowIndex, 3).toString();
		  if (owner.equals((String)getUser.invoke())) {
		      int n =  JOptionPane.showConfirmDialog(dmf,
							     "Permanently delete " + projectid + " and all its\n"
							     + "components? This is a cascading delete\n"
							     + "that cannot be undone!",
							     "Delete Project?",
							     JOptionPane.YES_NO_OPTION);
		      if(n == JOptionPane.YES_OPTION){
			  int prj_id = Integer.parseInt(projectid.substring(4));
			  IFn deleteProject = Clojure.var("lnrocks.core", "delete-project");
			  deleteProject.invoke(prj_id);
			  
		      }
		    
	      } else {
                JOptionPane.showMessageDialog(
                    dmf,
                    "Only the owner can modify a project.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
              }
            } catch(ArrayIndexOutOfBoundsException aioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a project!", "Error", JOptionPane.ERROR_MESSAGE);
            }catch(IndexOutOfBoundsException ioob) {
              JOptionPane.showMessageDialog(
                  dmf, "Please select a project!", "Error", JOptionPane.ERROR_MESSAGE);
            }
	 	
	  }
	});
    
    projectMenu.add(menuItem);

    menuItem = new JMenuItem("Import Plate Layout", KeyEvent.VK_I);
    menuItem.addActionListener(
	   new ActionListener() {
	       public void actionPerformed(ActionEvent e) {
	       
		   JFileChooser file_chooser= new JFileChooser();
		   int returnVal = file_chooser.showOpenDialog(null);
		   
		   if (returnVal == JFileChooser.APPROVE_OPTION) {
		       java.io.File file = file_chooser.getSelectedFile();
		       // This is where a real application would open the file.
		       imported_layout = utils.loadDataFile(file.toString());
		       int lines_data = imported_layout.size() -1; //for header
		       if(lines_data!= 96 && lines_data!=384 ){
			   JOptionPane.showMessageDialog(dmf,
							 "Layout import file must contain 96 or 384 rows of data.",
							 "File format Error",
							 JOptionPane.ERROR_MESSAGE);
		       }
		       // new DialogImportLayoutViewer(dbm, imported_layout);
		 
		   } else {
		       LOGGER.info("Open command cancelled by user.\n");
		   }

	      
	  }
        });
    this.add(menuItem);

        projectMenu.add(menuItem);

    menuItem = new JMenuItem("DB Utilities", KeyEvent.VK_U);
    menuItem.addActionListener(
	   new ActionListener() {
	       public void actionPerformed(ActionEvent e) {
		   // IFn getAllProps = Clojure.var("lnrocks.core", "get-all-props");

			      //   new DialogPropertiesNotFound(getAllProps.invoke());
		 		   	      
	  }
        });
    this.add(menuItem);


    
  }
}
