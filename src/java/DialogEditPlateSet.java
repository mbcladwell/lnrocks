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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogEditPlateSet extends JDialog {
  static JButton button;
  static JLabel label;
  static JLabel Description;
  static JTextField nameField;
  static JTextField ownerField;
  static JTextField descriptionField;
  static JButton okButton;
  static JButton cancelButton;
  static String plate_set_sys_name;
  static String project_sys_name;
    
  final Instant instant = Instant.now();
  static DialogMainFrame dmf;
  
    //  private static Session session;
  
  final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  public DialogEditPlateSet(
      DialogMainFrame _dmf, String _plate_set_sys_name, String _name, String _description) {

      dmf = _dmf;
      require.invoke(Clojure.read("lnrocks.core"));
      //session = dmf.getSession();
        IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");

	project_sys_name = (String)getProjectSysName.invoke();
    //dbm = session.getDatabaseManager();
    plate_set_sys_name = _plate_set_sys_name;

    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Edit Plate Set " + plate_set_sys_name);
    // c.gridwidth = 2;

    label = new JLabel("Date:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 2, 2);
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel("Owner:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    pane.add(label, c);

    label = new JLabel("Plate Set Name:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    pane.add(label, c);

    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    pane.add(label, c);

    label = new JLabel(df.format(Date.from(instant)));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

      IFn getUser = Clojure.var("lnrocks.core", "get-user");
   
      label = new JLabel((String)getUser.invoke());
    c.gridx = 1;
    c.gridy = 1;
    c.gridheight = 1;
    pane.add(label, c);

    nameField = new JTextField(30);
    nameField.setText(_name);
    c.gridwidth = 2;
    c.gridx = 1;
    c.gridy = 2;
    pane.add(nameField, c);

    descriptionField = new JTextField(30);
    descriptionField.setText(_description);
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 2;
    pane.add(descriptionField, c);

    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 5;
    c.gridwidth = 1;
    c.gridheight = 1;
    okButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {

    //         dbm.getDatabaseInserter()
      //          .updatePlateSet(nameField.getText(), descriptionField.getText(), plate_set_sys_name);
        //    dmf.getPlateSetPanel().updatePanel(project_sys_name); //the plate set table needs the project id
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
    c.gridy = 5;
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
