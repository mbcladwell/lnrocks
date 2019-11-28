package lnrocks;

import java.awt.BorderLayout;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogHelpAbout extends JDialog {
  static JButton button;
  static JLabel licenceKey;
  static JLabel picLabel;
  static JLabel label;
    private String dbname;
    private String dbsource;
  static JButton okButton;
  final Instant instant = Instant.now();
  final DateFormat df = new SimpleDateFormat("d MMMMM yyyy");
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  public DialogHelpAbout() {

      require.invoke(Clojure.read("ln.codax-manager"));
    IFn getDBname = Clojure.var("ln.codax-manager", "get-dbname");
    dbname = (String)getDBname.invoke();
    IFn getDBsource = Clojure.var("ln.codax-manager", "get-source");
    dbsource = (String)getDBsource.invoke();
    

    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();

    // this.setIconImage(img);
    this.setTitle("About LIMS*Nucleus");
    // c.gridwidth = 2;

    label = new JLabel(df.format(Date.from(instant)), SwingConstants.CENTER);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(5, 5, 2, 2);
    pane.add(label, c);

    label = new JLabel("LIMS*Nucleus v0.1.10.19", SwingConstants.CENTER);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    c.insets = new Insets(5, 5, 2, 2);
    pane.add(label, c);

    label = new JLabel(new String("Database: " + dbname + " ( " + dbsource + " )"), SwingConstants.CENTER);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    c.insets = new Insets(5, 5, 2, 2);
    pane.add(label, c);

    ImageIcon img = new ImageIcon(this.getClass().getResource("/images/las.png"));
    picLabel = new JLabel(img, SwingConstants.CENTER);
    c.gridx = 0;
    c.gridy = 3;
    pane.add(picLabel, c);

    label = new JLabel("email: info@labsolns.com", SwingConstants.CENTER);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 4;
    c.insets = new Insets(5, 5, 2, 2);
    pane.add(label, c);

    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 4;
    c.gridwidth = 1;
    c.gridheight = 1;
    okButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));

    pane.add(okButton, c);

    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }
}
