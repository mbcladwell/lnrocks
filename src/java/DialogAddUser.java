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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogAddUser extends JDialog {
  static JButton button;
  static JLabel label;
  static JLabel Description;
  static JTextField nameField;
  static JTextField ownerField;
  static JTextField tagsField;
  static JTextField passwordField;
  static JButton okButton;
  static JButton cancelButton;
    static JComboBox<ComboItem> groupList;

  final Instant instant = Instant.now();
  static DialogMainFrame dmf;
    //  private static Session session;
  //private static DatabaseManager dbm;
  final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private IFn require = Clojure.var("clojure.core", "require");


  public DialogAddUser(DialogMainFrame _dmf) {
      
      dmf = _dmf;
      // session = dmf.getSession();
      //dbm = session.getDatabaseManager();

    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Add a User");
    // c.gridwidth = 2;

    label = new JLabel("Date:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 2, 2);
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel("User Name:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    pane.add(label, c);

    label = new JLabel("Tags:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    pane.add(label, c);

        label = new JLabel("Password:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    pane.add(label, c);

    label = new JLabel("Group:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 5;
    pane.add(label, c);

    label = new JLabel(df.format(Date.from(instant)));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);
 
   nameField = new JTextField(30);
    c.gridwidth = 2;
    c.gridx = 1;
    c.gridy = 2;
    pane.add(nameField, c);

    tagsField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    pane.add(tagsField, c);

        passwordField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    pane.add(passwordField, c);

       IFn getAllUserGroups = Clojure.var("lnrocks.core", "get-all-user-groups");
 
    ComboItem[] groups = (ComboItem[])(getAllUserGroups.invoke());

    groupList = new JComboBox<ComboItem>(groups);
    groupList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(groupList, c);

    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    okButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {    
    IFn insertUser = Clojure.var("lnrocks.core", "insertUser");
 
           // dbm.getDatabaseInserter()
             //   .insertUser(
		//	    nameField.getText(), tagsField.getText(), passwordField.getText(), ((ComboItem)groupList.getSelectedItem()).getKey() ); 
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

  private void addToDB() {}
}
