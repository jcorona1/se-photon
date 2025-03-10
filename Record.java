import java.io.IOException;

public class Record {

  private boolean empty;

  public String Id;
  public String Codename;

  public Record() {
    empty = true;
  }

  /**
   * Update the fields of a record from an array of fields
   * 
   * @param fields array with values of fields
   * @return nothing
   * @throws IOException
   */
  public void updateFields(String[] fields) throws IOException {
    if (fields.length == 2) {
      this.Id = fields[0];
      this.Codename = fields[1];

      empty = false;
    } else {
      throw new IOException();
    }
  }

  /**
   * Check if record fields have been updated
   * 
   * @return true if record is empty otherwise false
   */
  public boolean isEmpty() {
    return empty;
  }

  public String toString() {
    return "Id: " + this.Id +
        ", Codename: " + this.Codename;
  }

}
