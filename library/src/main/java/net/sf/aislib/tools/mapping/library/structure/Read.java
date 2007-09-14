package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains Function and JavaField definitions.
 *
 * @author Ania Grzebieniak, AIS.PL
 */
public class Read {

  private String function;

  private List javaFields = new ArrayList();

  public Read(String function) {
    this.function = function;
  }

  public String getFunction() {
    return function;
  }

  public List getJavaFields() {
    return javaFields;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  public void setJavaFields(List javaFields) {
    this.javaFields = javaFields;
  }

  public void addJavaField(JavaField javaField) {
    this.javaFields.add(javaField);
  }
}
