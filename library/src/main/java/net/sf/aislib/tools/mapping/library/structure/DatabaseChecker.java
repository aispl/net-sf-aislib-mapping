package net.sf.aislib.tools.mapping.library.structure;

import java.util.Iterator;
import java.util.List;

/**
 * Database checker - validates Structure against rules which cannot be
 * validated by XML parser.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class DatabaseChecker {

  /**
   * Checks some constraints which cannot be assured by DTD.
   */
  public void check(Database database) throws IllegalArgumentException {
    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size() ; i < size ; i++) {  // for all structures
      Structure structure   = (Structure) structureList.get(i);
      Fields fields         = structure.getFields();
      Operations operations = structure.getOperations();

      structure.checkPKMethods();

      checkClobTypes(fields);

      if (operations != null) {

        List aggregateList = operations.getAggregateList();
        for (int j = 0, sizej = aggregateList.size() ; j < sizej ; j++) {  // for all aggregates
          Aggregate aggregate   = (Aggregate) aggregateList.get(j);
          JavaMethod javaMethod = aggregate.getJavaMethod();
          SqlQuery sqlQuery     = aggregate.getSqlQuery();
          javaMethod.checkSyntaxInAggregateContext();
          sqlQuery.checkSyntaxInAggregateContext();
        }

        List callList = operations.getCallList();
        for (int j = 0, sizej = callList.size() ; j < sizej ; j++) {  // for all calls
          Call call             = (Call) callList.get(j);
          JavaMethod javaMethod = call.getJavaMethod();
          SqlQuery sqlQuery     = call.getSqlQuery();
          CallParams callParams = call.getCallParams();
          sqlQuery.checkSyntaxInCallContext();
          callParams.checkOutParamCardinality();
          List callParamList    = callParams.getCallParamList();
          for (int k = 0, sizek = callParamList.size() ; k < sizek ; k++) {  // for all callParams
            CallParam callParam = (CallParam) callParamList.get(k);
            callParam.checkAttributes(fields, javaMethod);
          }
        }

        List updateList = operations.getUpdateList();
        for (int j = 0, sizej = updateList.size() ; j < sizej ; j++) {  // for all updates
          Update update   = (Update) updateList.get(j);
          SqlQuery sqlQuery     = update.getSqlQuery();
          sqlQuery.checkSyntaxInUpdateContext();
        }

      }

    }
  }

  private void checkClobTypes(Fields fields) {
    List fieldList = fields.getFieldList();
    for (Iterator iter = fieldList.iterator(); iter.hasNext();) {
      Field field = (Field) iter.next();
      field.checkClobType();
    }
  }

}
