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
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

/**
 * Called from  DatabaseManager.reformatPlateSet(table)
 * Calls DatabaseInserter.reformatPlateSet()

 */
public class DialogReformatPlateSet extends JDialog implements ActionListener {
  static JButton button;
  static JLabel label;
  static JLabel Description;
   public JLabel predictedNumberOfPlatesLabel;
    public JLabel sourceLayoutLabel;
  static JTextField nameField;
  static JLabel ownerLabel;
  static String owner;
  static JTextField descriptionField;
  static JTextField numberField;
    public ComboItem[] source_layout;
    static JComboBox<Integer> formatList;
    static JComboBox<ComboItem> typeList;
    static JComboBox<ComboItem> layoutList;
    static JComboBox<ComboItem> sampleRepsList;
    static JComboBox<ComboItem> targetRepsList;
    public ComboItem[] target_reps;
    public ComboItem[] sample_reps;
  static JButton okButton;
    static JButton cancelButton;
    final Instant instant = Instant.now();
    final DialogMainFrame dmf;
    //    final Session session;
    final DatabaseManager dbm;
    
    final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
    public int predicted_number_of_plates_int;
public int new_plate_format_id;
    public int old_num_samples;
    public int old_num_plates;
    private DefaultComboBoxModel<ComboItem> layout_names_list_model;
    private DefaultComboBoxModel<ComboItem> target_reps_list_model;
    public int old_plate_layout_name_id;
    public int old_plate_set_id;
    
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");
  // final EntityManager em;

  /**
   * Called from DatabaseInserter.reformatPlateSet()
   *
   * @param _plate_set_num_plates plate set id and the number of plates in the plate set
   * @param _format number of wells per plate
   */
  public DialogReformatPlateSet(
      DialogMainFrame dmf,
      int _plate_set_id,
      String _plate_set_sys_name,
      String _descr,
      int _num_plates,
      int _num_samples,
      String _plate_type,
      String _plate_format,
      int _plate_layout_name_id) {
      dbm = _dbm;
      //this.session = _session;
      this.dmf = dmf;
    require.invoke(Clojure.read("lnrocks.core"));
     IFn getUser = Clojure.var("lnrocks.core", "get-user");
     owner = (String)getUser.invoke();
    old_plate_set_id = _plate_set_id;
    //  HashMap<String, String> plate_set_num_plates = _plate_set_num_plates;
    String old_plate_format = _plate_format;
    String new_plate_format = _plate_format;
    String plate_set_sys_name = _plate_set_sys_name;
    String old_descr =_descr;
    old_num_plates = _num_plates;
    old_num_samples = _num_samples;
    String old_plate_type =  _plate_type;
    old_plate_layout_name_id = _plate_layout_name_id;
 
    switch(old_plate_format){
    case "96":
	new_plate_format = "384";
	new_plate_format_id = 384;
	break;
  case "384":
	new_plate_format = "1536";
	new_plate_format_id = 1536;
	break;
  case "1536":
      JOptionPane.showMessageDialog(dmf, "1536 well plates cannot be reformated", "Error", JOptionPane.ERROR_MESSAGE);
	break;
    }


    //assemble old plate layout label
    ComboItem old_plate_layout_name = dbm.getDatabaseRetriever().getPlateLayoutNameAndID(old_plate_layout_name_id);
   
    
    predicted_number_of_plates_int = (int)Math.ceil(old_num_plates/4.0);
    String predicted_number_of_plates = String.valueOf(predicted_number_of_plates_int);
	

    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    
    pane.setBorder(BorderFactory.createRaisedBevelBorder());

     JPanel pane1 = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Reformat Plate Set " + plate_set_sys_name);
   
    label = new JLabel("Date:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;
 c.anchor = GridBagConstraints.LINE_END;

    c.insets = new Insets(5, 5, 2, 2);
    pane1.add(label, c);

    label = new JLabel(df.format(Date.from(instant)));
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_START;
    pane1.add(label, c);

    label = new JLabel("Owner:", SwingConstants.RIGHT);
    c.gridx = 4;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    pane1.add(label, c);

    ownerLabel = new JLabel(owner);
    c.gridx = 5;
    c.gridy = 0;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane1.add(ownerLabel, c);


    
javax.swing.border.TitledBorder source = BorderFactory.createTitledBorder("Source Plate Set:");
    source.setTitlePosition(javax.swing.border.TitledBorder.TOP);

    JPanel pane2 = new JPanel(new GridBagLayout());
    pane2.setBorder(source);

    label = new JLabel("System Name:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

    JLabel oldNameLabel = new JLabel(plate_set_sys_name, SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 1;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(oldNameLabel, c);

    
    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

JLabel oldDescrLabel = new JLabel(old_descr, SwingConstants.RIGHT);
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(oldDescrLabel, c);

    
       label = new JLabel("# plates:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

    label = new JLabel(Integer.valueOf(old_num_plates).toString(), SwingConstants.RIGHT);
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);

           label = new JLabel("# samples:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

    label = new JLabel(Integer.valueOf(old_num_samples).toString(), SwingConstants.RIGHT);
    c.gridx = 3;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);

    label = new JLabel("Plate Type:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

    label = new JLabel(old_plate_type, SwingConstants.RIGHT);
    c.gridx = 1;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);

    label = new JLabel("Format:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);
  
    label = new JLabel(old_plate_format + " well");

    c.gridx = 3;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);
  
    label = new JLabel("Plate Layout:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane2.add(label, c);

    label = new JLabel(old_plate_layout_name.toString() );

    c.gridx = 1;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_START;
    pane2.add(label, c);
  
    
javax.swing.border.TitledBorder dest = BorderFactory.createTitledBorder("Destination Plate Set:");
    dest.setTitlePosition(javax.swing.border.TitledBorder.TOP);

    JPanel pane3 = new JPanel(new GridBagLayout());
    pane3.setBorder(dest);
    
    label = new JLabel("New Plate Set Name:", SwingConstants.RIGHT);
    // c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 2;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
    
    nameField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 2;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(nameField, c);
  
    label = new JLabel("New Plate Set Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 1;
    pane3.add(label, c);
  
    descriptionField = new JTextField(30);
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(descriptionField, c);
 
    label = new JLabel("Predicted # plates:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
 
    predictedNumberOfPlatesLabel = new JLabel(predicted_number_of_plates);
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 5;
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(predictedNumberOfPlatesLabel, c);
 
    label = new JLabel("New Plate Set Type:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
 
    ComboItem[] plateTypes = dbm.getDatabaseRetriever().getPlateTypes();

    typeList = new JComboBox<ComboItem>(plateTypes);
    typeList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(typeList, c);

    
    label = new JLabel("Format:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
  
    label = new JLabel(new_plate_format + " well");

    c.gridx = 3;
    c.gridy = 6;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(label, c);

   label = new JLabel("Sample replication:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
 
    sample_reps = new ComboItem[]{ new ComboItem(1,"1"), new ComboItem(2,"2"), new ComboItem(4,"4")};

    sampleRepsList = new JComboBox<ComboItem>(sample_reps);
    sampleRepsList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    sampleRepsList.addActionListener(this);
    pane3.add(sampleRepsList, c);

   label = new JLabel("Target replication:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
 
    target_reps = new ComboItem[]{ new ComboItem(4,"4")};
target_reps_list_model =  new DefaultComboBoxModel<ComboItem>( target_reps );
    
    targetRepsList = new JComboBox<ComboItem>(target_reps);
    targetRepsList.setModel(target_reps_list_model);
    targetRepsList.setSelectedIndex(0);
    c.gridx = 3;
    c.gridy = 7;
    c.gridheight = 1;
    c.gridwidth = 1;
    targetRepsList.addActionListener(this);
    c.anchor = GridBagConstraints.LINE_START;
    pane3.add(targetRepsList, c);

    
        label = new JLabel("New Plate Set Layout:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 8;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane3.add(label, c);
 
    source_layout = dbm.getDatabaseRetriever().getLayoutDestinationsForSourceID(old_plate_layout_name_id,1,4);
    //layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutTypes );

    
    sourceLayoutLabel = new JLabel(source_layout[0].toString());
    //layoutList.setModel(layout_names_list_model);
    //layoutList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 8;
    c.gridheight = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_START;
    //layoutList.addActionListener(this);
    pane3.add(sourceLayoutLabel, c);

    JPanel pane4 = new JPanel(new GridBagLayout());

    
    okButton = new JButton("OK");
    okButton.setMnemonic(KeyEvent.VK_O);
    okButton.setActionCommand("ok");
    okButton.setEnabled(true);
    okButton.setForeground(Color.GREEN);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 2;
    c.gridy = 9;
    c.gridwidth = 2;
    c.gridheight = 1;
    okButton.addActionListener(this);
    pane4.add(okButton, c);

    cancelButton = new JButton("Cancel");
    cancelButton.setMnemonic(KeyEvent.VK_C);
    cancelButton.setActionCommand("cancel");
    cancelButton.setEnabled(true);
    cancelButton.setForeground(Color.RED);
    c.gridx = 1;
    c.gridy = 9;
    c.gridwidth = 1;
    pane4.add(cancelButton, c);
    cancelButton.addActionListener(
        (new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dispose();
          }
        }));

        pane.add(pane1);
        pane.add(pane2);
        pane.add(pane3);
        pane.add(pane4);

    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

      public void actionPerformed(ActionEvent e) {

    if (e.getSource() == okButton) {

	dbm
	    .getDatabaseInserter()
	    .reformatPlateSet(old_plate_set_id,
			      dmf,
			      nameField.getText(),
			      old_num_plates,
			      descriptionField.getText(),			      
			      new_plate_format_id,
			      ((ComboItem)typeList.getSelectedItem()).getKey(),
			      source_layout[0].getKey(),			      
			      ((ComboItem)sampleRepsList.getSelectedItem()).getKey()
			      
	    );
	
            dispose();


    }

     if (e.getSource() == sampleRepsList) {
	 int number_of_sample_replicates = ((ComboItem)sampleRepsList.getSelectedItem()).getKey();
	 predicted_number_of_plates_int = ((int)Math.ceil(old_num_plates*number_of_sample_replicates/4.0));
	 predictedNumberOfPlatesLabel.setText(String.valueOf(predicted_number_of_plates_int));

	 switch(number_of_sample_replicates){
	 case 1:
	    
	      targetRepsList.setModel(new DefaultComboBoxModel<ComboItem>( new ComboItem[]{ new ComboItem(4,"4")}  ));
	      targetRepsList.setSelectedIndex(0);
	      targetRepsList.setEnabled(false);
	      	ComboItem[] layoutTypes = dbm.getDatabaseRetriever().getLayoutDestinationsForSourceID(old_plate_layout_name_id,number_of_sample_replicates,((ComboItem)targetRepsList.getSelectedItem()).getKey());
		//layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutTypes  );
		//layoutList.setModel(layout_names_list_model);
		//layoutList.setSelectedIndex(0);
	      sourceLayoutLabel.setText(layoutTypes[0].toString());
	     break;
	 case 2:
	    targetRepsList.setModel(new DefaultComboBoxModel<ComboItem>( new ComboItem[]{ new ComboItem(2,"2"), new ComboItem(4,"4")}  ));
	      targetRepsList.setSelectedIndex(0);
	      targetRepsList.setEnabled(true);
	       layoutTypes = dbm.getDatabaseRetriever().getLayoutDestinationsForSourceID(old_plate_layout_name_id,number_of_sample_replicates,((ComboItem)targetRepsList.getSelectedItem()).getKey());
	       //layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutTypes );
	       //layoutList.setModel(layout_names_list_model);
	       //layoutList.setSelectedIndex(0);
	      sourceLayoutLabel.setText(layoutTypes[0].toString());
	     break;
	 case 4:
	     targetRepsList.setModel(new DefaultComboBoxModel<ComboItem>( new ComboItem[]{ new ComboItem(1,"1"), new ComboItem(2,"2") }  ));
	  
	     targetRepsList.setSelectedIndex(0);
	      targetRepsList.setEnabled(true);
	       layoutTypes = dbm.getDatabaseRetriever().getLayoutDestinationsForSourceID(old_plate_layout_name_id,number_of_sample_replicates,((ComboItem)targetRepsList.getSelectedItem()).getKey());
	       //layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutTypes  );
	       //layoutList.setModel(layout_names_list_model);
	       //layoutList.setSelectedIndex(0);
	      sourceLayoutLabel.setText(layoutTypes[0].toString());
	     break;
	     
	 }
	 
     }

         if (e.getSource() == targetRepsList) {
	    
	      	ComboItem[] layoutTypes = dbm.getDatabaseRetriever().getLayoutDestinationsForSourceID(old_plate_layout_name_id,((ComboItem)sampleRepsList.getSelectedItem()).getKey(),((ComboItem)targetRepsList.getSelectedItem()).getKey());
	
	      sourceLayoutLabel.setText(layoutTypes[0].toString());
	 

     }
	 }
	 /*
    if (e.getSource() == layoutList) {
	
	int selected_layout = ((ComboItem)layoutList.getSelectedItem()).getKey();

	int number_of_sample_replicates = dbm.getDatabaseRetriever().getNumberOfReplicatesForPlateLayout(selected_layout);
	
	predicted_number_of_plates_int = (int)(old_num_plates*number_of_sample_replicates)/4;
     predictedNumberOfPlatesLabel.setText(String.valueOf(predicted_number_of_plates_int));
	     
     }*/
      }

      
