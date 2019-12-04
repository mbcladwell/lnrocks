package lnrocks;
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.awt.Toolkit;

/* Creates an indeterminite progress bar to be used across swing workers

 */
public class ProgressBar extends JFrame implements 
					    PropertyChangeListener {
    private JProgressBar progressBar;
    //private Task task;
 
 
    public ProgressBar() {
        this.setLayout(new BorderLayout());
       progressBar = new JProgressBar(0, 100);
        //progressBar.setValue(0);
        //progressBar.setStringPainted(true);
	progressBar.setIndeterminate(true);
       
        JPanel panel = new JPanel();
        panel.add(progressBar, BorderLayout.PAGE_START);
 
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
	panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
	this.add(panel, BorderLayout.PAGE_START);	
    }
 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
      
    } 

    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private void createAndShowGUI(String frame_title) {
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	this.setTitle(frame_title);
	this.setPreferredSize(new Dimension( 200 + frame_title.length()*5, 70 ));
	this.setResizable(false);
        this.pack();
	this.setLocation(
        (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - getWidth() / 2,
        (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - getHeight() / 2);
    this.setVisible(true);

    }
 
    public void main(String[] frame_title) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI( frame_title[0]);
            }
        });
    }
}
