package lnrocks;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ViewerMenu extends JMenu{
    DialogMainFrame dmf;
    DatabaseManager dbm;
  // J/Table table;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public ViewerMenu(DatabaseManager _dbm) {
      dbm = _dbm;
      this.dmf = dbm.getDialogMainFrame();
    this.setText("Viewers");
    this.setMnemonic(KeyEvent.VK_V);
    this.getAccessibleContext().setAccessibleDescription("Viewers");

    JMenuItem menuItem = new JMenuItem("Plate Layouts");
     menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            new LayoutViewer(dbm);
          }
        });
    this.add(menuItem);

  menuItem = new JMenuItem("Assay Runs/Hit Lists", KeyEvent.VK_A);
     menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
    menuItem.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      AssayRunViewer arv = new AssayRunViewer(dbm);
          }
        });
    this.add(menuItem);

   
  }
}
