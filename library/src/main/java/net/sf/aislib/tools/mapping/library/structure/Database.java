package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

public class Database {

  private List structureList = new ArrayList();
  
  public List getStructureList() {
    return structureList;
  }
  
  public void addStructure(Structure structure) {
    structureList.add(structure);
  }
  
}

