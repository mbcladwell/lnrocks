package lnrocks;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import clojure.java.api.Clojure;
import clojure.lang.IFn;


public class WellPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  private CustomTable table;
  private JScrollPane scrollPane;
  private DialogMainFrame dmf;
  private JPanel textPanel;
  private String plateset_sys_name;
  private String plate_sys_name;
    //   private Session session;    
    //  
    
  public WellPanel(DialogMainFrame _dmf, CustomTable _table) {
    this.setLayout(new BorderLayout());
   
    dmf = _dmf;
    table = _table;
    //  session = dmf.getSession();
    
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BorderLayout());
    headerPanel.add(new MenuBarForWell(dmf, table), BorderLayout.NORTH);

    textPanel = new JPanel();
    textPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    JLabel label = new JLabel("Plate Set:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.1;
    c.insets = new Insets(5, 5, 2, 2);
    textPanel.add(label, c);

    /*    label = new JLabel("Project: ", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    textPanel.add(label, c);
    
    
    JLabel projectLabel = new JLabel(dmf.getSession().getProjectSysName(), SwingConstants.LEFT);
    c.gridx = 3;
    c.gridy = 0;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    textPanel.add(projectLabel, c);
*/
	
    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_END;
    textPanel.add(label, c);

    IFn getPlateSetSysNameForPlateSysName = Clojure.var("lnrocks.core", "get-plate-set-sys-name-for-plate-sys-name");

    plate_sys_name = (String) table.getValueAt(1,1);
    plateset_sys_name = (String)getPlateSetSysNameForPlateSysName.invoke(plate_sys_name);

    
    JLabel platesetLabel = new JLabel(plateset_sys_name, SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.9;

    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    textPanel.add(platesetLabel, c);

       IFn getDescriptionForPlateSet = Clojure.var("lnrocks.core", "get-description-for-plate-set");

    
    JLabel descriptionLabel =
        new JLabel(
		   (String)getDescriptionForPlateSet.invoke(plateset_sys_name),
            SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 1;
    textPanel.add(descriptionLabel, c);

    headerPanel.add(textPanel, BorderLayout.CENTER);
    this.add(headerPanel, BorderLayout.NORTH);

    scrollPane = new JScrollPane(table);
    this.add(scrollPane, BorderLayout.CENTER);
    table.setFillsViewportHeight(true);
    FilterPanel fp = new FilterPanel(dmf, (JTable)table, Integer.parseInt(plate_sys_name.substring(4)) , DialogMainFrame.WELL );
    this.add(fp, BorderLayout.SOUTH);
  }

  public CustomTable getTable() {
    return table;
  }

    public void updatePanel(String _plate_sys_name) {
    String plate_sys_name = _plate_sys_name;
      int plate_id = Integer.parseInt(plate_sys_name.substring(4));

       IFn getWellsForPlate = Clojure.var("lnrocks.core", "get-wells-for-plate");
	CustomTable table = (CustomTable)getWellsForPlate.invoke(plate_id);
	//DefaultTableModel model = (DefaultTableModel) table.getModel();

      
    // JTable table = dbm.getDatabaseRetriever().getDMFTableData(plate_id, DialogMainFrame.WELL);
     TableModel model = table.getModel();
    this.table.setModel(model);
  }
}
