package net.sf.aislib.tools.mapping.library.structure;

public class Select extends Operation {

  private boolean    multipleRows;

  public Select(boolean multipleRows) {
    this.multipleRows = multipleRows;
  }

  public boolean isMultipleRows() {
    return multipleRows;
  }

  public String createReturnType(JavaClass javaClass, boolean useGenerics) {
    if (isMultipleRows()) {
      if (useGenerics) {
        return "List<" + javaClass.getName() + ">";
      } else {
        return "List";
      }
    } else {
      return javaClass.getName();
    }
  }

  public String createReturnForJavadoc(JavaClass javaClass) {
    String className = javaClass.getName();
    if (isMultipleRows()) {
      return "a List of <code>" + className + "</code> objects";
    } else {
      return "an instance of <code>" + className + "</code> or <code>null</code>";
    }
  }

}
