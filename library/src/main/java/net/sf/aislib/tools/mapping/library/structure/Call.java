package net.sf.aislib.tools.mapping.library.structure;

public class Call extends Operation {

  private CallParams callParams;

  public static final String JAVADOC_RETURN_STRING = "result of stored procedure execution";

  public CallParams getCallParams() {
    return callParams;
  }
  public void setCallParams(CallParams object) {
    callParams = object;
  }

  // Logic starts here.

  public String createReturnForJavadoc() {
    CallParam callParam = callParams.findOutOrInoutParam();
    if (callParam == null) {
      return null;
    }
    return JAVADOC_RETURN_STRING ;
  }

  public String createReturnType(Fields fields) {
    CallParam callParam = callParams.findOutOrInoutParam();
    if (callParam == null) {
      return "void";
    }
    return callParam.determineType(fields, getJavaMethod());
  }

}
