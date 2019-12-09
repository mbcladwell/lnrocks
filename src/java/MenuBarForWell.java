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


public class MenuBarForWell extends JMenuBar {

  private static final long serialVersionUID = 1L;
  DialogMainFrame dmf;
 
  CustomTable well_table;
    //    Session session;
     private IFn require = Clojure.var("clojure.core", "require");

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public MenuBarForWell(DialogMainFrame dmf, CustomTable _table) {
   
      //session = _s;
      //dmf = session.getDialogMainFrame();
    well_table = _table;
       require.invoke(Clojure.read("lnrocks.core"));
  
    // Create the menu bar.
    // JMenuBar menuBar = new JMenuBar();
    //    this.em = em;
    // Build the first menu.
    JMenu menu = new JMenu("Well");
    menu.setMnemonic(KeyEvent.VK_P);
    menu.getAccessibleContext()
        .setAccessibleDescription("The only menu in this program that has menu items");
    this.add(menu);

    // a group of JMenuItems
    JMenuItem menuItem = new JMenuItem("All wells", KeyEvent.VK_A);
    menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Show all wells for this projet.");
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      try {
		  IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
		  String project_sys_name = (String)getProjectSysName.invoke();
	 
		  dmf.showAllWellsTable(project_sys_name);
		      
          
            } catch (IndexOutOfBoundsException s) {
		JOptionPane.showMessageDialog(dmf,
					      "Select a row!","Error",JOptionPane.ERROR_MESSAGE);
            }

          }
        });
    menu.add(menuItem);

    menu = new JMenu("Utilities");
    menu.setMnemonic(KeyEvent.VK_U);
    menu.getAccessibleContext().setAccessibleDescription("Project utilities");
    this.add(menu);


    menuItem = new JMenuItem("Export", KeyEvent.VK_E);
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
    menuItem.getAccessibleContext().setAccessibleDescription("Export as .csv.");
    menuItem.putClientProperty("mf", dmf);
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {

            String[][] results = well_table.getSelectedRowsAndHeaderAsStringArray();
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

    JButton upbutton = new JButton();

    try {
      ImageIcon img =
          new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif"));
      upbutton.setIcon(img);
    } catch (Exception ex) {
      System.out.println(ex);
    }
    upbutton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      dmf.flipToPlate();
          }
        });

    this.add(upbutton);

    menu = new ViewerMenu(dmf);
    this.add(menu);

    this.add(Box.createHorizontalGlue());

    menu = new HelpMenu();
    this.add(menu);
  
   }
}
