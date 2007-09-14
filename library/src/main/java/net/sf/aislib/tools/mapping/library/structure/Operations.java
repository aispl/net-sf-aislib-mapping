package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

public class Operations {

  private List aggregateList  = new ArrayList();
  private List callList       = new ArrayList();
  private List countList      = new ArrayList();
  private List deleteList     = new ArrayList();
  private List selectList     = new ArrayList();
  private List updateList     = new ArrayList();

  public List getSelectList() {
    return selectList;
  }
  public void addSelect(Select object) {
    selectList.add(object);
  }
  public List getDeleteList() {
    return deleteList;
  }
  public void addDelete(Delete object) {
    deleteList.add(object);
  }
  public List getUpdateList() {
    return updateList;
  }
  public void addUpdate(Update object) {
    updateList.add(object);
  }
  public List getCountList() {
    return countList;
  }
  public void addCount(Count object) {
    countList.add(object);
  }
  public List getAggregateList() {
    return aggregateList;
  }
  public void addAggregate(Aggregate object) {
    aggregateList.add(object);
  }
  public List getCallList() {
    return callList;
  }
  public void addCall(Call object) {
    callList.add(object);
  }

  public List getAllOperations() {
    List result = new ArrayList(aggregateList);
    result.addAll(callList);
    result.addAll(countList);
    result.addAll(deleteList);
    result.addAll(selectList);
    result.addAll(updateList);
    return result;
  }

}
