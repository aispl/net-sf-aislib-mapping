package net.sf.aislib.tools.mapping.library.structure;

public class SqlTable {
  
  private String  name;
  private boolean insert;
  private boolean select;
  private boolean delete;
  private boolean update;
  
  public SqlTable(String aname, boolean ainsert, boolean aselect, boolean adelete, boolean aupdate) {
    name   = aname;
    insert = ainsert;
    select = aselect;
    delete = adelete;
    update = aupdate;
  }
  
  public String getName() {
    return name;
  }
  public boolean isInsert() {
    return insert;
  }
  public boolean isSelect() {
    return select;
  }
  public boolean isDelete() {
    return delete;
  }
  public boolean isUpdate() {
    return update;
  }

}

