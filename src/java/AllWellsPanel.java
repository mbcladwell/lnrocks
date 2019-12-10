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

public class AllWellsPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
   private IFn require = Clojure.var("clojure.core", "require");

  private CustomTable table;
  private JScrollPane scrollPane;
  private DialogMainFrame dmf;
    private DatabaseManager dbm;
  private JPanel textPanel;
  private String project_sys_name;
    // private Session session;
    
    public AllWellsPanel(DialogMainFrame _dmf, CustomTable _table, String _project_sys_name) {
    this.setLayout(new BorderLayout());
    
    dmf = _dmf;
    project_sys_name = _project_sys_name;
    // session = dmf.getSession();
    table = _table;

    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BorderLayout());
    headerPanel.add(new MenuBarForPlate(dmf, table), BorderLayout.NORTH);

    textPanel = new JPanel();
    textPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    JLabel label = new JLabel("Project:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.LINE_END;
    c.weightx = 0.1;
    c.insets = new Insets(5, 5, 2, 2);
    textPanel.add(label, c);
	
    label = new JLabel("Description:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 1;
    c.anchor = GridBagConstraints.LINE_END;
    textPanel.add(label, c);

    //LOGGER.info("table.getValueAt(0, 0)" + table.getValueAt(0, 0));

    //  plateset_sys_name =
    //  dbm.getDatabaseRetriever()
    //      .getPlateSetSysNameForPlateSysName((String) table.getValueAt(0, 0));
    JLabel projectLabel = new JLabel(project_sys_name, SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 0;
    c.gridwidth = 1;
    c.weightx = 0.9;

    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    textPanel.add(projectLabel, c);

           IFn getDescriptionForProject = Clojure.var("lnrocks.core", "get-project-desc");

    JLabel descriptionLabel =
        new JLabel((String)getDescriptionForProject.invoke(project_sys_name),
            SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 1;
    textPanel.add(descriptionLabel, c);

    headerPanel.add(textPanel, BorderLayout.CENTER);
    this.add(headerPanel, BorderLayout.NORTH);

    scrollPane = new JScrollPane(table);
    this.add(scrollPane, BorderLayout.CENTER);
    table.setFillsViewportHeight(true);
    FilterPanel fp = new FilterPanel(dmf, table, Integer.parseInt(project_sys_name.substring(4)) ,DialogMainFrame.ALLWELLS );
    this.add(fp, BorderLayout.SOUTH);
  }

  public JTable getTable() {
    return table;
  }

  public void updatePanel(String _project_sys_name) {
    String project_sys_name = _project_sys_name;
      int project_id = Integer.parseInt(project_sys_name.substring(4));

        IFn getAllWells = Clojure.var("lnrocks.core", "get-all-wells");

      //CustomTable table = session.getDatabaseManager().getProjectTableData();
	CustomTable table = (CustomTable)getAllWells.invoke(project_sys_name);


	// JTable table = dbm.getDatabaseRetriever().getDMFTableData(project_id, DialogMainFrame.ALLWELLS);
    TableModel model = table.getModel();
    this.table.setModel(model);
  }
}
