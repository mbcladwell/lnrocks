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
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogRearrayHitList extends JDialog {
  static JButton button;
  static JLabel label;
  static JLabel Description;
  static JTextField nameField;
  static JLabel ownerLabel;
  static JLabel numberLabel;
  static String owner;
  static JTextField descriptionField;
  static JTextField numberField;
  static JComboBox<Integer> formatList;
  static JComboBox<ComboItem> typeList;
    private ComboItem [] layoutNames;
    private JComboBox<ComboItem> layoutList;
    private DefaultComboBoxModel<ComboItem> layout_names_list_model;
    private int hit_list_id;
    private int plate_set_id;
    private int source_plate_set_format;
    private int dest_plate_set_format;
    private int num_dest_plates;
    private int unknown_count;
    private int unknowns_per_dest_plate;
    private String hit_list_sys_name;
    private String plate_set_sys_name;
    private IFn require = Clojure.var("clojure.core", "require");
    
  static JButton okButton;
  static JButton cancelButton;
  static JButton helpButton;
  final Instant instant = Instant.now();
    final DialogMainFrame dmf;
    final DatabaseManager dbm;
    //  final Session session;
  final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  // final EntityManager em;

    public DialogRearrayHitList(DatabaseManager _dbm, int _plate_set_id, String _plate_set_sys_name, int _source_plate_set_format, int _hit_list_id, String _hit_list_sys_name, int _unknown_count) {
	dbm = _dbm;
	this.dmf = dbm.getDialogMainFrame();
    plate_set_id = _plate_set_id;
    plate_set_sys_name = _plate_set_sys_name;
    source_plate_set_format = _source_plate_set_format;
    hit_list_id = _hit_list_id;
    hit_list_sys_name = _hit_list_sys_name;
    dest_plate_set_format = source_plate_set_format;
    unknown_count = _unknown_count;
    require.invoke(Clojure.read("ln.codax-manager"));
     IFn getUser = Clojure.var("ln.codax-manager", "get-user");
    
     //    this.session = dmf.getSession();
     owner = (String)getUser.invoke();
    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    // this.em = em;
    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Rearray Hit List " + hit_list_sys_name + " using plate set " + plate_set_sys_name);
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

    label = new JLabel("Plate Set Name:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    pane.add(label, c);

    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    c.gridheight = 1;
    pane.add(label, c);

    label = new JLabel("Number of plates:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    c.gridheight = 1;
    pane.add(label, c);

    ownerLabel = new JLabel(owner);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(ownerLabel, c);

    nameField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 2;
    c.gridheight = 1;
    pane.add(nameField, c);

    descriptionField = new JTextField(30);
    descriptionField.setText("Rearray of plate set " + plate_set_sys_name + " using hit list " + hit_list_sys_name + ";");
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    pane.add(descriptionField, c);

    
    numberLabel = new JLabel("");
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    pane.add(numberLabel, c);

    
    label = new JLabel("Format:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    Integer[] formats = {96, 384};

    formatList = new JComboBox<Integer>(formats);
     for(int i=0; i < formatList.getItemCount(); i++){
	if(formatList.getItemAt(i) == dest_plate_set_format){
		formatList.setSelectedIndex(i);
	    }
    }
       c.gridx = 3;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    formatList.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      layoutNames = dbm.getDatabaseRetriever().
		  getSourcePlateLayoutNames((int)formatList.getSelectedItem(),((ComboItem)typeList.getSelectedItem()).getKey());
	      layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutNames );
	      layoutList.setModel(layout_names_list_model );
	      layoutList.setSelectedIndex(0);
	      DialogRearrayHitList.this.refreshPlateNumberLabel();
          }
        }));
    pane.add(formatList, c);
    // formatList.addActionListener(this);

    label = new JLabel("Type:", SwingConstants.RIGHT);
    c.gridx = 4;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    ComboItem[] plateTypes = dbm.getDatabaseRetriever().getPlateTypes();

    //suggest "rearray" as the plate type; I do not accomodate replication selections here
    //i.e. selection of assay will not trigger repopulating the Layout dropdown
    typeList = new JComboBox<ComboItem>(plateTypes);
     for(int i=0; i < typeList.getItemCount(); i++){
	 if((((ComboItem)typeList.getItemAt(i)).toString()).equals("rearray")){
		typeList.setSelectedIndex(i);
	    }
    }
    c.gridx = 5;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(typeList, c);

    label = new JLabel("Layout:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 5;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    ComboItem[] layoutTypes = dbm.getDatabaseRetriever().
	getSourcePlateLayoutNames((int)formatList.getSelectedItem(),									                       ((ComboItem)typeList.getSelectedItem()).getKey());
    //LOGGER.info("layoutTypes: " + layoutTypes[0].toString());
    layoutList = new JComboBox<ComboItem>(layoutTypes);
    layoutList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(layoutList, c);
    layoutList.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	      DialogRearrayHitList.this.refreshPlateNumberLabel();
          }
        }));

    
    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 6;
    c.gridwidth = 2;
    c.gridheight = 1;
    okButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {

     
                dbm.getDatabaseInserter()
                .insertRearrayedPlateSet(
                    nameField.getText(),
                    descriptionField.getText(),
                    numberLabel.getText(),
                    (int)formatList.getSelectedItem(),
                    ((ComboItem)typeList.getSelectedItem()).getKey(),
		    ((ComboItem)layoutList.getSelectedItem()).getKey(),
		    hit_list_id, plate_set_id);
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
    c.gridy = 6;
    c.gridwidth = 1;
    pane.add(cancelButton, c);
    cancelButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));


    helpButton = new JButton("Help");
    helpButton.setMnemonic(KeyEvent.VK_H);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 5;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    helpButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {

                   IFn openHelpPage = Clojure.var("ln.session", "open-help-page");
	      openHelpPage.invoke( "rearray");

          }
        }));

    pane.add(helpButton, c);



    
    refreshPlateNumberLabel();
    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

  private void refreshPlateNumberLabel() {
      unknowns_per_dest_plate = dbm.getDatabaseRetriever().getUnknownCountForLayoutID(((ComboItem)layoutList.getSelectedItem()).getKey());
      num_dest_plates = (int)Math.ceil(unknown_count/(double)unknowns_per_dest_plate);
      numberLabel.setText(String.valueOf(num_dest_plates));
  }
}
