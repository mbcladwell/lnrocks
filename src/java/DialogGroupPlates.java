package lnrocks;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;


import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogGroupPlates extends JDialog {
  static JButton button;
  static JLabel label;
  static JLabel Description;
  static JTextField nameField;
  static JLabel ownerLabel;
  static String owner;
  static JTextField descriptionField;
  static JTextField numberField;
  static JComboBox<Integer> formatList;
  static JComboBox<ComboItem> typeList;
  static JComboBox<ComboItem> layoutList;
  static JButton okButton;
  static JButton cancelButton;
  final Instant instant = Instant.now();
  final DialogMainFrame dmf;
    final DatabaseManager dbm;
    //  final Session session;
  final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
      private IFn require = Clojure.var("clojure.core", "require");
 
  // final EntityManager em;

  /**
   * Called from DatabaseManager.groupPlates()
   *
   * @param _plate_set_num_plates plate set id and the number of plates in the plate set
   * @param _format number of wells per plate
   */
  public DialogGroupPlates(DatabaseManager _dbm, Set<String> _plates, String _format) {
      dbm = _dbm;
      this.dmf = dbm.getDialogMainFrame();
      //this.session = dmf.getSession();
      require.invoke(Clojure.read("ln.codax-manager"));
      IFn getUser = Clojure.var("ln.codax-manager", "get-user");
      
      owner = (String)getUser.invoke();
    Set<String> plates = _plates;
    String num_plates = Integer.valueOf(plates.size()).toString();
    String format = _format;
    String plate_sys_names = new String(); //for display on the dialog box; may be truncated if too many plates
    for (String temp : plates) {
      plate_sys_names = plate_sys_names + temp + ";";
    }

    if (plate_sys_names.length() > 70) {
      plate_sys_names = plate_sys_names.substring(0, 70) + "...";
    }

    //   List<String> list = new ArrayList<String>(plates);
    // String a_ps = list.get(0); // a plate_set from which I will determine the layout
    // LOGGER.info("a_ps: " + a_ps);
    //figure out the layout from the parent plate_set
    IFn getPlateSetSysName = Clojure.var("ln.codax-manager", "get-plate-set-sys-name");
    
    IFn getLayoutIDforPlateSetSysName = Clojure.var("ln.db-retriever", "get-layout-id-for-plate-set-sys-name");
    int layout_id = (int)getLayoutIDforPlateSetSysName.invoke((String)getPlateSetSysName.invoke());
    
    
    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    // this.em = em;
    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Group Plates");
    // c.gridwidth = 2;

    label = new JLabel("Date:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;

    c.insets = new Insets(5, 5, 2, 2);
    pane.add(label, c);

    label = new JLabel(df.format(Date.from(instant)));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("Owner:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    ownerLabel = new JLabel(owner);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(ownerLabel, c);

    label = new JLabel("New Plate Set Name:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    nameField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 2;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(nameField, c);

    label = new JLabel("New Plate Set Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 1;
    pane.add(label, c);

    descriptionField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(descriptionField, c);

    label = new JLabel("Plate IDs:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(plate_sys_names);
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("Format:", SwingConstants.RIGHT);
    c.gridx = 4;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(format + " well");

    c.gridx = 5;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);
    // formatList.addActionListener(this);

    label = new JLabel("New Plate Set Type:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    ComboItem[] plateTypes = dbm.getDatabaseRetriever().getPlateTypes();

    typeList = new JComboBox<ComboItem>(plateTypes);
    typeList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(typeList, c);

    label = new JLabel("New Plate Set Layout:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    //ComboItem[] plateLayouts = dbm.getDatabaseRetriever().getSourcePlateLayoutNames(Integer.parseInt(format),((ComboItem)typeList.getSelectedItem()).getKey());
  
    IFn getLayoutNameDescrForLayoutID = Clojure.var("ln.db-retriever", "get-layout-name-descr-for-layout-id");

    //layoutList = new JComboBox<ComboItem>(plateLayouts);
    //layoutList.setSelectedIndex(0);
    label = new JLabel((String)getLayoutNameDescrForLayoutID.invoke(layout_id));
    c.gridx = 1;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    
    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 8;
    c.gridwidth = 2;
    c.gridheight = 1;
    okButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      IFn getProjectID = Clojure.var("ln.codax-manager", "get-project-id");
	      System.out.println("in DialogGrouPlates launching dbi.groupPlatesIntoPlateSet");
	      dbm.getDatabaseInserter()
		  .groupPlatesIntoPlateSet(
					   descriptionField.getText(),
					   nameField.getText(),
					   plates,
					   format,
					   typeList.getSelectedItem().toString(),
					   ((Long)getProjectID.invoke()).intValue(),
					   layout_id);
	      dispose();
          }
        }));

    pane.add(okButton, c);

    cancelButton = new JButton("Cancel");
    cancelButton.setMnemonic(KeyEvent.VK_C);
    cancelButton.setActionCommand("cancel");
    cancelButton.setEnabled(true);
    cancelButton.setForeground(Color.RED);
    c.gridx = 1;
    c.gridy = 8;
    c.gridwidth = 1;
    pane.add(cancelButton, c);
    cancelButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));

    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }
}
