package lnrocks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogImportPlateSetAccessionIDs extends JDialog
    implements java.awt.event.ActionListener, javax.swing.event.DocumentListener {
  static JButton button;
  static JLabel label;
  static JLabel nLabel;
  static JComboBox<ComboItem> assayTypes;
  static JComboBox<ComboItem> plateLayouts;
  static JComboBox<ComboItem> algorithmList;
    
  static JTextField fileField;
  static JTextField nameField;
  static JTextField descrField;
  static JTextField layoutField;
  static JTextField nField;
    
  static JButton okButton;

  static JButton select;
  static JButton cancelButton;
  static JButton helpButton;
  private static final long serialVersionUID = 1L;
  private DialogMainFrame dmf;
  private DatabaseManager dbm;
  private DatabaseRetriever dbr;
  private DatabaseInserter dbi;

  private ComboItem plate_set;
   
  private String plate_set_description;
    private ComboItem format;
    private int plate_num;
    private int expected_rows;
    private ComboItem plate_layout;
  private JFileChooser fileChooser;
    private JCheckBox checkBox;
    private ArrayList<String[]>  accessions; 
    //    private Session session;
    private IFn require = Clojure.var("clojure.core", "require");
        
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DialogImportPlateSetAccessionIDs(
      DatabaseManager _dbm,
      String _plate_set_sys_name,
      int _plate_set_id,
      int _format_id,
      int _plate_num) {

    plate_set = new ComboItem(_plate_set_id, _plate_set_sys_name);
    format = new ComboItem(_format_id, String.valueOf(_format_id));
    plate_num = _plate_num;
    require.invoke(Clojure.read("ln.codax-manager"));
    //    expected_rows = dbr.getNumberOfSamplesForPlateSetID(_plate_set_id);
    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    this.dbm = _dbm;
    //  this.session = dmf.getSession();
    // this.dbm = session.getDatabaseManager();
    this.dbr = dbm.getDatabaseRetriever();
    this.dbi = dbm.getDatabaseInserter();
    expected_rows = dbr.getNumberOfSamplesForPlateSetID(_plate_set_id);

    //LOGGER.info("plate_set_id: " + plate_set_id);
    plate_set_description = dbr.getDescriptionForPlateSet(_plate_set_sys_name);
    
    
    fileChooser = new JFileChooser();

    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Import Accession IDs for Plate Set " + plate_set.toString());
    // c.gridwidth = 2;

    label = new JLabel("Plate set:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;

    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);

    pane.add(label, c);

    label = new JLabel(plate_set.toString(), SwingConstants.LEFT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("Format:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(format.toString(), SwingConstants.LEFT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 3;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("# plates:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 4;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(String.valueOf(plate_num), SwingConstants.LEFT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 5;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("Plate set description:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(plate_set_description, SwingConstants.LEFT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    label = new JLabel("Expected row count:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(String.valueOf(expected_rows), SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 2;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);

    
    select =
        new JButton(
            "Select IDs file...", createImageIcon("/toolbarButtonGraphics/general/Open16.gif"));
    select.setMnemonic(KeyEvent.VK_S);
    select.setActionCommand("select");
    select.setEnabled(true);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 6;
    c.gridwidth = 1;
    c.gridheight = 1;
    select.addActionListener(this);
    pane.add(select, c);

    fileField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 6;
    c.gridwidth = 5;
    c.gridheight = 1;
    fileField.getDocument().addDocumentListener(this);
    pane.add(fileField, c);

    
    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 8;
    c.gridwidth = 1;
    c.gridheight = 1;
    pane.add(okButton, c);
    okButton.setEnabled(false);
    okButton.addActionListener(this);

    cancelButton = new JButton("Cancel");
    cancelButton.setMnemonic(KeyEvent.VK_C);
    cancelButton.setActionCommand("cancel");
    cancelButton.setEnabled(true);
    cancelButton.setForeground(Color.RED);
    c.gridx = 2;
    c.gridy = 8;
    pane.add(cancelButton, c);
    cancelButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));

        helpButton = new JButton("Help");
    helpButton.setMnemonic(KeyEvent.VK_H);
    helpButton.setActionCommand("help");
    helpButton.setEnabled(true);
    c.gridx = 4;
    c.gridy = 8;
    pane.add(helpButton, c);
    helpButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	          IFn getHelpURLPrefix = Clojure.var("ln.codax-manager", "get-help-url-prefix");

		  openWebpage(URI.create((String)getHelpURLPrefix.invoke() + "accessionids"));
            
          }
        }));

    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path) {
    java.net.URL imgURL = DialogAddPlateSetData.class.getResource(path);
    if (imgURL != null) {
      return new ImageIcon(imgURL);
    } else {
      System.err.println("Couldn't find file: " + path);
      return null;
    }
  }

  public void actionPerformed(ActionEvent e) {
      int top_n_number = 0;

    if (e.getSource() == okButton) {

	accessions = dbm.getDialogMainFrame().getUtilities().loadDataFile(fileField.getText());
	if(!((accessions.size()-1) == expected_rows)){  //If Top N is the algorithm
	    	JOptionPane.showMessageDialog(dmf,
					      new String("Expecting " + String.valueOf(expected_rows) + " rows but found " + (accessions.size()-1) + " rows." ), "Import Error",      JOptionPane.ERROR_MESSAGE);
		return;
	    
	    
	}
	
      dbi.associateAccessionsWithPlateSet( plate_set.getKey(), format.getKey(), accessions);
      dispose();
    }

    if (e.getSource() == select) {
      int returnVal = fileChooser.showOpenDialog(DialogImportPlateSetAccessionIDs.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        java.io.File file = fileChooser.getSelectedFile();
        // This is where a real application would open the file.
        fileField.setText(file.toString());
      } else {
        LOGGER.info("Open command cancelled by user.\n");
      }
    }
  }

  public void insertUpdate(DocumentEvent e) {

    if ( fileField.getText().length() > 0) {
      okButton.setEnabled(true);
    } else {
      okButton.setEnabled(false);
    }
  }

  public void removeUpdate(DocumentEvent e) {
    if ( fileField.getText().length() > 0) {
      okButton.setEnabled(true);
    } else {
      okButton.setEnabled(false);
    }
  }

  public void changedUpdate(DocumentEvent e) {
    // Plain text components don't fire these events.
  }

    public static boolean openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(uri);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return false;
}

    public static boolean openWebpage(URL url) {
    try {
        return openWebpage(url.toURI());
    } catch (URISyntaxException e) {
        e.printStackTrace();
    }
    return false;
}

}
