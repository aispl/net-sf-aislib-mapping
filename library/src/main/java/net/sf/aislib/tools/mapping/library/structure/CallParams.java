package net.sf.aislib.tools.mapping.library.structure;

import java.util.ArrayList;
import java.util.List;

public class CallParams {

  private List callParamList = new ArrayList();

  public List getCallParamList() {
    return callParamList;
  }
  public void addCallParam(CallParam object) {
    callParamList.add(object);
  }

  // Logic starts here.

  /**
   * Returns the first CallParam with accessType='out|inout' or null if not found.
   */
  public CallParam findOutOrInoutParam() {
    for (int i = 0, size = callParamList.size() ; i < size ; i++) {  // for all callParams
      CallParam callParam = (CallParam) callParamList.get(i);
      if (callParam.isOutOrInoutParam()) {
        return callParam;
      }
    }
    return null;
  }

  /**
   * Returns index of the first CallParam with accessType='out|inout' or -1 if not found.
   * Index starts with 0.
   */
  public int getIndexOfOutOrInoutParam() {
    for (int i = 0, size = callParamList.size() ; i < size ; i++) {  // for all callParams
      CallParam callParam = (CallParam) callParamList.get(i);
      if (callParam.isOutOrInoutParam()) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Assures that at most one CallParam has accessType of 'out' or 'inout'.
   */
  public void checkOutParamCardinality() {
    int outCount = 0;
    for (int i = 0, size = callParamList.size() ; i < size ; i++) {  // for all callParams
      CallParam callParam = (CallParam) callParamList.get(i);
      if (callParam.isOutOrInoutParam()) {
        outCount++;
      }
    }
    if (outCount > 1) {
      throw new IllegalArgumentException("<call-params>: only one <call-param> with access-type=\"out|inout\" is allowed.");
    }
  }


}
