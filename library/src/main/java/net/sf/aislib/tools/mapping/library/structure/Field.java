package net.sf.aislib.tools.mapping.library.structure;

/**
 * Contains SqlField and JavaField definitions.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class Field implements Comparable {

  private String  name;
  private boolean notNull;
  private boolean primaryKey;

  private SqlField  sqlField;
  private JavaField javaField;

  public Field(String name, boolean notNull, boolean primaryKey) {
    this.name = name;
    this.notNull = notNull;
    this.primaryKey = primaryKey;
  }

  public String getName() {
    return name;
  }
  public boolean isNotNull() {
    return notNull;
  }
  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public SqlField getSqlField() {
    return sqlField;
  }
  public void setSqlField(SqlField object) {
    sqlField = object;
  }
  public JavaField getJavaField() {
    return javaField;
  }
  public void setJavaField(JavaField object) {
    javaField = object;
  }

  /**
   * Sorts by primary keys then by javaNames. Primary keys go first.
   */
  public int compareTo(Object o) {
    Field field2 = (Field) o;
    String javaName = javaField.getName();
    if (primaryKey) {
      if (field2.primaryKey) {
        return javaName.compareTo(field2.javaField.getName());
      } else {
        return -1;
      }
    } else {
      if (field2.primaryKey) {
        return 1;
      } else {
        return javaName.compareTo(field2.javaField.getName());
      }
    }
  }

  /**
   * Assures that CLOB SQL-type can be associated only with String Java-type.
   */
  public void checkClobType() {
    boolean valid = !(sqlField.isClobType() && !javaField.getType().equals("String"));
    if (!valid) {
      throw new IllegalArgumentException("<field> : CLOB type can be bound to String only.");
    }
  }

}
