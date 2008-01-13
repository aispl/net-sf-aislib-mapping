package net.sf.aislib.tools.mapping.library.structure;

public class JavaField {

  private String name;
  private String type;
  private String defaultt;
  private boolean sensitive = false;

  public JavaField(String name, String type, String defaultt, boolean sensitive) {
    this.name = name;
    this.type = type;
    this.defaultt = defaultt;
    this.sensitive = sensitive;
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
  public boolean isSensitive() {
    return sensitive;
  }
}
