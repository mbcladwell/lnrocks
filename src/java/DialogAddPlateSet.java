
package lnrocks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*; 
import java.util.Vector; 
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;
import java.beans.*;
import javax.swing.*;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.PersistentHashSet;

public class DialogAddPlateSet extends JDialog   {
    static JButton button;
    static JLabel label;
    static JLabel Description;
    static JTextField nameField;
    static JLabel ownerLabel;
    static String owner;
    static JTextField descriptionField;
    static JTextField numberField;
    static JComboBox<Integer> formatList;
    static JComboBox<String> typeList;
    private ComboItem [] layoutNames;
    private JComboBox<ComboItem> layoutList;
    private DefaultComboBoxModel<ComboItem> layout_names_list_model;
    private ProgressBar progress_bar;
    
    static JButton okButton;
    static JButton cancelButton;
    final Instant instant = Instant.now();
    final DialogMainFrame dmf;
    //final 
    //   private Task task;
    //    final Session session;
    final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    // final EntityManager em;
    private IFn require = Clojure.var("clojure.core", "require");

  public DialogAddPlateSet(DialogMainFrame _dmf) {
      
      this.dmf = _dmf;
    require.invoke(Clojure.read("lnrocks.core"));
     IFn getUser = Clojure.var("lnrocks.core", "get-user");
      //this.session = dmf.getSession();
     owner = (String)getUser.invoke();
    // Create and set up the window.
    // JFrame frame = new JFrame("Add Project");
    // this.em = em;
    JPanel pane = new JPanel(new GridBagLayout());
    pane.setBorder(BorderFactory.createRaisedBevelBorder());
    progress_bar = new ProgressBar();

    GridBagConstraints c = new GridBagConstraints();
    // Image img = new
    // ImageIcon(DialogAddProject.class.getResource("../resources/mwplate.png")).getImage();
    // this.setIconImage(img);
    this.setTitle("Add a Plate Set");
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
    c.gridx = 1;
    c.gridy = 3;
    c.gridheight = 1;
    pane.add(descriptionField, c);

    numberField = new JTextField(4);
    c.gridx = 1;
    c.gridy = 4;
    c.gridheight = 1;
    c.gridwidth = 1;
    pane.add(numberField, c);

    label = new JLabel("Format:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_END;
    pane.add(label, c);

    Integer[] formats = {96, 384, 1536};

    formatList = new JComboBox<Integer>(formats);
    formatList.setSelectedIndex(0);
    c.gridx = 3;
    c.gridy = 4;
    c.gridheight = 1;
    c.anchor = GridBagConstraints.LINE_START;
    formatList.addActionListener(
				 (new ActionListener() {
					 public void actionPerformed(ActionEvent e) {
					     layoutNames = getSourcePlateLayoutNames((int)formatList.getSelectedItem(), "assay");
					     layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutNames );
					     layoutList.setModel(layout_names_list_model );
					     layoutList.setSelectedIndex(-1);
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

    IFn getPlateTypes = Clojure.var("lnrocks.core", "get-plate-types");
    ArrayList<String> plate_types_pre =  new ArrayList<String>((PersistentVector)getPlateTypes.invoke());
    String[] plate_types = new String[plate_types_pre.size()];
    for (int j = 0; j < plate_types_pre.size(); j++) { 
              plate_types[j] = plate_types_pre.get(j); 
        } 

    typeList = new JComboBox<String>(plate_types);
     typeList.setSelectedIndex(0);
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


    layoutNames = getSourcePlateLayoutNames((int)formatList.getSelectedItem(), (String)typeList.getSelectedItem());
    //System.out.println("layoutNames: " + layoutNames.size());
    layout_names_list_model = new DefaultComboBoxModel<ComboItem>( layoutNames );
    layoutList = new JComboBox<ComboItem>();
    layoutList.setModel(layout_names_list_model );
    layoutList.setSelectedIndex(0);
    c.gridx = 1;
    c.gridy = 5;
    c.gridheight = 1;
    c.gridwidth = 3;
    c.anchor = GridBagConstraints.LINE_START;
    pane.add(layoutList, c);
    
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
	      Task task = new Task();
	      	     IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");

	      //task.addPropertyChangeListener(this);
	    progress_bar.main( new String[] {"Creating Plate Set"} );
	      task.execute();
	      // dbm
	      // 	    .getDatabaseInserter().insertPlateSet(
              //       nameField.getText(),
              //       descriptionField.getText(),
              //       Integer.valueOf(numberField.getText()),
              //       Integer.valueOf(formatList.getSelectedItem().toString()),
              //       ((ComboItem)typeList.getSelectedItem()).getKey(),
	      // 	    ((Long)getProjectID.invoke()).intValue(),
	      // 	    ((ComboItem)layoutList.getSelectedItem()).getKey(),
	      // 						  true);
	      //progress_bar.setVisible(false);
	      
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

    //    getSourcePlateLayoutNames(96, "assay");
    this.getContentPane().add(pane, BorderLayout.CENTER);
    this.pack();
    this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);
  }

      /**
     * Need only source layouts for a given format;
     * If the destination plate is an assay plate (plate_type="assay"), replicate info must be provided
     */   
    public ComboItem[] getSourcePlateLayoutNames(int format_id, String plate_type) {
    ComboItem[] output = null;
    //    Array results = null;
    ArrayList<ComboItem> combo_items = new ArrayList<ComboItem>();
    IFn getPlateLayouts = Clojure.var("lnrocks.core", "get-source-plate-layout-names");
    PersistentHashSet src_plt_lyts = (PersistentHashSet)getPlateLayouts.invoke(format_id);
    Iterator value = src_plt_lyts.iterator(); 

    while (value.hasNext()) {
	PersistentVector pv = (PersistentVector)value.next();
	  
	if(plate_type=="assay"){
	    combo_items.add(new ComboItem((int)pv.nth(0), new String(pv.nth(1) + ";" + pv.nth(2) )));	 
	}else{
	    combo_items.add(new ComboItem((int)pv.nth(0), (String)pv.nth(1)));
	}		
    }
    
    output = combo_items.toArray(new ComboItem[combo_items.size()]);
    
    return (ComboItem[])output;
  }

         class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
	     IFn getProjectID = Clojure.var("lnrocks.core", "get-project-id");
   
        @Override
        public Void doInBackground() {
	    IFn newPlateSet = Clojure.var("lnrocks.core", "new-plate-set");
	    newPlateSet.invoke(nameField.getText(),
				descriptionField.getText(),
			    (int)Integer.valueOf(numberField.getText()),
			    (int)Integer.valueOf(formatList.getSelectedItem().toString()),
			    (String)typeList.getSelectedItem(),
				((Long)getProjectID.invoke()).intValue(),
				((ComboItem)layoutList.getSelectedItem()).getKey(),
				true);
	
	   
            return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            setCursor(null); //turn off the wait cursor
	    progress_bar.setVisible(false);
	    IFn getProjectSysName = Clojure.var("lnrocks.core", "get-project-sys-name");
	    dmf.showPlateSetTable((String)getProjectSysName.invoke());
            dispose();
	    //  System.out.println("complete done in swingworker in DialogAddPlateSet");
     }
    }
       
  
}

