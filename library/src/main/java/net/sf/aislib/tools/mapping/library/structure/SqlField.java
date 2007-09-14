package net.sf.aislib.tools.mapping.library.structure;

/**
 * Holds data related to a column in table in database.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class SqlField {

  private String name;
  private String type;
  private String defaultt;
  private boolean useOnInsert;

  private Read read;
  private Write write;

  public boolean hasWrite() {
    return (write != null);
  }

  public boolean hasRead() {
    return (read != null);
  }

  public Write getWrite() {
    return write;
  }


  public void setWrite(Write write) {
    this.write = write;
  }

  public SqlField(String name, String type, String defaultt, String onInsert) {
    this.name = name;
    this.type = type;
    this.defaultt = defaultt;
    this.useOnInsert = onInsert.equals("use");
  }

  public Read getRead() {
    return read;
  }
  public void setRead(Read read) {
    this.read = read;
  }
  public String getName() {
    return name;
  }
  public String getType() {
    return type;
  }
  public String getDefault() {
    return defaultt;
  }
  public boolean useOnInsert() {
    return useOnInsert;
  }

  public boolean isClobType() {
    return "CLOB".equalsIgnoreCase(type);
  }

}
