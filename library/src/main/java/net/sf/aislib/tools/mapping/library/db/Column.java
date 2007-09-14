package net.sf.aislib.tools.mapping.library.db;

import java.sql.DatabaseMetaData;
import java.sql.Types;

import org.jdom.CDATA;
import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 * @author Milosz Tylenda, AIS.PL
 */
public class Column {

   private Integer cnColumnSize;
   private Integer cnDecimalDigits;
   private int     cnNullable;
   private int     cnNumPrecRadix;
   private int     cnOrdinalPosition;
   private short   cnDataType;
   private String  cnCatalog;
   private String  cnClassName;
   private String  cnColumnDef;
   private String  cnName;
   private String  cnRemarks;
   private String  cnSchema;
   private String  cnTableName;
   private String  cnTypeName;

  /**
   *
   */
  protected Column(String tName) {
    cnName = tName;
  }

  /**
   *
   */
  public String getName() {
    return ((cnName != null) ? new String (cnName) : cnName);
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    cnCatalog = tCatalog;
  }

  /**
   *
   */
  public void setClassName(String tClassName) {
    cnClassName = tClassName;
  }

  /**
   *
   */
  public void setColumnDef(String tColumnDef) {
    cnColumnDef = tColumnDef;
  }

  /**
   *
   */
  public void setColumnSize(int tColumnSize) {
    cnColumnSize = new Integer(tColumnSize);
  }

  /**
   *
   */
  public void setDataType(short tDataType) {
    cnDataType = tDataType;
  }

  /**
   *
   */
  public void setDecimalDigits(int tDecimalDigits) {
    cnDecimalDigits = new Integer(tDecimalDigits);
  }

  /**
   *
   */
  public void setNullable(int tNullable) {
    cnNullable = tNullable;
  }

  /**
   *
   */
  public void setNumPrecRadix(int tNumPrecRadix) {
    cnNumPrecRadix = tNumPrecRadix;
  }

  /**
   *
   */
  public void setOrdinalPosition(int tOrdinalPosition) {
    cnOrdinalPosition = tOrdinalPosition;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    cnSchema = tSchema;
  }

  /**
   *
   */
  public void setTableName(String tTableName) {
    cnTableName = tTableName;
  }

  /**
   *
   */
  public void setTypeName(String tTypeName) {
    cnTypeName = tTypeName;
  }

  /**
   *
   */
  private String firstCharToUpper(String source) {
    if (source.length() == 0) {
      return source;
    }
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
   * FIXME
   */
  public String toString() {
    return new String ("Column:\n" + cnName + "(" + cnCatalog + ", " + cnSchema
                      + ", " + cnTypeName + ")\n");
  }

  /**
   * FIXME
   */
  public Element toXML () {

    Element result = new Element ("column");
    result.setAttribute ("name", cnName);
    result.setAttribute ("java-name", canonizeFieldName (cnName.toLowerCase ()));
    result.setAttribute ("order", "" + cnOrdinalPosition);
    result.setAttribute ("db-type", cnTypeName);
    if (cnColumnSize != null) {
      result.setAttribute("column-size", "" + cnColumnSize.intValue());
    }
    if (cnDecimalDigits != null) {
      result.setAttribute("decimal-digits", "" + cnDecimalDigits.intValue());
    }

    if (cnRemarks != null) {
      Element remarks = new Element ("remarks");
      remarks.addContent (new CDATA (cnRemarks));
      result.addContent (remarks);
    }

    if (cnColumnDef != null) {
      result.setAttribute ("default-value", cnColumnDef);
    }

    String notNull = null;
    if (cnNullable == DatabaseMetaData.columnNoNulls)  { notNull = "true"; }
    if (cnNullable == DatabaseMetaData.columnNullable) { notNull = "false"; }
    if (notNull != null) {
      result.setAttribute ("not-null", notNull);
    }

    if (cnClassName != null) {
      String shortName = cnClassName.substring (cnClassName.lastIndexOf ('.') + 1);
      result.setAttribute ("class-name", cnClassName);
      result.setAttribute ("class-shortcut", shortName);
    }

    String sqlType = cnTypeName;
    switch (cnDataType) {
      case Types.CHAR:
      case Types.DATE:
      case Types.LONGVARCHAR:
      case Types.TIMESTAMP:
      case Types.VARCHAR:
        if (cnColumnSize != null) {
          sqlType = sqlType.concat ("(" + cnColumnSize.intValue() + ")");
        }
        break;

      case Types.BIGINT:
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.INTEGER:
      case Types.NUMERIC:
        if ((cnColumnSize != null) && (cnDecimalDigits != null)) {
          sqlType = sqlType.concat ("(" + cnColumnSize + ", " + cnDecimalDigits + ")");
        }
        break;
    }
    result.setAttribute ("sql-type", sqlType);
    return result;
  }
}
