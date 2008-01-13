package net.sf.aislib.tools.mapping.library.structure;

import net.sf.aislib.tools.mapping.library.generators.Utils;


public class CallParam {

  private String accessType;
  private String fieldRef;
  private String methodRef;
  private String type;

  private static final String ACCESS_TYPE_IN    = "in";
  private static final String ACCESS_TYPE_OUT   = "out";
  private static final String ACCESS_TYPE_INOUT = "inout";

  public CallParam(String accessType, String fieldRef, String methodRef, String type) {
    this.accessType = accessType;
    this.fieldRef   = fieldRef;
    this.methodRef  = methodRef;
    this.type       = type;
  }

  public String getAccessType() {
    return accessType;
  }
  public String getFieldRef() {
    return fieldRef;
  }
  public String getMethodRef() {
    return methodRef;
  }
  public String getType() {
    return type;
  }
  public boolean hasFieldRef() {
    return (fieldRef != null);
  }
  public boolean hasMethodRef() {
    return (methodRef != null);
  }
  public boolean hasType() {
    return (type != null);
  }
  public boolean isInParam() {
    return (accessType.equals(ACCESS_TYPE_IN));
  }
  public boolean isOutParam() {
    return (accessType.equals(ACCESS_TYPE_OUT));
  }
  public boolean isInOrInoutParam() {
    return (accessType.equals(ACCESS_TYPE_IN)  || accessType.equals(ACCESS_TYPE_INOUT));
  }
  public boolean isOutOrInoutParam() {
    return (accessType.equals(ACCESS_TYPE_OUT) || accessType.equals(ACCESS_TYPE_INOUT));
  }

  // Logic starts here.

  /**
   * Hides methodRef/fieldRef branch.
   * The returned JavaParam may be used to determine type of this CallParam.
   * @return a copy from JavaMethod (methodRef) or a newly created from a JavaField (fieldRef)
   */
  public JavaParam createJavaParam(Fields fields, JavaMethod javaMethod) {
    if (hasFieldRef()) {
      JavaField javaField = fields.findFieldByName(getFieldRef()).getJavaField();
      String name = "object." + Utils.getter(javaField.getName());  // Used in Utils.generatePstmSet..
      String type = javaField.getType();
      return new JavaParam(name, type, javaField.isSensitive());
    }
    if (hasMethodRef()) {
      JavaParam javaParam = javaMethod.findJavaParamByName(getMethodRef());
      return javaParam;
    }
    return null;
  }

  public String determineType(Fields fields, JavaMethod javaMethod) {
    if (hasType()) {
      return type;
    }
    return createJavaParam(fields, javaMethod).getType();
  }

  public void checkAttributes(Fields fields, JavaMethod javaMethod) {
    boolean valid;

    if (isOutParam()) {
      valid = (!hasFieldRef() && !hasMethodRef() && hasType());
      if (!valid) {
        throw new IllegalArgumentException("<call-param>: when access-type=\"out\", only 'type' is allowed and required.");
      }
    }

    if (isInOrInoutParam()) {
      valid = ((hasFieldRef() ^ hasMethodRef()) && !hasType());
      if (!valid) {
        throw new IllegalArgumentException("<call-param>: when access-type!=\"out\", either 'field-ref' or 'method-ref' " +
            "is required and 'type' is forbidden.");
      }
    }

    if (isInOrInoutParam() && hasFieldRef()) {
      if (fields.findFieldByName(fieldRef) == null) {
        throw new IllegalArgumentException("<call-param>: no <field> exists with name \"" + fieldRef + "\"");
      }
    }

    if (isInOrInoutParam() && hasMethodRef()) {
      if (javaMethod.findJavaParamByName(methodRef) == null) {
        throw new IllegalArgumentException("<call-param>: no <java-param> exists with name \"" + methodRef + "\"");
      }
    }

  }

}
