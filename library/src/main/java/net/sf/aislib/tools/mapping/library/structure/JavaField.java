package net.sf.aislib.tools.mapping.library.structure;

public class JavaField {

  private String name;
  private String type;
  private String defaultt;
  private boolean toString = true;

  public JavaField(String name, String type, String defaultt, boolean toString) {
    this.name = name;
    this.type = type;
    this.defaultt = defaultt;
    this.toString = toString;
  }

  public String getName() {
    return name;
  }
  public String getType() {
    return type;
  }
  public String getDefault() {
    if (defaultt == null) {
      return "";
    }
    return defaultt;
  }
  public boolean isToString() {
    return toString;
  }
}
