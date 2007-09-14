package net.sf.aislib.tools.mapping.library.db;

import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 */
public class PrimaryKey {

  private Short  pkSequenceNumber;
  private String pkCatalog;
  private String pkName;
  private String pkColumnName;
  private String pkSchema;
  private String pkTableName;

  /**
   *
   */
  public PrimaryKey(String tColumnName) {
    pkColumnName = tColumnName;
  }

  /**
   *
   */
  public String getColumnName() {
    return ((pkColumnName != null) ? new String (pkColumnName) : pkColumnName);
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    pkCatalog = tCatalog;
  }

  /**
   *
   */
  public void setName(String tName) {
    pkName = tName;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    pkSchema = tSchema;
  }

  /**
   *
   */
  public void setSequenceNumber(short tSequenceNumber) {
    pkSequenceNumber = new Short(tSequenceNumber);
  }

  /**
   *
   */
  public void setTableName(String tTableName) {
    pkTableName = tTableName;
  }

  /**
   * FIXME
   */
  public String toString() {
    return new String ("");
  }

  /**
   * FIXME
   */
  public Element toXML() {

    Element result = new Element ("primary-key");
    result.setAttribute ("column-name", pkColumnName);
    if (pkName != null) {
      result.setAttribute ("name", pkName);
    }
    return result;
  }

} // class
