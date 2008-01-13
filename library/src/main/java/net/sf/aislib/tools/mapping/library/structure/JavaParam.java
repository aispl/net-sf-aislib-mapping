package net.sf.aislib.tools.mapping.library.structure;

public class JavaParam {

  private String name;
  private String type;
  private boolean sensitive;

  public JavaParam(String name, String type, boolean sensitive) {
    this.name = name;
    this.type = type;
    this.sensitive = sensitive;
  }

  public String getName() {
    return name;
  }
  public String getType() {
    return type;
  }
  public boolean isSensitive() {
    return sensitive;
  }
}

