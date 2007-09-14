package net.sf.aislib.tools.mapping.library.db;

import java.util.HashMap;
import java.util.Iterator;

import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 */
public class Table {

  private String tbCatalog;
  private String tbName;
  private String tbRemarks;
  private String tbSchema;
  private String tbType;

  private HashMap columns;
  private HashMap primaryKeys;
  private HashMap foreignKeys;
  private HashMap indexes;

  /**
   *
   */
  public Table(String tName) {
    columns     = new HashMap ();
    primaryKeys = new HashMap ();
    foreignKeys = new HashMap ();
    indexes     = new HashMap ();
    tbName      = tName;
  }

  /**
   *
   */
  public void addColumn(Column column) {
    columns.put (column.getName (), column);
  }

  /**
   *
   */
  public Column getColumn(String columnName) {
    return (Column) columns.get (columnName);
  }

  /**
   *
   */
  public void addPrimaryKey(PrimaryKey primaryKey) {
    primaryKeys.put (primaryKey.getColumnName (), primaryKey);
  }

  /**
   *
   */
  public void addForeignKey(ForeignKey foreignKey) {
    foreignKeys.put (foreignKey.getSourceColumnName (), foreignKey);
  }

  /**
   *
   */
  public void addIndex(Index index) {
    indexes.put ("" + index.getOrdinalPosition (), index);
  }

  /**
   *
   */
  public String getName() {
    return ((tbName != null) ? new String (tbName) : tbName);
  }

  /**
   *
   */
  public String getType() {
    return ((tbType != null) ? new String (tbType) : tbType);
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    tbCatalog = tCatalog;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    tbSchema = tSchema;
  }

  /**
   *
   */
  public void setType(String tType) {
    tbType = tType;
  }

  /**
   *
   */
  private String firstCharToUpper(String source) {
    return source.substring (0, 1).toUpperCase ().concat (source.substring (1));
  }

  /**
   *
   */
  private String dropUnderscores(String name) {

    String       source = new String (name);
    StringBuffer result = new StringBuffer ();

    int          idx   = 0;
    int          vSize = source.length ();

    while (idx < vSize) {
      int spaceIdx = source.indexOf ('_', idx);
      if (spaceIdx >= 0) { result.append (firstCharToUpper (source.substring (idx, spaceIdx))); }
      else {
        result.append (firstCharToUpper (source.substring (idx)));
        spaceIdx = vSize;
      }
      idx = spaceIdx + 1;
    }
    return new String (result);
  }

  /**
   *
   */
  private String canonizeFieldName(String name) {
    String result = dropUnderscores (name);
    return result.substring(0,1).toLowerCase().concat (result.substring(1));
  }

  /**
   *
   */
  private String canonizeClassName(String name) { return dropUnderscores (name); }

  /**
   * FIXME
   */
  public String toString() {
    return new String ("Table: " + tbName + "(" + tbCatalog + ", " + tbSchema
                      + ", " + tbType + ")\n" + columns);
  }

  /**
   *
   */
  public Element toXML() {
    Element result = new Element ("table");
    result.setAttribute ("name", tbName);
    result.setAttribute ("java-name", canonizeClassName (tbName.toLowerCase ()));
    if (tbCatalog != null) { result.setAttribute ("catalog", tbCatalog); }
    if (tbSchema != null)  { result.setAttribute ("schema", tbSchema); }
    if (tbType != null)    { result.setAttribute ("type", tbType); }

    if (!columns.isEmpty ()) {
      for (Iterator it = columns.keySet ().iterator ();  it.hasNext (); ) {
        String columnName = (String) it.next ();
        Column column = (Column) columns.get (columnName);
        result.addContent (column.toXML ());
      }
    }

    if (!indexes.isEmpty ()) {
      for (Iterator it = indexes.keySet ().iterator ();  it.hasNext (); ) {
        String idxName = (String) it.next ();
        Index index = (Index) indexes.get (idxName);
        result.addContent (index.toXML ());
      }
    }

    if (!primaryKeys.isEmpty ()) {
      for (Iterator it = primaryKeys.keySet ().iterator ();  it.hasNext (); ) {
        String keyName = (String) it.next ();
        PrimaryKey pKey = (PrimaryKey) primaryKeys.get (keyName);
        result.addContent (pKey.toXML ());
      }
    }

    if (!foreignKeys.isEmpty ()) {
      for (Iterator it = foreignKeys.keySet ().iterator ();  it.hasNext (); ) {
        String keyName = (String) it.next ();
        ForeignKey fKey = (ForeignKey) foreignKeys.get (keyName);
        result.addContent (fKey.toXML ());
      }
    }

    return result;
  }

} // class
