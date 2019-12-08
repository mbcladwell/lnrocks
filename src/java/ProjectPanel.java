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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public class ProjectPanel extends JPanel {

  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private IFn require = Clojure.var("clojure.core", "require");

  private CustomTable table;
  private JScrollPane scrollPane;
  private DialogMainFrame dmf;
    private JPanel textPanel;
  private ListSelectionModel listSelectionModel;
    //     private Session session;
      // private SharedListSelectionHandler sharedListSelectionHandler;
  /**
   * To accomodate all components place panels within panels.
   *
   * <p>HeaderPanel<br>
   * NORTH: menubar<br>
   * CENTER: textPanel<br>
   *
   * <p>FooterPanel<br>
   * NORTH: table<br>
   * SOUTH: filter<br>
   */
  public ProjectPanel(DialogMainFrame _dmf, CustomTable _table) {
    this.setLayout(new BorderLayout());
   
    dmf = _dmf;
    table = _table;
    require.invoke(Clojure.read("lnrocks.core"));
       IFn getSessionID = Clojure.var("lnrocks.core", "get-session-id");
       //session = (int)getSessionID.invoke();

    /*
        listSelectionModel = table.getSelectionModel();
        sharedListSelectionHandler = new SharedListSelectionHandler();
        listSelectionModel.addListSelectionListener(sharedListSelectionHandler);
        table.setSelectionModel(listSelectionModel);
        table.setRowSelectionAllowed(true);


        // table.getColumnModel().getColumn(0).setMinWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(75);
        table.getColumnModel().getColumn(1).setMaxWidth(150);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
    */
    //    LOGGER.info("table: " + table);
    // LOGGER.info("class: " + table.getClass());

    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BorderLayout());
     headerPanel.add(new MenuBarForProject(dmf, table), BorderLayout.NORTH);

    textPanel = new JPanel();
    textPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    JLabel label = new JLabel("User:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.1;
    c.anchor = GridBagConstraints.LINE_END;
    c.insets = new Insets(5, 5, 2, 2);
    textPanel.add(label, c);

    label = new JLabel("Group:", SwingConstants.RIGHT);
    c.gridy = 1;
    textPanel.add(label, c);

         IFn getUser = Clojure.var("lnrocks.core", "get-user");

    	 JLabel userLabel = new JLabel((String)getUser.invoke(), SwingConstants.LEFT);
     c.gridx = 1;
     c.gridy = 0;
    // c.gridwidth = 3;
    c.weightx = 0.9;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    textPanel.add(userLabel, c);

        IFn getUserGroup = Clojure.var("lnrocks.core", "get-user-group");

    	JLabel groupLabel = new JLabel((String)getUserGroup.invoke(), SwingConstants.LEFT);
    c.gridx = 1;
    c.gridy = 1;
    textPanel.add(groupLabel, c);
    headerPanel.add(textPanel, BorderLayout.CENTER);

    this.add(headerPanel, BorderLayout.NORTH);

    scrollPane = new JScrollPane(table);
    this.add(scrollPane, BorderLayout.CENTER);
    table.setFillsViewportHeight(true);
       FilterPanel fp = new FilterPanel(dmf, table, 0, DialogMainFrame.PROJECT );
     this.add(fp, BorderLayout.SOUTH);
  }

  public JTable getTable() {
    return table;
  }

  public void updatePanel() {
        IFn getAllProjects = Clojure.var("lnrocks.core", "get-all-projects");

      //CustomTable table = session.getDatabaseManager().getProjectTableData();
	CustomTable table = (CustomTable)getAllProjects.invoke();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    this.table.setModel(model);
  }

  /*
  class SharedListSelectionHandler implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {

      boolean isAdjusting = e.getValueIsAdjusting();
      if (!isAdjusting) {
        listSelectionModel.removeListSelectionListener(sharedListSelectionHandler);
        // ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        CustomTableModel tm = (CustomTableModel) ProjectPanel.this.getTable().getModel();

        // Find out which indexes are selected.
        int minIndex = listSelectionModel.getMinSelectionIndex();
        int maxIndex = listSelectionModel.getMaxSelectionIndex();
        for (int i = minIndex; i <= maxIndex; i++) {
          if (listSelectionModel.isSelectedIndex(i)) {}
          // ProjectPanel.this.table.setModel(tm);
        }
      }
      listSelectionModel.addListSelectionListener(sharedListSelectionHandler);
    }
  }
  */
}

// https://stackoverflow.com/questions/2668547/stackoverflowerror-being-caused-by-a-tablemodellistener

// https://stackoverflow.com/questions/10679425/multiple-row-selection-with-checkbox-in-jtable
