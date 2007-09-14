package net.sf.aislib.tools.mapping.library.structure;

public class JavaClass {

  private String  name;
  private boolean propertyChangeSupport;

  public JavaClass(String aname, boolean apropertyChangeSupport) {
    name = aname;
    propertyChangeSupport = apropertyChangeSupport;
  }

  public String getName() {
    return name;
  }

  public boolean isPropertyChangeSupport() {
    return propertyChangeSupport;
  }

}
