package lnrocks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlOptions;


public class POIUtilities {

 
  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private File file;
  private static final String newline = "\n";
    private DialogMainFrame dmf;

  public POIUtilities(DialogMainFrame _dmf) {
      dmf = _dmf;    
  }
  /**
   * Convert any table to a spreadsheet. This first row is a header. Use the first column in
   * remaining rows as boolean to determine if row should be printed (true) or not (false)
   */
  public void writeJTableToSpreadsheet(String sheetName, Object[][] _tableData) {

    XSSFWorkbook workbook = new XSSFWorkbook();
    // Create a blank sheet
    XSSFSheet spreadsheet = workbook.createSheet(sheetName);
    Object[][] tableData = _tableData;
    // Create row object
    XSSFRow row;

    int rowsize = tableData.length;
    int rowcounter = 0;
    // assume all rows have the same number of columns
    int colsize = tableData[0].length;

    for (int rowid = 0; rowid < rowsize; rowid++) {

      if (rowid == 0) { // the headers
        row = spreadsheet.createRow(rowcounter);
        for (int colid = 0; colid < colsize; colid++) {
          XSSFCell cell = row.createCell(colid);
          cell.setCellValue((String) tableData[rowid][colid]);
        }
        rowcounter++;
      } else {

        row = spreadsheet.createRow(rowcounter);

        for (int colid = 0; colid < colsize; colid++) {
          XSSFCell cell = row.createCell(colid);
          cell.setCellValue((String) tableData[rowid][colid]);
        }
        rowcounter++;
      }
    }

    // Write the workbook in file system
    try {
      FileOutputStream out = new FileOutputStream(new File("Writesheet.xlsx"));
      workbook.write(out);
      out.close();
    } catch (IOException ex) {
      LOGGER.severe("Error: " + ex);
    }
    System.out.println("Writesheet.xlsx written successfully");
  }
}
