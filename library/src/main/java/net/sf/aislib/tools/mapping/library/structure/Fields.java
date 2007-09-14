package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

public class Fields {

  private List fieldList = new ArrayList();

  public List getFieldList() {
    return fieldList;
  }
  public void addField(Field object) {
    fieldList.add(object);
  }

  // Logic begins here.

  public boolean hasPrimaryKey() {
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      if (field.isPrimaryKey()) {
        return true;
      }
    }
    return false;
  }

  public List getPrimaryKeyList() {
    List primaryKeys = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      if (field.isPrimaryKey()) {
        primaryKeys.add(field);
      }
    }
    return primaryKeys;
  }

  public List getNonPrimaryKeyList() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      if (!field.isPrimaryKey()) {
        fields.add(field);
      }
    }
    return fields;
  }

  public List getReadArgs() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      if (sqlField.hasRead()) {
        fields.addAll(sqlField.getRead().getJavaFields());
      }
    }
    return fields;
  }

  public List getWriteArgs() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      if (sqlField.hasWrite()) {
        fields.addAll(sqlField.getWrite().getJavaFields());
      }
    }
    return fields;
  }

  public List getWriteArgsPrimary() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      if (sqlField.hasWrite() && field.isPrimaryKey()) {
        fields.addAll(sqlField.getWrite().getJavaFields());
      }
    }
    return fields;
  }

  public List getWriteArgsNonPrimary() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      if (sqlField.hasWrite() && !field.isPrimaryKey()) {
        fields.addAll(sqlField.getWrite().getJavaFields());
      }
    }
    return fields;
  }
  public List getFieldListNotOmitted() {
    List fields = new ArrayList();
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      if (sqlField.useOnInsert()) {
        fields.add(field);
      }
    }
    return fields;
  }

  public Field findFieldByName(String name) {
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      if (field.getName().equals(name)) {
        return field;
      }
    }
    return null;
  }

}
