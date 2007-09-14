package net.sf.aislib.tools.mapping.library.structure;

import java.util.List;

import net.sf.aislib.tools.mapping.library.generators.Utils;

public class SqlQuery {

  private boolean distinct;
  private String columns;
  private String from;
  private String where;
  private String groupBy;
  private String having;
  private String orderBy;
  private String other;
  private String set;
  private String body;

  public SqlQuery(String where, String orderBy, boolean distinct, String set,
      String columns, String from, String body, String groupBy, String having,
      String other) {
    this.where    = where;
    this.orderBy  = orderBy;
    this.distinct = distinct;
    this.set      = set;
    this.columns  = columns;
    this.from     = from;
    this.body     = body;
    this.groupBy  = groupBy;
    this.having   = having;
    this.other    = other;
  }

  public String getWhere() {
    return where;
  }
  public String getOrderBy() {
    return orderBy;
  }
  public String getSet() {
    return set;
  }
  public String getColumns() {
    return columns;
  }
  public String getFrom() {
    return from;
  }
  public String getBody() {
    return body;
  }
  private String getGroupBy() {
    return groupBy;
  }
  private String getHaving() {
    return having;
  }
  private String getOther() {
    return other;
  }
  public void setColumns(String object) {
    columns = object;
  }
  public boolean isDistinct() {
    return distinct;
  }
  public boolean hasWhere() {
    return (where != null);
  }
  public boolean hasOrderBy() {
    return (orderBy != null);
  }
  public boolean hasSet() {
    return (set != null);
  }
  public boolean hasColumns() {
    return (columns != null);
  }
  public boolean hasFrom() {
    return (from != null);
  }
  public boolean hasBody() {
    return (body != null);
  }
  private boolean hasGroupBy() {
    return (groupBy != null);
  }
  private boolean hasHaving() {
    return (having != null);
  }
  private boolean hasOther() {
    return (other != null);
  }

  public String createDistinctClause() {
    if (isDistinct()) {
      return " DISTINCT ";
    }
    return "";
  }

  public String createColumnsClause(List fieldList, String tableName) {
    if (hasColumns()) {
      return " " + getColumns() + " ";
    }
    return Utils.formatSqlList2(fieldList, hasFrom(), tableName, true);
  }

  public String createWhereClause() {
    if (hasWhere()) {
      return " WHERE " + getWhere() + " ";
    }
    return "";
  }

  public String createOrderByClause() {
    if (hasOrderBy()) {
      return " ORDER BY " + getOrderBy() + " ";
    }
    return "";
  }

  public String createFromClause(String tableName) {
    if (hasFrom()) {
      return " FROM " + getFrom() + " ";
    }
    return " FROM " + tableName + " ";
  }

  public String createGroupByAndHavingClause() {
    StringBuffer result = new StringBuffer();
    if (hasGroupBy()) {
      result.append(" GROUP BY " + getGroupBy() + " ");
    }
    if (hasHaving()) {
      result.append(" HAVING " + getHaving() + " ");
    }
    return result.toString();
  }

  public String createOtherClause() {
    if (hasOther()) {
      return " " + getOther() + " ";
    }
    return "";
  }

  /**
   * Returns true if any part of this query contains a raw parameter.
   * @return true if any part of this query contains a raw parameter
   */
  public boolean containsRawParam() {
    return
      (
        Utils.containsRawParam(getWhere()) ||
        Utils.containsRawParam(getOrderBy()) ||
        Utils.containsRawParam(getSet()) ||
        Utils.containsRawParam(getColumns()) ||
        Utils.containsRawParam(getFrom()) ||
        Utils.containsRawParam(getBody()) ||
        Utils.containsRawParam(getGroupBy()) ||
        Utils.containsRawParam(getHaving()) ||
        Utils.containsRawParam(getOther())
      );
  }

  /**
   * Assures this object has valid attributes in an 'aggregate' context.
   */
  public void checkSyntaxInAggregateContext() {
    boolean valid = (!hasBody() && hasColumns() && !hasSet());
    if (!valid) {
      throw new IllegalArgumentException("<sql-query> in <aggregate> context: 'columns' is required, " +
          "'body' and 'set' are forbidden");
    }
  }

  /**
   * Assures this object has valid attributes in a 'call' context.
   */
  public void checkSyntaxInCallContext() {
    boolean valid = (!isDistinct() && !hasWhere() && !hasOrderBy() && !hasSet() && !hasColumns() &&
      !hasFrom() && hasBody() && !hasGroupBy() && !hasHaving() && !hasOther());
    if (!valid) {
      throw new IllegalArgumentException("<sql-query> in <call> context: only 'body' is allowed and required.");
    }

    if (containsRawParam()) {
      throw new IllegalArgumentException("<sql-query> in <call> context: '??' markers are not supported.");
    }
  }

  /**
   * Assures this object has valid attributes in an 'update' context.
   */
  public void checkSyntaxInUpdateContext() {
    boolean valid = (!isDistinct() && !hasBody() && !hasColumns() && !hasFrom() && !hasOrderBy() &&
      !hasGroupBy() && !hasHaving() && hasSet());
    if (!valid) {
      throw new IllegalArgumentException("<sql-query> in <update> context: 'set' is required, " +
          "'where' and 'other' are optional, other attributes are forbidden.");
    }
  }


}
