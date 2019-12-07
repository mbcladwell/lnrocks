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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.event.DocumentEvent;
import java.beans.*;
import javax.swing.*;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class DialogAddPlateSetData extends JDialog
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
  private static final long serialVersionUID = 1L;
  private DialogMainFrame dmf;
  private DatabaseManager dbm;
  private DatabaseRetriever dbr;
  private DatabaseInserter dbi;
   private ProgressBar progress_bar;

  private ComboItem plate_set;
   
  private String plate_set_description;
    private ComboItem format;
    private int plate_num;
    private ComboItem plate_layout;
  private JFileChooser fileChooser;
    private JCheckBox checkBox;
    //    private Session session;
    private IFn require = Clojure.var("clojure.core", "require");
    
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DialogAddPlateSetData(
      DialogMainFrame dmf,
      String _plate_set_sys_name,
      int _plate_set_id,
      int _format_id,
      int _plate_num) {

    plate_set = new ComboItem(_plate_set_id, _plate_set_sys_name);
    format = new ComboItem(_format_id, String.valueOf(_format_id));
    plate_num = _plate_num;
    require.invoke(Clojure.read("lnrocks.core"));
    	progress_bar = new ProgressBar();

    dbm = _dbm;
    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    this.dmf = dmf;
    //session = dmf.getSession();
    //this.dbm = session.getDatabaseManager();
    this.dbr = dbm.getDatabaseRetriever();
    this.dbi = dbm.getDatabaseInserter();

    //LOGGER.info("plate_set_id: " + plate_set_id);
    plate_set_description = dbr.getDescriptionForPlateSet(_plate_set_sys_name);

    int plate_layout_name_id = dbm.getDatabaseRetriever().getPlateLayoutNameIDForPlateSetID(_plate_set_id );
    plate_layout = dbm.getDatabaseRetriever().getPlateLayoutNameAndID(plate_layout_name_id);
    // LOGGER.info("plate_layout_name_id: " + plate_layout_name_id);
    //LOGGER.info("plate_layout_name key: " + plate_layout.getKey());
    //LOGGER.info("plate_layout_name string: " + plate_layout.toString());
    
    
    fileChooser = new JFileChooser();

    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Associate assay run data with plate set");
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

    label = new JLabel("Assay run name:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 2;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    nameField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(nameField, c);
    nameField.getDocument().addDocumentListener(this);

    label = new JLabel("Assay run description:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    descrField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 3;
    c.gridwidth = 5;
    c.gridheight = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    // c.anchor = GridBagConstraints.LINE_START;
    pane.add(descrField, c);

    label = new JLabel("Assay type:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    assayTypes = new JComboBox<ComboItem>(dbr.getAssayTypes());
    c.gridx = 1;
    c.gridy = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(assayTypes, c);

    label = new JLabel("Plate layout:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 5;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    label = new JLabel(plate_layout.toString());
    c.gridx = 3;
    c.gridy = 5;
    c.gridwidth = 3;
    c.gridheight = 1;
    //c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(label, c);
    /*
    plateLayouts = new JComboBox<ComboItem>(dbr.getPlateLayoutNames(dbr.getPlateFormatID(format)));
    c.gridx = 3;
    c.gridy = 5;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(plateLayouts, c);
    */
    
    select =
        new JButton(
            "Select data file...", createImageIcon("/toolbarButtonGraphics/general/Open16.gif"));
    select.setMnemonic(KeyEvent.VK_O);
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

    checkBox=new JCheckBox("Auto-identify hits using algorithm:");
    c.gridx = 0;
    c.gridy = 7;
    c.gridwidth = 2;
    c.gridheight = 1;
    pane.add(checkBox, c);
    checkBox.addItemListener(new ItemListener() {
    public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange()==ItemEvent.SELECTED){
            algorithmList.setEnabled(true);
        }else if(e.getStateChange()==ItemEvent.DESELECTED){
            algorithmList.setEnabled(false);
        }
    }
	});

    ComboItem[] algorithmTypes = new ComboItem[]{ new ComboItem(4,">0% enhanced"), new ComboItem(3,"mean(background) + 3SD"), new ComboItem(2,"mean(background) + 2SD"), new ComboItem(1,"Top N")};
    algorithmList = new JComboBox<ComboItem>(algorithmTypes);
   
    c.gridx = 2;
    c.gridy = 7;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(algorithmList, c);
    algorithmList.setEnabled(false);
    algorithmList.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e){
	    switch(((ComboItem)algorithmList.getSelectedItem()).getKey()){
	    case 1:
		nLabel.setVisible(true);
		nField.setVisible(true);
		DialogAddPlateSetData.this.revalidate();
		DialogAddPlateSetData.this.repaint();
		
		break;
	    case 2:
		nLabel.setVisible(false);
		nField.setVisible(false);
		DialogAddPlateSetData.this.revalidate();
		DialogAddPlateSetData.this.repaint();
		break;
	    case 3:
		nLabel.setVisible(false);
		nField.setVisible(false);
		DialogAddPlateSetData.this.revalidate();
		DialogAddPlateSetData.this.repaint();
		break;		
	    }
	    }
	});


    nLabel = new JLabel("N:", SwingConstants.RIGHT);
    c.gridx = 3;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_END;
    nLabel.setVisible(false);

    pane.add(nLabel, c);

    
    nField = new JTextField(5);
    c.gridx = 4;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    nField.setVisible(false);
    //nField.getDocument().addDocumentListener(this);
    pane.add(nField, c);

    
    JButton helpButton = new JButton("Help");
    helpButton.setMnemonic(KeyEvent.VK_H);
    helpButton.setActionCommand("help");
    c.fill = GridBagConstraints.NONE;
    c.gridx = 5;
    c.gridy = 7;
    c.gridwidth = 1;
    c.gridheight = 1;
    pane.add(helpButton, c);
      try {
      ImageIcon help =
          new ImageIcon(this.getClass().getResource("/toolbarButtonGraphics/general/Help16.gif"));
      helpButton.setIcon(help);
    } catch (Exception ex) {
      System.out.println("Can't find help icon: " + ex);
    }
    helpButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
	          IFn getHelpURLPrefix = Clojure.var("lnrocks.core", "get-help-url-prefix");

		  openWebpage(URI.create((String)getHelpURLPrefix.invoke() + "importassaydata"));
          }
        });
    helpButton.setSize(10, 10);
    //; helpButton.setPreferredSize(new Dimension(5, 20));
    // helpButton.setBounds(new Rectangle(
    //             getLocation(), getPreferredSize()));
    //helpButton.setMargin(new Insets(1, -40, 1, -100)); //(top, left, bottom, right)

    
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
	if(((ComboItem)algorithmList.getSelectedItem()).getKey()==1){  //If Top N is the algorithm
	    try{
		top_n_number = Integer.parseInt(nField.getText());
	    }catch(NumberFormatException nfe){
		JOptionPane.showMessageDialog(dmf,
			      "Invalid number of hits.",
			      "Number Format Exception",
			      JOptionPane.ERROR_MESSAGE);
		return;
	    }
	    
	}
	 Task task = new Task(top_n_number);
	 progress_bar.main( new String[] {"Associating data with plate set."} );
	   task.execute();
	    
      // dbi.associateDataWithPlateSet(
      //     nameField.getText(),
      //     descrField.getText(),
      //     plate_set.toString(),
      //     format.getKey(),
      //     ((ComboItem)assayTypes.getSelectedItem()).getKey(),
      //     plate_layout.getKey(),
      //     dmf.getUtilities().loadDataFile(fileField.getText()),
      // 	  checkBox.isSelected(),
      // 	  ((ComboItem)algorithmList.getSelectedItem()).getKey(),
      // 	  top_n_number);
      dispose();
    }

    if (e.getSource() == select) {
      int returnVal = fileChooser.showOpenDialog(DialogAddPlateSetData.this);

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

    if (nameField.getText().length() > 0 & fileField.getText().length() > 0) {
      okButton.setEnabled(true);
    } else {
      okButton.setEnabled(false);
    }
  }

  public void removeUpdate(DocumentEvent e) {
    if (nameField.getText().length() > 0 & fileField.getText().length() > 0) {
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

      class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
	private int top_n_number;

    public Task(int _top_n_number) {
       this.top_n_number = _top_n_number;
       System.out.println("top n number: " + top_n_number);
    }

        @Override
        public Void doInBackground() {
	    dbi.associateDataWithPlateSet(
					  nameField.getText(),
					  descrField.getText(),
					  plate_set.toString(),
					  format.getKey(),
					  ((ComboItem)assayTypes.getSelectedItem()).getKey(),
					  plate_layout.getKey(),
					  dmf.getUtilities().loadDataFile(fileField.getText()),
					  checkBox.isSelected(),
					  ((ComboItem)algorithmList.getSelectedItem()).getKey(),
					  top_n_number);
       System.out.println("top n number: " + top_n_number);
       
	    return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor
	    progress_bar.dispose();
     }
    }



    
}
