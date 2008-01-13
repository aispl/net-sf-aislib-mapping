package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

public class JavaMethod {

  private String name;
  private String returnType;
  private List javaParamList = new ArrayList();

  public JavaMethod(String name, String returnType) {
    this.name    = name;
    this.returnType = returnType;
  }

  public String getName() {
    return name;
  }
  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String object) {
    returnType = object;
  }

  public boolean hasReturnType() {
    return (returnType != null);
  }

  public List getJavaParamList() {
    return javaParamList;
  }
  public void addJavaParam(JavaParam object) {
    javaParamList.add(object);
  }

  // Logic starts here.

  public JavaParam findJavaParamByName(String name) {
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      if (javaParam.getName().equals(name)) {
        return javaParam;
      }
    }
    return null;
  }

  /**
   * Creates and returns a list containing javaParamList and an additional JavaParam with type
   * from the given JavaClass.
   */
  public List createExtendedJavaParamList(JavaClass javaClass) {
    List result = new ArrayList(javaParamList.size() + 1);
    JavaParam object = new JavaParam("object", javaClass.getName(), false);
    result.add(object);
    result.addAll(javaParamList);
    return result;
  }

  /**
   * Assures this object has valid attributes in an 'aggregate' context.
   */
  public void checkSyntaxInAggregateContext() {
    boolean valid = (hasReturnType());
    if (!valid) {
      throw new IllegalArgumentException("<java-method> in <aggregate> context: 'returnType' is required.");
    }
  }

}
