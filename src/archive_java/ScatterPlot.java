/*
 * ScatterPlot.java
 * http://forums.devshed.com/java-help-9/java-scatterplot-121767.html
 *
 * http://zetcode.com/java/postgresql/
 */
package lnrocks;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clojure.java.api.Clojure;
import clojure.lang.IFn;


public class ScatterPlot extends JFrame {
    private JButton genHitsBtn = new JButton("Generate hit list");
    private JButton help_button = new JButton("Help");
    private DatabaseRetriever dbr;
    private DialogMainFrame dmf;
    private CustomTable table;
    private Set<Integer> plate_set = new HashSet<Integer>();
    private Set<Integer> well_set = new HashSet<Integer>();
    private List<Double> norm_list = new LinkedList<Double>();
    private List<Double> bkgrnd_list = new LinkedList<Double>();
        static JComboBox<ComboItem> algorithmList;
    static JComboBox<ComboItem> responseList;
    private JTextField thresholdField;
    private int format;
    private double max_response;
    private double min_response;
    private double mean_bkgrnd;
    private double stdev_bkgrnd;
    private double threshold;
    private double mean_neg_3_sd;
    private double mean_neg_2_sd;
    private double mean_pos;
    
    private double[][] sortedResponse;
    private double[][] sorted_response_unknowns_only;
    private int margin = 60;
    private int wth;
    private int hgt;	  
    private double originX;
    private double originY;	
    private double scaleX; 
    private double scaleY;
    private int num_hits=0;
    //private DefaultTableModel dtm;
    private ResponseWrangler raw_response;
    public JTextField numHitsField;
    private ResponseWrangler norm_response;
    private ResponseWrangler norm_pos_response;
    private ResponseWrangler selected_response;
    private ResponseWrangler p_enhanced_response;
    private int assay_run_id;
    private ScatterPlotSlider slider;

    private int num_plates = 0;
    private DecimalFormat df = new DecimalFormat("#####.####");
    // private DecimalFormat df2 = new DecimalFormat("##.####");
    private JPanel panel;
    private JPanel panel2;
    private JPanel panel3;
    //    private Session session;
        private IFn require = Clojure.var("clojure.core", "require");
    private DatabaseManager dbm;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public ScatterPlot(DialogMainFrame dmf, int _assay_run_id) {
	super("Scatter Plot for AR-" + String.valueOf(_assay_run_id));
	dbm = _dbm;
	setSize(800, 600);
	//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setLayout(new BorderLayout());
	this.dmf = dmf;
	//session = dmf.getSession();
	assay_run_id = _assay_run_id;
	//need the assay run id
	table = dbm.getDatabaseRetriever().getDataForScatterPlot(assay_run_id);
	//LOGGER.info("row count: " + table.getRowCount());	    

	raw_response = new ResponseWrangler(table, ResponseWrangler.RAW);
	norm_response = new ResponseWrangler(table, ResponseWrangler.NORM);
	norm_pos_response = new ResponseWrangler(table, ResponseWrangler.NORM_POS);
	p_enhanced_response = new ResponseWrangler(table, ResponseWrangler.P_ENHANCE);

	selected_response = norm_response;
	threshold = selected_response.getThreshold();
	//updateAllVariables();
	min_response = selected_response.getMin_response();
	max_response = selected_response.getMax_response();
	
	slider = new ScatterPlotSlider(min_response, max_response, threshold, 100, this);

    
	panel2 = new JPanel(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();

	JLabel label = new JLabel("Algorithm:", SwingConstants.RIGHT);
	c.gridx = 0;
	c.gridy = 1;
	c.gridheight = 1;
	c.gridwidth = 1;
	c.insets = new Insets(5, 5, 2, 2);
	c.anchor = GridBagConstraints.LINE_END;
	panel2.add(label, c);

	ComboItem[] algorithmTypes = new ComboItem[]{ new ComboItem(3,"mean(neg) + 3SD"), new ComboItem(2,"mean(neg) + 2SD"), new ComboItem(1,"> mean(pos)")};
    
	algorithmList = new JComboBox<ComboItem>(algorithmTypes);
	algorithmList.setSelectedIndex(0);
	c.gridx = 1;
	c.gridy = 1;
	c.gridheight = 1;
	c.gridwidth = 1;
	c.anchor = GridBagConstraints.LINE_START;
	panel2.add(algorithmList, c);
	algorithmList.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent evt) {
		    //   LOGGER.info("Algorithm event fired");
	    switch(((ComboItem)algorithmList.getSelectedItem()).getKey()){
	    case 3:
		setThreshold( mean_neg_3_sd);		
		//updateAllVariables();
		break;
	    case 2:
		setThreshold( mean_neg_2_sd);		
		//updateAllVariables();
		break;
	    case 1:
		setThreshold( mean_pos);		
		//updateAllVariables();
		break;
	    }
        }
    });


    
     label = new JLabel("Threshold:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel2.add(label, c);


    thresholdField = new JTextField(Double.toString(threshold), 10);
    c.gridx = 3;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    panel2.add(thresholdField, c);
    thresholdField.addActionListener(new ActionListener() { 
        public void actionPerformed(ActionEvent evt) {
	    setThreshold(Double.valueOf(thresholdField.getText()));	    
            
        }
    });

    
    c.gridx = 4;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    c.fill = GridBagConstraints.HORIZONTAL;
    panel2.add(genHitsBtn, c);
    genHitsBtn.addActionListener(new ActionListener() { 
        public void actionPerformed(ActionEvent evt) {
            new DialogNewHitList(dbm, assay_run_id, sortedResponse, num_hits);
        }
    });

        c.gridx = 4;
    c.gridy = 1;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel2.add(help_button, c);
    help_button.addActionListener(new ActionListener() { 
        public void actionPerformed(ActionEvent evt) {
              IFn openHelpPage = Clojure.var("ln.session", "open-help-page");
	      openHelpPage.invoke( "scatterplotviewer");

        }
    });

 label = new JLabel("Response:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel2.add(label, c);

    ComboItem[] responseTypes = new ComboItem[]{ new ComboItem(1,"raw"), new ComboItem(2,"norm"), new ComboItem(3,"norm_pos"), new ComboItem(4,"% enhancement")};
    
    responseList = new JComboBox<ComboItem>(responseTypes);
    responseList.setSelectedIndex(1);
    c.gridx = 1;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;
    panel2.add(responseList, c);
    responseList.addActionListener(new ActionListener() { 
        public void actionPerformed(ActionEvent evt) {
	    switch(((ComboItem)responseList.getSelectedItem()).getKey()){
	    case 1:
		algorithmList.setEnabled(true);
		algorithmList.setSelectedIndex(0); 
		selected_response = raw_response;
		setThreshold(raw_response.getThreshold());
		break;
	    case 2:
		algorithmList.setEnabled(true);
		algorithmList.setSelectedIndex(0); 
		selected_response = norm_response;
		setThreshold(norm_response.getThreshold());
		break;
	    case 3:
		algorithmList.setEnabled(true);
		algorithmList.setSelectedIndex(0); 
		selected_response = norm_pos_response;
		setThreshold(norm_pos_response.getThreshold());
		break;
	    case 4:
		selected_response = p_enhanced_response;
		setThreshold(p_enhanced_response.getThreshold());
		algorithmList.setSelectedIndex(2); //sets to > mean(pos)
		algorithmList.setEnabled(false);
		break;
	    }
        }
    });

    
     label = new JLabel("Number of hits:", SwingConstants.RIGHT);
    c.gridx = 2;
    c.gridy = 1;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel2.add(label, c);

    numHitsField = new JTextField(String.valueOf(num_hits), 10);
    c.gridx = 3;
    c.gridy = 1;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_START;

    /**
     * get the sorted response: double[][]  sorted_response [response] [well] [type_id] [sample_id]
     * the needed threshold for e.g. 26 hits will be sorted_response[26][0]
     */
    panel2.add(numHitsField, c);
    numHitsField.addActionListener(new ActionListener() { 
        public void actionPerformed(ActionEvent evt) {
	    num_hits = Integer.valueOf(numHitsField.getText()).intValue();
	    //sorted_response_unknowns_only = selected_response.getSortedResponseUnknownsOnly();
	    // LOGGER.info("num_hits: " + num_hits);
	    //LOGGER.info("sorted_response.length: " + sorted_response_unknowns_only.length);	    
	    //LOGGER.info("sorted_response_unk_only[num_hits][0]: " + sorted_response_unknowns_only[num_hits][0]);
       	
	    setThreshold(sorted_response_unknowns_only[num_hits][0]);	    
            
        }
    });

    getContentPane().add(panel2, BorderLayout.SOUTH);

    panel3 = new JPanel(new GridBagLayout());
 
    label = new JLabel("Legend:", SwingConstants.RIGHT);
    c.gridx = 0;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel3.add(label, c);

    label = new JLabel("positive", SwingConstants.RIGHT);
    label.setForeground(Color.green);
    c.gridx = 1;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel3.add(label, c);

    label = new JLabel("negative", SwingConstants.RIGHT);
    label.setForeground(Color.red);
    c.gridx = 2;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel3.add(label, c);

    label = new JLabel("unknown", SwingConstants.RIGHT);
    c.gridx = 3;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel3.add(label, c);

    label = new JLabel("blank", SwingConstants.RIGHT);
    label.setForeground(Color.gray);
    c.gridx = 4;
    c.gridy = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.LINE_END;
    panel3.add(label, c);

    getContentPane().add(panel3, BorderLayout.NORTH);
    
    panel = new JPanel() {
	    	    
	    public void paintComponent(Graphics g) {

		format = selected_response.getFormat();
		num_plates = selected_response.getNum_plates();
		max_response = selected_response.getMax_response();
		min_response = selected_response.getMin_response();
		mean_bkgrnd = selected_response.getMean_bkgrnd();
		stdev_bkgrnd = selected_response.getStdev_bkgrnd();
		mean_neg_3_sd = selected_response.getMean_neg_3_sd();
		mean_neg_2_sd = selected_response.getMean_neg_2_sd();
		mean_pos = selected_response.getMean_pos();
		sortedResponse = selected_response.getSortedResponse();
		sorted_response_unknowns_only = selected_response.getSortedResponseUnknownsOnly();
		num_hits = selected_response.getHitsAboveThreshold(threshold);

		// Plot points below.
		Font font = new Font(null, Font.PLAIN, 12);    
		g.setFont(font);
		wth = getWidth();
		hgt = getHeight();
		originX = margin;

		if(	selected_response == p_enhanced_response ){
	       //origin in the middle of the Y axis
		    originY = (getHeight() - margin)/2;
		    scaleY = (hgt-margin)/280; //140% on either side of the origin  
		}else{  //origin lower left corner
		    originY = getHeight() - margin;
		    scaleY = (hgt-margin)/max_response;  
		}
	
		scaleX = (wth-margin)/Double.valueOf(format);
		//    double[][]  sortedResponse [response] [well] [type_id] [sample_id]
		
		for (int i = 0; i < sortedResponse.length; i++) {
	     

		    //set // commentlor based on well type
		    switch((int)Math.round(sortedResponse[i][2])){
		    case 1: g.setColor(Color.black);
			break;
		    case 2: g.setColor(Color.green);
			break;
		    case 3: g.setColor(Color.red);
			//LOGGER.info("color set to red");
			break;
		    case 4: g.setColor(Color.gray);
			break;
		    case 5: g.setColor(Color.blue);
			break;
		    }

	      
		    int xpt = (int)Math.round(originX + scaleX*sortedResponse[i][1]);
		    int ypt = (int)Math.round(originY - scaleY*sortedResponse[i][0]);
	      // LOGGER.info("scaleX: " + scaleX + " response: " + sortedResponse[i][0] + " xpt: " + xpt);

	      g.drawString("X", xpt, ypt);

	      g.setColor(Color.black);
	      g.drawLine(margin, 0,  margin, hgt-margin); // y-axis
	      g.drawLine(margin, hgt-margin, wth-10, hgt-margin); // x-axis

	      if(selected_response == p_enhanced_response ){
		  g.drawString("Well", (int)Math.round(originX + (wth-margin)/2)  , (int)Math.round(originY*2 + margin/2 + 10) );
	      }else{
		  g.drawString("Well", (int)Math.round(originX + (wth-margin)/2)  , (int)Math.round(originY + margin/2 + 10) );
	      }
	     
	      
	      //draw the axes ticks and labels
	      
	      switch(format){
	      case 96:		  
		  for( int j = 10; j <100; j=j+10 ){  //X- axis
		      g.drawLine( (int)Math.round(originX +scaleX*j), hgt-margin,   (int)Math.round(originX +scaleX*j), hgt-margin+10);
		      g.drawString(String.valueOf(j),  (int)Math.round(originX +scaleX*j - 10), hgt-margin+25 );
		  }		  
		  break;
	      case 384:
		  for( int j = 50; j <=400; j=j+50 ){  //X- axis
		      g.drawLine( (int)Math.round(originX +scaleX*j), hgt-margin,   (int)Math.round(originX +scaleX*j), hgt-margin+10);
		      g.drawString(String.valueOf(j),  (int)Math.round(originX +scaleX*j - 10), hgt-margin+25 );
		  }		  
		  
		  break;    
	      case 1536:
		  for( int j = 155; j <1550; j=j+155 ){  //X- axis
		      g.drawLine( (int)Math.round(originX +scaleX*j), hgt-margin,   (int)Math.round(originX +scaleX*j), hgt-margin+10);
		      g.drawString(String.valueOf(j),  (int)Math.round(originX +scaleX*j - 10), hgt-margin+25 );
		  }		  

		  
		  break;	    
	      }

	      //scale Y is 280 i.e. Y axis broke into 280 units, 140 on each side of 0
	      //so height - margin is 280
	      // consider originY-140 is the origin and add to that 40, 90 140, 190, 240
	      // or originY - 150 is where you want to start
	      if(selected_response == p_enhanced_response ){
		  //Y axis at -100, -50, 0, 50, 100
		  String[] labels = {"150","100","50","0","-50","-100","-150"};
		  for(int k = 0; k <7; k++){ //Y axis
		      g.drawLine( (int)Math.round(originX-10), (int)Math.round(originY-150 + k*50),
				  (int)Math.round(originX), (int)Math.round(originY-150 + k*50));
		      g.drawString( labels[k],  (int)Math.round(originX - 50),
				    (int)Math.round(originY-150 + k*50 ));	   	
		  }
		  
		      
	      }else{
		  //Yaxis  6 evenly spaced ticks
		  for(int k = 1; k <6; k++){ //Y axis
		      g.drawLine( (int)Math.round(originX-10), (int)Math.round(originY-k*((hgt-margin)/6)),
				  (int)Math.round(originX), (int)Math.round(originY-k*((hgt-margin)/6)));
		      g.drawString(String.valueOf(df.format((k*((hgt-margin)/6))/scaleY)),  (int)Math.round(originX - 50),
				   (int)Math.round(originY-k*((hgt-margin)/6)) );	   	
		  }
	      }
	      
	      //draw "Response" on the Y axis
	      Graphics2D g2d = (Graphics2D) g.create();
	      AffineTransform affineTransform = new AffineTransform();
	      affineTransform.rotate(Math.toRadians(-90), 0, 0);
	      Font rotatedFont = font.deriveFont(affineTransform);
	      g2d.setFont(rotatedFont);
	      if(selected_response == p_enhanced_response ){
		  g2d.drawString("% Enhancement", Math.round(originX -25)  , Math.round(originY*2 - hgt/2 + 60) );
	      }else{
		  g2d.drawString("Response", Math.round(originX -25)  , Math.round(originY - hgt/2 + 40) );
	      }
	      
	      g2d.setFont(font);
	      g2d.dispose();

 		Graphics2D g2db = (Graphics2D) g.create();
		//set the stroke of the copy, not the original 
		Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
		g2db.setStroke(dashed);
		g2db.drawLine(margin, (int)Math.round(originY - scaleY*threshold) , wth-10, (int)Math.round(originY - scaleY*threshold));
	      //label the threshold if it is + 2 or 3 SD
		if( threshold == mean_neg_3_sd){
		g2db.setColor(Color.blue);
		g2db.drawString( "mean(neg) + 3SD", margin+20  , (int)Math.round(originY - scaleY*threshold - 10)  );
		}
		if( threshold == mean_neg_2_sd){
		g2db.setColor(Color.blue);
		g2db.drawString( "mean(neg) + 2SD", margin+20  , (int)Math.round(originY - scaleY*threshold - 10)  );
		}
		if( threshold == mean_pos){
		g2db.setColor(Color.blue);
		g2db.drawString( "mean(pos)", margin+20  , (int)Math.round(originY - scaleY*threshold - 10)  );
		}
		//gets rid of the copy
		g2db.dispose();
	      
	  }

	  
      }


	};  //removed semicolon here
  
    getContentPane().add(slider, BorderLayout.EAST);
     getContentPane().add(panel, BorderLayout.CENTER);
     
    setVisible(true);
   
 
  }
    
    public void setThreshold(double _threshold){
	this.threshold = _threshold;
        thresholdField.setText(df.format(threshold));
	slider.setDoubleValue(threshold);
	num_hits = selected_response.getHitsAboveThreshold(threshold);
	numHitsField.setText(String.valueOf(num_hits));
	repaint();
	
    }

    /*
    public void updateAllVariables(){
	
	format = selected_response.getFormat();
	num_plates = selected_response.getNum_plates();
	max_response = selected_response.getMax_response();
	min_response = selected_response.getMin_response();
	mean_bkgrnd = selected_response.getMean_bkgrnd();
	stdev_bkgrnd = selected_response.getStdev_bkgrnd();
	mean_neg_3_sd = selected_response.getMean_neg_3_sd();
	mean_neg_2_sd = selected_response.getMean_neg_2_sd();
	mean_pos = selected_response.getMean_pos();
	sortedResponse = selected_response.getSortedResponse();
	sorted_response_unknowns_only = selected_response.getSortedResponseUnknownsOnly();
	
	num_hits = selected_response.getHitsAboveThreshold(threshold);
	//LOGGER.info("max " + max_response);
	//LOGGER.info("mean_bkgrnd " + mean_bkgrnd);
	//LOGGER.info("mean_neg_3_sd " + mean_neg_3_sd);
	//LOGGER.info("num_hits " + num_hits);
	
	//	LOGGER.info("num_hits: " + Integer.toString(num_hits));
	//	    slider.setDoubleMinimum(min_response);
	//    slider.setDoubleMaximum(max_response);
	//    slider.setDoubleValue(threshold);

	//numHitsLabel.setText(Integer.toString(num_hits));
	repaint();
    }
    */

}
