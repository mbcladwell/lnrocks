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

import clojure.java.api.Clojure;
import clojure.lang.IFn;



public class MenuBarForProject extends JMenuBar {

  DialogMainFrame dmf;
  CustomTable project_table;
    //    Session session;

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public MenuBarForProject(DialogMainFrame _dmf, CustomTable _project_table) {

      
      dmf = _dmf;
    project_table = _project_table;
    //session = dbm.getDialogMainFrame().getSession();
     IFn require = Clojure.var("clojure.core", "require");
    require.invoke(Clojure.read("lnrocks.core"));

    JMenu menu = new JMenu("Project");
    menu.setMnemonic(KeyEvent.VK_P);
    menu.getAccessibleContext().setAccessibleDescription("Project");
    this.add(menu);

    JMenuItem    menuItem = new JMenuItem("Export", KeyEvent.VK_E);
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Export as .csv.");
   // menuItem.putClientProperty("mf", dbm);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            String[][] results = project_table.getSelectedRowsAndHeaderAsStringArray();
            POIUtilities poi = new POIUtilities(dmf);
            poi.writeJTableToSpreadsheet("Projects", results);
            try {
              Desktop d = Desktop.getDesktop();
              d.open(new File("./Writesheet.xlsx"));
            } catch (IOException ioe) {
            }
            // JWSFileChooserDemo jwsfcd = new JWSFileChooserDemo();
            // jwsfcd.createAndShowGUI();

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
      LOGGER.severe(ex + " down image not found");
    }
    downbutton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            try {
		int i = project_table.convertRowIndexToModel(project_table.getSelectedRow());
		String project_sys_name = (String) project_table.getValueAt(i, 1);
	         String results[][] = project_table.getSelectedRowsAndHeaderAsStringArray();
		 //	 LOGGER.info("view row: " + project_table.getSelectedRow());
		 //LOGGER.info("model row: " + i);
	      
		 // LOGGER.info("down button results: " + results);
		 //LOGGER.info("down button row results: " + results[1][1]);
		 //    dbm.updateSessionWithProject(results[1][0]);
		 dmf.setMainFrameTitle(results[1][1]);
	      dmf.showPlateSetTable(results[1][1]);
	     
            } catch (ArrayIndexOutOfBoundsException s) {
		JOptionPane.showMessageDialog( dmf,
					      "Select a row!","Error",JOptionPane.ERROR_MESSAGE);
            } catch (IndexOutOfBoundsException s) {
		JOptionPane.showMessageDialog(dmf,
					      "Select a row!","Error",JOptionPane.ERROR_MESSAGE);
            }
          }
        });
    this.add(downbutton);

    menu = new ViewerMenu(dmf);
    this.add(menu);

    // IFn getUserGroup = Clojure.var("lnrocks.core", "get-user-group");
    
    // if(getUserGroup.invoke().equals("admin")){
    // menu = new AdminMenu(dmf, project_table);
    //  this.add(menu);
    // }
    
     this.add(Box.createHorizontalGlue());

    menu = new HelpMenu();

    this.add(menu);
  }
}
