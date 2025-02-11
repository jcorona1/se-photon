import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class DB {
  //public static final int NUM_RECORDS = 10;
  public static final int RECORD_SIZE = 35;

  private RandomAccessFile Dinout;
  private int num_records;
  private String Id;
  private String Codename;

  public DB() {
    this.Dinout = null;
    this.num_records = 0;
    this.Id = "ID";
    this.Codename = "Codename";
  }

  /**
   * Opens the file in read/write mode
   * 
   * @param filename (e.g., input.data)
   * @return status true if operation successful
   */
  public void  openDB(String filename) {
        // Open file in read/write mode
    try {
      this.Dinout = new RandomAccessFile(filename, "rw");
      // Set the number of records
      String line;
      try {
        line = Dinout.readLine();
      } catch (IOException e) {
        System.out.println("Error, file may be empty\n");
        line = null;
        e.printStackTrace();
      }
      
      while (line != null) {     
        this.num_records++;
        try {
          line = Dinout.readLine();
        } catch (IOException e) {
          System.out.println("Error, file may have reached end\n");
          line = null;
          e.printStackTrace();
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Could not open file\n");
      e.printStackTrace();
    }
  }
  
  /** 
   * Writes the data to the location specified by file parameter
   *  
   */
  public void writeRecord(RandomAccessFile file, String Id, String Codename ) {
    	//format input values to be put in record
        this.Id = String.format("%-3s", Id.length() > 3 ? Id.substring(0, 2) : Id);
        this.Codename = String.format("%-30s", Codename.length() > 29 ? Codename.substring(0, 29) : Codename);
	try {
		file.writeBytes(this.Id + "," + this.Codename + "\n");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
  }
  
  /**Takes in record number of record to be changed as well as what values to overwrite with 
   * 
   *  
   */ 
  public void overwriteRecord(int record_num, String Id, String Codename ) {
    if ((record_num >= 0) && (record_num < this.num_records)) {
      try {
        Dinout.seek(0); // return to the top of the file
        Dinout.skipBytes(record_num * RECORD_SIZE);
	      //overwrite the specified record
        writeRecord (Dinout, Id, Codename);
      } catch (IOException e) {
        System.out.println("There was an error while attempting to overwrite a record from the database file.\n");
        e.printStackTrace();
      }
    }
	System.out.println("Record Successfully Overwritten");
  }

  /**
   * Close the database file
   */
  public void close() {
    try {
      Dinout.close();
    } catch (IOException e) {
      System.out.println("There was an error while attempting to close the database file.\n");
      e.printStackTrace();
    }
  }

  /**
   * Get record number n (Records numbered from 0 to NUM_RECORDS-1)
   * 
   * @param record_num
   * @return values of the fields with the name of the field and
   *         the values read from the record
   */
  public Record readRecord(int record_num) {
    Record record = new Record();
    String[] fields;
    if ((record_num >= 0) && (record_num < this.num_records)) {
      try {
        Dinout.seek(0); // return to the top of the file
        Dinout.skipBytes(record_num * RECORD_SIZE);
        // parse record and update fields
        fields = Dinout.readLine().split(",", 0);
        record.updateFields(fields);
      } catch (IOException e) {
        System.out.println("There was an error while attempting to read a record from the database file.\n");
        e.printStackTrace();
      }
    }
    return record;
  }

  /**
   * Search by record id
   * 
   * @param Id
   * @return Record number (which can then be used by read to
   *         get the fields) or -1 if id not found
   */
  public int search(String Id) {
    int Index = 0;
    int High = num_records - 1;
    boolean Found = false;
    Record record;

    while (!Found && (High >= Index)) {
      record = readRecord(Index);
      String CheckId = record.Id.trim();

      // int result = MiddleId[0].compareTo(id); // DOES STRING COMPARE
      int result = Integer.parseInt(CheckId) - Integer.parseInt(Id); 
      if (result == 0)
        Found = true;
      else 
        Index++;
    }
    if (Found) {
      return Index; // the record number of the record
    } else
      return -1;
  }

  public void addRecord(String Id, String Codename){
    try {
      Dinout.seek(0); // return to the top of the file
      Dinout.skipBytes(num_records * RECORD_SIZE);
      writeRecord(Dinout, Id, Codename);
      num_records++;
    } catch (IOException e) {
      System.out.println("There was an error while attempting to write a record to the database file.\n");
      e.printStackTrace();
    }
  }
}
