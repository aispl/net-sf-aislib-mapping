package net.sf.aislib.tools.mapping.library.db;

import java.sql.DatabaseMetaData;

import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 */
public class ForeignKey {

  private Short  fkDeleteRule;
  private Short  fkUpdateRule;
  private String fkCatalog;
  private String fkDestinationColumnName;
  private String fkDestinationTableName;
  private String fkDestinationName;
  private String fkSchema;
  private String fkSourceName;
  private String fkSourceColumnName;
  private String fkSourceTableName;

  /**
   *
   */
  protected ForeignKey(String tSourceColumnName) {
    fkSourceColumnName = tSourceColumnName;
  }

  /**
   *
   */
  public String getSourceColumnName() {
    return ((fkSourceColumnName != null) ? new String (fkSourceColumnName) : fkSourceColumnName);
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    fkCatalog = tCatalog;
  }

  /**
   *
   */
  public void setDeleteRule(short tDeleteRule) {
    fkDeleteRule = new Short(tDeleteRule);
  }

  /**
   *
   */
  public void setDestinationColumnName(String tDestinationColumnName) {
    fkDestinationColumnName = tDestinationColumnName;
  }

  /**
   *
   */
  public void setDestinationName(String tDestinationName) {
    fkDestinationName = tDestinationName;
  }

  /**
   *
   */
  public void setDestinationTableName(String tDestinationTableName) {
    fkDestinationTableName = tDestinationTableName;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    fkSchema = tSchema;
  }

  /**
   *
   */
  public void setSourceName(String tSourceName) {
    fkSourceName = tSourceName;
  }

  /**
   *
   */
  public void setSourceTableName(String tSourceTableName) {
    fkSourceTableName = tSourceTableName;
  }

  /**
   *
   */
  public void setUpdateRule(short tUpdateRule) {
    fkUpdateRule = new Short(tUpdateRule);
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
  private String describeRule(short rule) {
    String result = null;
    switch (rule) {
      case DatabaseMetaData.importedKeyNoAction:
        result = "No action";
        break;
      case DatabaseMetaData.importedKeyCascade:
        result = "No action";
        break;
    }
    return result;
  }

  /**
   * FIXME
   */
  public Element toXML() {
    Element result = new Element ("foreign-key");

    result.setAttribute ("src-column-name",  fkSourceColumnName);
    result.setAttribute ("src-table-name",   fkSourceTableName);
    result.setAttribute ("dest-column-name", fkDestinationColumnName);
    result.setAttribute ("dest-table-name",  fkDestinationTableName);

    if (fkSourceName != null) {
      result.setAttribute ("src-name", fkSourceName);
    }

    if (fkDestinationName != null) {
      result.setAttribute ("dest-name", fkDestinationName);
    }

    String temp = null;
    if (fkUpdateRule != null) {
      temp = describeRule (fkUpdateRule.shortValue());
      if (temp != null) {
        result.setAttribute ("update-rule", temp);
      }
    }

    if (fkDeleteRule != null) {
      temp = describeRule (fkDeleteRule.shortValue());
      if (temp != null) {
        result.setAttribute ("delete-rule", temp);
      }
    }

    return result;
  }

} // class
