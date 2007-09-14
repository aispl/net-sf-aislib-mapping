package net.sf.aislib.tools.mapping.library.db;

import java.math.BigDecimal;

import org.jdom.Element;

/**
 * @author Daniel Rychcik, AIS.PL
 */
public class Sequence {

  public static final int ASCENDING  = 1;
  public static final int DESCENDING = 2;

  private String      sName;
  private String      sSchema;
  private Integer     sOrder;
  private BigDecimal  sMinValue;
  private BigDecimal  sMaxValue;
  private BigDecimal  sIncrement;
  private BigDecimal  sInitialValue;
  private Boolean     sCycleFlag;
  private Boolean     sOrderFlag;
  private BigDecimal  sCacheSize;

  /**
   *
   */
  protected Sequence() {
  }


  /**
   *
   */
  public String getName() {
    return sName;
  }

  /**
   *
   */
  public void setName(String name) {
    sName = name;
  }

  /**
   *
   */
  public String getSchema() {
    return sSchema;
  }

  /**
   *
   */
  public void setSchema(String schema) {
    sSchema = schema;
  }

  /**
   *
   */
  public int getOrder() {
    return sOrder.intValue();
  }

  /**
   *
   */
  public void setOrder(int order) {
    sOrder = new Integer(order);
  }

  /**
   *
   */
  public BigDecimal getMinValue() {
    return sMinValue;
  }

  /**
   *
   */
  public void setMinValue(BigDecimal minValue) {
    sMinValue = minValue;
  }

  /**
   *
   */
  public BigDecimal getMaxValue() {
    return sMaxValue;
  }

  /**
   *
   */
  public void setMaxValue(BigDecimal maxValue) {
    sMaxValue = maxValue;
  }

  /**
   *
   */
  public BigDecimal getIncrement() {
    return sIncrement;
  }

  /**
   *
   */
  public void setIncrement(BigDecimal increment) {
    sIncrement = increment;
  }

  /**
   *
   */
  public BigDecimal getInitialValue() {
    return sInitialValue;
  }

  /**
   *
   */
  public void setInitialValue(BigDecimal initialValue) {
    sInitialValue = initialValue;
  }

  /**
   *
   */
  public Boolean getCycleFlag() {
    return sCycleFlag;
  }

  /**
   *
   */
  public void setCycleFlag(Boolean cycleFlag) {
    sCycleFlag = cycleFlag;
  }

  /**
   *
   */
  public Boolean getOrderFlag() {
    return sOrderFlag;
  }

  /**
   *
   */
  public void setOrderFlag(Boolean orderFlag) {
    sOrderFlag = orderFlag;
  }

  /**
   *
   */
  public BigDecimal getCacheSize() {
    return sCacheSize;
  }

  /**
   *
   */
  public void setCacheSize(BigDecimal cacheSize) {
    sCacheSize = cacheSize;
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
        result.append (firstCharToUpper(source.substring(idx)));
        spaceIdx = vSize;
      }
      idx = spaceIdx + 1;
    }
    return new String (result);
  }

  /**
   *
   */
  private String canonizeSequenceName (String name) {
    return dropUnderscores (name.toLowerCase());
  }


  /**
   * FIXME
   */
  public String toString () {
    return new String ("");
  }

  /**
   * FIXME
   */
  public Element toXML () {
    Element result = null;
    result = new Element ("sequence");
    result.setAttribute("type","SEQUENCE");
    if (sName != null ) {
      result.setAttribute ("name", sName);
      result.setAttribute ("java-name", canonizeSequenceName(sName));
    }
    if (sSchema != null) { result.setAttribute ("schema", sSchema); }
    if (sOrder != null) { result.setAttribute ("order", sOrder.toString()); }
    if (sMinValue != null) { result.setAttribute ("min-value", sMinValue.toString()); }
    if (sMaxValue != null) { result.setAttribute ("max-value", sMaxValue.toString()); }
    if (sIncrement != null) { result.setAttribute ("increment", sIncrement.toString()); }
    if (sInitialValue != null) { result.setAttribute ("initial-value", sInitialValue.toString()); }
    if (sCycleFlag != null) { result.setAttribute ("cycle-flag", sCycleFlag.toString()); }
    if (sOrderFlag != null) { result.setAttribute ("order-flag", sOrderFlag.toString()); }
    if (sCacheSize != null) { result.setAttribute ("cache-size", sCacheSize.toString()); }
    return result;
  }

} // class
