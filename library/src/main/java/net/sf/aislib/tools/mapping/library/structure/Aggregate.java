package net.sf.aislib.tools.mapping.library.structure;

public class Aggregate extends Operation {

  private boolean    multipleRows;

  public Aggregate(boolean multipleRows) {
    this.multipleRows = multipleRows;
  }

  public boolean isMultipleRows() {
    return multipleRows;
  }

  public String createReturnType(Boolean java5Compatible) {
    if (isMultipleRows()) {
      if (java5Compatible.booleanValue()) {
        return "List<" + getJavaMethod().getReturnType() + ">";
      } else {
        return "List";
      }
    } else {
      return getJavaMethod().getReturnType();
    }
  }

  public String createReturnForJavadoc() {
    String returnType = getJavaMethod().getReturnType();
    if (isMultipleRows()) {
      return "a List of <code>" + returnType + "</code> objects " +
        "(first column of all returned rows, NULL values are omitted)";
    } else {
      return "result of query (first column of first returned row)";
    }
  }

}
