package net.sf.aislib.tools.mapping.library.db;

import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 */
public class Index {

  private short  iOrdinalPosition;
  private String iCatalog;
  private String iColumnName;
  private String iName;
  private String iSchema;
  private String iTableName;

  /**
   *
   */
  protected Index(short tOrdinalPosition) {
    iOrdinalPosition = tOrdinalPosition;
  }

  /**
   *
   */
  public short getOrdinalPosition() {
    return iOrdinalPosition;
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    iCatalog = tCatalog;
  }

  /**
   *
   */
  public void setColumnName(String tColumnName) {
    iColumnName = tColumnName;
  }

  /**
   *
   */
  public void setName(String tName) {
    iName = tName;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    iSchema = tSchema;
  }

  /**
   *
   */
  public void setTableName(String tTableName) {
    iTableName = tTableName;
  }

  /**
   * FIXME
   */
  public String toString() {
    return new String("");
  }

  /**
   * FIXME
   */
  public Element toXML() {

    Element result = null;
    result = new Element ("index");
    if (iColumnName != null) { result.setAttribute ("column-name", iColumnName); }
    if (iName != null) { result.setAttribute ("name", iName); }
    return result;
  }

}
