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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

public class DialogGroupPlateSet extends JDialog {
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
    static String layout;
  static JButton okButton;
  static JButton cancelButton;
  final Instant instant = Instant.now();
  final DialogMainFrame dmf;
  
    //final Session session;
  final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
    private IFn require = Clojure.var("clojure.core", "require");
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  // final EntityManager em;

  /**
   * Called from DatabaseManager.groupPlateSets()
   *
   * @param _plate_set_num_plates plate set id and the number of plates in the plate set
   * @param _format number of wells per plate
   */
    public DialogGroupPlateSet(
			       DialogMainFrame dmf,
			       HashMap<String, String> _plate_set_num_plates,
			       String _format,
			       ArrayList<String> _plate_sys_names,
			       String _layout) {
      //this.session = _session;
      dbm = _dbm;
    this.dmf = dmf;
    require.invoke(Clojure.read("lnrocks.core"));
     IFn getUser = Clojure.var("lnrocks.core", "get-user");
    
     owner = (String)getUser.invoke();
    HashMap<String, String> plate_set_num_plates = _plate_set_num_plates;
    String format = _format;
    layout = _layout;
    String plateSetsAndNumbers = new String("");
    ArrayList<String> plate_sys_names = _plate_sys_names;

    for (HashMap.Entry<String, String> entry : plate_set_num_plates.entrySet()) {
      plateSetsAndNumbers = plateSetsAndNumbers + entry.getKey() + " (" + entry.getValue() + "); ";
    }

    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    // this.em = em;
    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Group Plate Sets");
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

    label = new JLabel("Plate Set ID (# plates):", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(plateSetsAndNumbers);
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
    //LOGGER.info("format: " + format);
    // ComboItem[] plateLayouts = dbm.getDatabaseRetriever().getSourcePlateLayoutNames(Integer.parseInt(format));
    // //LOGGER.info("plateLayout: " + plateLayouts);

    // layoutList = new JComboBox<ComboItem>(plateLayouts);
    // layoutList.setSelectedIndex(0);
    // c.gridx = 1;
    // c.gridy = 7;
    // c.gridheight = 1;
    // c.gridwidth = 1;
    // c.anchor = GridBagConstraints.LINE_START;
    // pane.add(layoutList, c);
           label = new JLabel(layout, SwingConstants.RIGHT);
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
	      IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");

            dbm.getDatabaseInserter()
                .groupPlateSetsIntoNewPlateSet(
                    descriptionField.getText(),
                    nameField.getText(),
                    plate_set_num_plates,
                    format,
                    typeList.getSelectedItem().toString(),
                    ((Long)getProjectID.invoke()).intValue(),             
		    //((ComboItem)layoutList.getSelectedItem()).getKey(),
		    plate_sys_names);
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
