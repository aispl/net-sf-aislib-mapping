package net.sf.aislib.tools.mapping.library.structure;

import java.util.List;

public class Structure {

  private String     name;
  private SqlTable   sqlTable;
  private JavaClass  javaClass;
  private Fields     fields;
  private Operations operations;

  public Structure(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public SqlTable getSqlTable() {
    return sqlTable;
  }
  public void setSqlTable(SqlTable object) {
    sqlTable = object;
  }

  public JavaClass getJavaClass() {
    return javaClass;
  }
  public void setJavaClass(JavaClass object) {
    javaClass = object;
  }

  public Fields getFields() {
    return fields;
  }
  public void setFields(Fields object) {
    fields = object;
  }

  public Operations getOperations() {
    return operations;
  }
  public void setOperations(Operations object) {
    operations = object;
  }

  // Logic starts here.

  /**
   * Check the methods which operate on primary keys.
   */
  public void checkPKMethods() {
    boolean pkDefined      = fields.hasPrimaryKey();
    List nonPrimaryKeyList = fields.getNonPrimaryKeyList();
    String className       = javaClass.getName();

    if (sqlTable.isDelete() && !pkDefined) {
      System.out.println("ERROR - <structure>: 'delete' method wanted for " + className +
          " class but no primary key specified");
    }

    if (sqlTable.isSelect() && !pkDefined) {
      System.out.println("ERROR - <structure>: 'select' method wanted for " + className +
          " class but no primary key specified");
    }

    if (sqlTable.isUpdate() && !pkDefined) {
      System.out.println("ERROR - <structure>: 'update' method wanted for " + className +
          " class but no primary key specified");
    }

    if (sqlTable.isUpdate() && nonPrimaryKeyList.isEmpty()) {
      System.out.println("ERROR - <structure>: 'update' method wanted for " + className +
          " class but all fields are primary keys");
    }
  }

}
