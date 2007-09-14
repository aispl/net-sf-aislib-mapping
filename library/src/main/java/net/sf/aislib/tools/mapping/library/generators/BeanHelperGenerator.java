package net.sf.aislib.tools.mapping.library.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.structure.Aggregate;
import net.sf.aislib.tools.mapping.library.structure.Call;
import net.sf.aislib.tools.mapping.library.structure.CallParam;
import net.sf.aislib.tools.mapping.library.structure.CallParams;
import net.sf.aislib.tools.mapping.library.structure.Count;
import net.sf.aislib.tools.mapping.library.structure.Delete;
import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.Fields;
import net.sf.aislib.tools.mapping.library.structure.JavaClass;
import net.sf.aislib.tools.mapping.library.structure.JavaField;
import net.sf.aislib.tools.mapping.library.structure.JavaMethod;
import net.sf.aislib.tools.mapping.library.structure.Operation;
import net.sf.aislib.tools.mapping.library.structure.Operations;
import net.sf.aislib.tools.mapping.library.structure.Select;
import net.sf.aislib.tools.mapping.library.structure.SqlField;
import net.sf.aislib.tools.mapping.library.structure.SqlQuery;
import net.sf.aislib.tools.mapping.library.structure.SqlTable;
import net.sf.aislib.tools.mapping.library.structure.Structure;
import net.sf.aislib.tools.mapping.library.structure.Update;

import org.apache.commons.io.IOUtils;


/**
 * <code>dbhandlers</code> generator.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class BeanHelperGenerator extends Generator {

  private File baseDir;
  private Writer writer;

  private File initDirectory(File destinationDir) {
    File dir = new File(destinationDir.getPath() + File.separator + dbHandlersSubdir);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private void initWriter(String fileName) throws IOException {
    writer = new FileWriter(baseDir.getPath() + File.separator + fileName);
  }

  private void closeWriter() throws IOException {
    writer.close();
  }

  public void generate() throws IOException {
    baseDir = initDirectory(destinationDir);
    MiscPutter miscPutter = new MiscPutter();
    DisuPutter disuPutter = new DisuPutter();
    AggregatePutter aggregatePutter = new AggregatePutter();
    CallPutter callPutter = new CallPutter();
    CountPutter countPutter = new CountPutter();
    DeletePutter deletePutter = new DeletePutter();
    SelectPutter selectPutter = new SelectPutter();
    UpdatePutter updatePutter = new UpdatePutter();
    ReadPutter readPutter = new ReadPutter();
    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size(); i < size; i++) { // all structures
      Structure structure = (Structure) structureList.get(i);
      SqlTable sqlTable = structure.getSqlTable();
      JavaClass javaClass = structure.getJavaClass();
      Fields fields = structure.getFields();

      initWriter(javaClass.getName() + "Handler.java"); // open file
      miscPutter.setWriter(writer);
      miscPutter.setProperties(packageName, javaClass);
      miscPutter.putHeader(); // to file
      disuPutter.setWriter(writer);
      disuPutter.setProperties(sqlTable, javaClass, fields);
      disuPutter.put(); // to file

      aggregatePutter.setWriter(writer);
      callPutter.setWriter(writer);
      countPutter.setWriter(writer);
      deletePutter.setWriter(writer);
      selectPutter.setWriter(writer);
      updatePutter.setWriter(writer);
      Operations operations = structure.getOperations();
      if (operations != null) {
        List aggregateList = operations.getAggregateList();
        for (Iterator aggregateIter = aggregateList.listIterator(); aggregateIter.hasNext();) {
          Aggregate aggregate = (Aggregate) aggregateIter.next();
          aggregatePutter.setProperties(aggregate, sqlTable);
          aggregatePutter.put(); // to file
        }
        List callList = operations.getCallList();
        for (Iterator callIter = callList.listIterator(); callIter.hasNext();) {
          Call call = (Call) callIter.next();
          callPutter.setProperties(call, javaClass, fields);
          callPutter.put(); // to file
        }
        List countList = operations.getCountList();
        for (Iterator countIter = countList.listIterator(); countIter.hasNext();) {
          Count count = (Count) countIter.next();
          countPutter.setProperties(count, sqlTable);
          countPutter.put(); // to file
        }
        List deleteList = operations.getDeleteList();
        for (Iterator deleteIter = deleteList.listIterator(); deleteIter.hasNext();) {
          Delete deletee = (Delete) deleteIter.next();
          deletePutter.setProperties(deletee, sqlTable);
          deletePutter.put(); // to file
        }
        List selectList = operations.getSelectList();
        for (Iterator selectIter = selectList.listIterator(); selectIter.hasNext();) {
          Select select = (Select) selectIter.next();
          selectPutter.setProperties(select, fields, sqlTable, javaClass);
          selectPutter.put(); // to file
        }
        List updateList = operations.getUpdateList();
        for (Iterator updateIter = updateList.listIterator(); updateIter.hasNext();) {
          Update update = (Update) updateIter.next();
          updatePutter.setProperties(update, sqlTable);
          updatePutter.put(); // to file
        }
      }
      readPutter.setWriter(writer);
      readPutter.setProperties(javaClass, fields);
      readPutter.put(); // to file
      miscPutter.putClassEndBrace(); // to file
      closeWriter();
    }
    putEnhancedStatement(structureList);
    log(structureList.size() + " " + dbHandlersSubpackage + ".XHandler classes generated");
  }

  /**
   * Puts a special implementation of PreparedStatement if a raw parameter found
   * in operations.
   */
  private void putEnhancedStatement(List structureList) throws IOException {
    boolean classFileNeeded = false;

    for (int i = 0, size = structureList.size(); i < size; i++) { // all structures
      Structure structure = (Structure) structureList.get(i);
      Operations operations = structure.getOperations();
      if (operations == null) {
        continue;
      }

      List operationList = operations.getAllOperations();
      for (int j = 0, sizej = operationList.size(); j < sizej; j++) {
        Operation operation = (Operation) operationList.get(j);
        if (operation.getSqlQuery().containsRawParam()) {
          classFileNeeded = true;
          break;
        }
      }
    }

    if (!classFileNeeded) {
      return;
    }

    initWriter("EnhancedStatement.java");
    InputStream classSourceStream = getClass().getClassLoader().getResourceAsStream(
      "net/sf/aislib/tools/mapping/library/resources/EnhancedStatement.tpl");
    String classSourceContent = IOUtils.toString(classSourceStream, "ISO-8859-2");
    writer.write("package " + packageName + "." + dbHandlersSubpackage + ";\n");
    writer.write(classSourceContent);
    closeWriter();
    log("EnhancedStatement class generated.");
  }

  //#############################  D/I/S/U  ###########################
  /**
   * Puts <code>delete/insert/select/update</code> methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class DisuPutter {

    private Writer writer;
    private SqlTable sqlTable;
    private JavaClass javaClass;
    private Fields fields;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      String className = javaClass.getName();
      String tableName = sqlTable.getName();
      List fieldList = fields.getFieldList();
      List fieldListNotOmitted = fields.getFieldListNotOmitted();
      List primaryKeyList = fields.getPrimaryKeyList();
      List nonPrimaryKeyList = fields.getNonPrimaryKeyList();
      List readArgs = fields.getReadArgs();
      List writeArgs = fields.getWriteArgs();
      boolean pkDefined = fields.hasPrimaryKey();

      // DELETE put
      if (sqlTable.isDelete() && pkDefined) {
        writer.write("  public static void delete(" + className + " object,\n");
        writer.write("      " + Utils.generateArgs(fields.getWriteArgsPrimary(), null) + "\n");
        writer.write("      Connection con) throws SQLException {\n");
        writer.write("    SQLException sqlEx = null;\n");
        writer.write("    try {\n");
        writer.write("      PreparedStatement pstmt = con.prepareStatement(\"delete from ");
        writer.write(tableName + " where \" +\n");
        writer.write("        \"" + Utils.formatSqlListForWhere(primaryKeyList) + "\");\n");
        writer.write("      int counter = 1;\n");
        writer.write("      try {\n");
        generateObjectIfs(primaryKeyList, null);
        writer.write("        pstmt.executeUpdate();\n");
        writer.write(createEndOfMethod2(false));
      }

      // INSERT put
      if (sqlTable.isInsert()) {
        writer.write("  public static void insert(" + className + " object,\n");
        writer.write("    " + Utils.generateArgs(writeArgs, null) + "\n");
        writer.write("    Connection con) throws SQLException {\n");
        writer.write("    SQLException sqlEx = null;\n");
        writer.write("    try {\n");
        writer.write("      int counter = 1;\n");
        writer.write("      PreparedStatement pstmt = con.prepareStatement(\"insert into ");
        writer.write(tableName + " (\" +\n");
        writer.write("        " + Utils.formatSqlList(fieldListNotOmitted, true) + " +\n");
        writer.write("        \") values (");
        int i = 0;
        for (Iterator iter = fieldListNotOmitted.iterator(); iter.hasNext();) {
          Field field = (Field) iter.next();
          SqlField sqlField = field.getSqlField();
          if (i > 0) {
            writer.write(", ");
          }
          if ((i % 10) == 0) {
            writer.write("\" +\n");
            writer.write("        \"");
          }
          if (sqlField.hasWrite()) {
            writer.write(sqlField.getWrite().getFunction());
          } else {
            writer.write("?");
          }
          i++;
        }
        writer.write(")\");\n");
        writer.write("      try {\n");
        generateObjectIfs(fieldListNotOmitted, null);
        writer.write("        pstmt.executeUpdate();\n");
        writer.write(createEndOfMethod2(false));
      }

      // SELECT put
      if (sqlTable.isSelect() && pkDefined) {
        writer.write("  public static " + className + " select(");
        writer.write("    " + Utils.formatArgList(primaryKeyList) + ",\n");
        writer.write("    " + Utils.generateArgs(readArgs, null) + "\n");
        writer.write("    " + Utils.generateArgs(fields.getWriteArgsPrimary(), new Integer(readArgs.size() + 1)) + "\n");
        writer.write("    Connection con) throws SQLException {\n");
        writer.write("    SQLException sqlEx = null;\n");
        writer.write("    " + className + " result = null;\n");
        writer.write("    try {\n");
        writer.write("      PreparedStatement pstmt = con.prepareStatement(\"select \" +\n");
        writer.write("        " + Utils.formatSqlList(fieldList, false) + " + \" \" +\n");
        writer.write("        \"from " + tableName + " where \" +\n");
        writer.write("        \"" + Utils.formatSqlListForWhere(primaryKeyList) + "\");\n");
        writer.write("      int counter = 1;\n");
        writer.write("      try {\n");
        for (int i = 0, size = readArgs.size(); i < size; i++) {
          JavaField javaField = (JavaField) readArgs.get(i);
          String fieldName = javaField.getName();
          String fieldType = javaField.getType();
          writer.write("        pstmt.set" + Utils.mapTypeToMethod(fieldType));
          writer.write("(counter++, " + fieldName + "_" + (i + 1));
          writer.write(Utils.castMethod(fieldType));
          writer.write(");\n");
        }
        int j = readArgs.size() + 1;
        for (int i = 0, size = primaryKeyList.size(); i < size; i++) {
          Field field = (Field) primaryKeyList.get(i);
          if (field.getSqlField().hasWrite()) {
            for (Iterator iter = field.getSqlField().getWrite().getJavaFields().iterator(); iter.hasNext();) {
              JavaField javaField = (JavaField) iter.next();
              String fieldName = javaField.getName();
              String fieldType = javaField.getType();
              writer.write("        pstmt.set" + Utils.mapTypeToMethod(fieldType));
              writer.write("(counter++, " + fieldName + "_" + j++);
              writer.write(Utils.castMethod(fieldType));
              writer.write(");\n");
            }
          } else {
            JavaField javaField = field.getJavaField();
            String fieldName = javaField.getName();
            String fieldType = javaField.getType();
            writer.write("        pstmt.set" + Utils.mapTypeToMethod(fieldType));
            writer.write("(counter++, " + fieldName);
            writer.write(Utils.castMethod(fieldType));
            writer.write(");\n");
          }
        }
        writer.write("        ResultSet rs = pstmt.executeQuery();\n");
        writer.write("        try {\n");
        writer.write("          if (rs.next()) {\n");
        writer.write("            return read(rs);\n");
        writer.write("          }\n");
        writer.write(createEndOfMethod());
      }

      // UPDATE put
      if (sqlTable.isUpdate() && pkDefined) {
        writer.write("  public static void update(" + className + " object,\n");
        writer.write("    " + Utils.generateArgs(fields.getWriteArgsNonPrimary(), null) + "\n");
        writer.write("    "
          + Utils.generateArgs(fields.getWriteArgsPrimary(), new Integer(fields.getWriteArgsNonPrimary().size() + 1)) + "\n");
        writer.write("    Connection con) throws SQLException {\n");
        writer.write("    SQLException sqlEx = null;\n");
        writer.write("    try {\n");
        writer.write("      PreparedStatement pstmt = con.prepareStatement(\"update ");
        writer.write(tableName + " set \" +\n");
        writer.write("        " + Utils.formatSqlListForUpdate(nonPrimaryKeyList) + " +\n");
        writer.write("        \"where \" +\n");
        writer.write("        \"" + Utils.formatSqlListForWhere(primaryKeyList) + "\");\n");
        writer.write("      int counter = 1;\n");
        writer.write("      try {\n");
        generateObjectIfs(nonPrimaryKeyList, null);
        generateObjectIfs(primaryKeyList, new Integer(fields.getWriteArgsNonPrimary().size() + 1));
        writer.write("        pstmt.executeUpdate();\n");
        writer.write(createEndOfMethod2(false));
      }
    }

    private void generateObjectIfs(List fieldList, Integer startIndex) throws IOException {
      int j = 1;
      if (startIndex != null) {
        j = startIndex.intValue();
      }
      for (int i = 0, size = fieldList.size(); i < size; i++) {
        Field field = (Field) fieldList.get(i);
        JavaField javaField = field.getJavaField();
        SqlField sqlField = field.getSqlField();
        String fieldName;
        String fieldType;

        if (sqlField.hasWrite()) {
          for (Iterator iter = sqlField.getWrite().getJavaFields().iterator(); iter.hasNext();) {
            JavaField javaField2 = (JavaField) iter.next();

            fieldName = javaField2.getName();
            fieldType = javaField2.getType();

            writer.write("        if (" + fieldName + "_" + j + " != null) {\n");
            writer.write("          pstmt.set" + Utils.mapTypeToMethod(fieldType));
            writer.write("(counter++, " + fieldName + "_" + j++);
            writer.write(Utils.castMethod(fieldType));
            writer.write(");\n");
            writer.write("        } else {\n");
            writer.write("          pstmt.setNull(counter++, ");
            writer.write(Utils.getSQLType(fieldType));
            writer.write(");\n");
            writer.write("        }\n");
          }
        } else {

          fieldName = javaField.getName();
          fieldType = javaField.getType();
          writer.write("        if (object." + Utils.getter(fieldName) + " != null) {\n");
          writer.write("          pstmt.set" + Utils.mapTypeToMethod(fieldType));
          writer.write("(counter++, object." + Utils.getter(fieldName));
          writer.write(Utils.castMethod(fieldType));
          writer.write(");\n");
          writer.write("        } else {\n");
          writer.write("          pstmt.setNull(counter++, ");
          writer.write(Utils.getSQLType(fieldType));
          writer.write(");\n");
          writer.write("        }\n");
        }
      }
    }

    public void setProperties(SqlTable sqlTable, JavaClass javaClass, Fields fields) {
      this.sqlTable = sqlTable;
      this.javaClass = javaClass;
      this.fields = fields;
    }
  } // class DisuPutter

  //#############################  READ  ############################
  /**
   * Puts <code>read</code> method.
   * @author Milosz Tylenda, AIS.PL
   */
  class ReadPutter {

    private Writer writer;
    private Fields fields;
    private String className;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      List fieldList = fields.getFieldList();
      writer.write("  public static " + className + " read(ResultSet rs) throws SQLException {\n");
      writer.write("    " + className + " result = new " + className + "();\n");
      writer.write("    int counter = 1;\n");

      for (int i = 0, size = fieldList.size(); i < size; i++) {
        Field field = (Field) fieldList.get(i);
        JavaField javaField = field.getJavaField();
        SqlField sqlField = field.getSqlField();
        if (sqlField.isClobType()) {
          putClobFieldGetting(javaField);
          continue;
        }
        String cappedFieldName = Utils.capitalize(javaField.getName());
        String fieldType = javaField.getType();
        writer.write("    result.set" + cappedFieldName + "(");
        if (Utils.isBaseType(fieldType)) {
          writer.write("new " + fieldType + "(rs.get" + Utils.mapTypeToMethod(fieldType) + "(counter++)));\n");
          writer.write("    if (rs.wasNull()) {\n");
          writer.write("      result.set" + cappedFieldName + "(null);\n");
          writer.write("    }\n");
        } else {
          writer.write("rs.get" + Utils.mapTypeToMethod(fieldType) + "(counter++));\n");
        }
      }
      writer.write("    return result;\n");
      writer.write("  }\n\n");
    }

    public void setProperties(JavaClass javaClass, Fields fields) {
      this.fields = fields;
      this.className = javaClass.getName();
    }

    /**
     * Puts Clob getting code. It is assumed than javaField is of String type.
     * That is, the application sees field mapped to CLOB as String.
     */
    private void putClobFieldGetting(JavaField javaField) throws IOException {
      String cappedFieldName = Utils.capitalize(javaField.getName());
      writer.write("    java.sql.Clob clob" + cappedFieldName + " = rs.getClob(counter++);\n");
      writer.write("    if (clob" + cappedFieldName + " != null) {\n");
      writer.write("      result.set" + cappedFieldName + "(clob" + cappedFieldName + ".getSubString(1L, (int) clob"
        + cappedFieldName + ".length()));\n");
      writer.write("    } else {\n");
      writer.write("      result.set" + cappedFieldName + "(null);\n");
      writer.write("    }\n");
    }

  } // class ReadPutter

  //#############################  OPERATIONS - AGGREGATE  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> aggregate-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class AggregatePutter {

    private Writer writer;
    private Aggregate aggregate;
    private SqlTable sqlTable;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      JavaMethod javaMethod = aggregate.getJavaMethod();
      SqlQuery sqlQuery = aggregate.getSqlQuery();
      List javaParamList = javaMethod.getJavaParamList();

      String query = "SELECT " + sqlQuery.createDistinctClause() + sqlQuery.getColumns()
        + sqlQuery.createFromClause(sqlTable.getName()) + sqlQuery.createWhereClause()
        + sqlQuery.createGroupByAndHavingClause() + sqlQuery.createOrderByClause() + sqlQuery.createOtherClause();

      writer.write(createJavaDoc(query, javaParamList, aggregate.createReturnForJavadoc()));
      if (aggregate.isMultipleRows()) {
        putMultipleRows(query);
      } else {
        putOneRow(query);
      }
    }

    public void setProperties(Aggregate aggregate, SqlTable sqlTable) {
      this.aggregate = aggregate;
      this.sqlTable = sqlTable;
    }

    private void putOneRow(String query) throws IOException {
      JavaMethod javaMethod = aggregate.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      String returnType = javaMethod.getReturnType();

      writer.write("  public static " + returnType + " aggregate" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    " + returnType + " result = null;\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        ResultSet rs = pstmt.executeQuery();\n");
      writer.write("        try {\n");
      writer.write("          if (rs.next()) {\n");
      if (Utils.isBaseType(returnType)) {
        writer.write("            result = new " + returnType + "(rs.get" + Utils.mapTypeToMethod(returnType)
          + "(1));\n");
        writer.write("            if (rs.wasNull()) {\n");
        writer.write("              result = null;\n");
        writer.write("            }\n");
      } else {
        writer.write("            result = rs.get" + Utils.mapTypeToMethod(returnType) + "(1);\n");
      }
      writer.write("          }\n");
      writer.write("          return result;\n");
      writer.write(createEndOfMethod());
    }

    private void putMultipleRows(String query) throws IOException {
      JavaMethod javaMethod = aggregate.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      String returnType = javaMethod.getReturnType();

      writer.write("  public static List aggregate" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    List result = new ArrayList();\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        ResultSet rs = pstmt.executeQuery();\n");
      writer.write("        try {\n");
      writer.write("          while (rs.next()) {\n");
      if (Utils.isBaseType(returnType)) {
        writer.write("            " + returnType + " element = new " + returnType + "(rs.get"
          + Utils.mapTypeToMethod(returnType) + "(1));\n");
      } else {
        writer.write("            " + returnType + " element = rs.get" + Utils.mapTypeToMethod(returnType) + "(1);\n");
      }
      writer.write("            if (!rs.wasNull()) {\n");
      writer.write("              result.add(element);\n");
      writer.write("            }\n");
      writer.write("          }\n");
      writer.write("          return result;\n");
      writer.write(createEndOfMethod());
    }

  } // class AggregatePutter

  //#############################  OPERATIONS - CALL  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> call-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class CallPutter {

    private Writer writer;
    private Call call;
    private JavaClass javaClass;
    private Fields fields;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      CallParams callParams = call.getCallParams();
      JavaMethod javaMethod = call.getJavaMethod();
      String methodName = javaMethod.getName();
      List extendedJavaParamList = javaMethod.createExtendedJavaParamList(javaClass);
      SqlQuery sqlQuery = call.getSqlQuery();

      boolean hasOutOrInoutParam = false;
      String returnType = "void";
      String returnString = null;

      String query = sqlQuery.getBody();
      CallParam outParam = callParams.findOutOrInoutParam();
      if (outParam != null) {
        hasOutOrInoutParam = true;
        returnType = outParam.determineType(fields, javaMethod);
        returnString = Call.JAVADOC_RETURN_STRING;
      }
      writer.write(createJavaDoc(query, extendedJavaParamList, returnString));
      writer.write("  public static " + returnType + " call" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(extendedJavaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      if (hasOutOrInoutParam) {
        writer.write("    " + returnType + " result = null;\n");
      }
      writer.write("    try {\n");
      writer.write("      CallableStatement pstmt = con.prepareCall(" + Utils.equalLengthSplit(query) + ");\n");
      writer.write("      try {\n");
      writer.write(generatePstmtSets(callParams, fields, javaMethod));
      writer.write("        pstmt.executeUpdate();\n");
      if (hasOutOrInoutParam) {
        int outIndex = callParams.getIndexOfOutOrInoutParam() + 1;
        String method = Utils.mapTypeToMethod(returnType) + "(" + outIndex + ")";
        if (Utils.isBaseType(returnType)) {
          writer.write("        result = new " + returnType + "(pstmt.get" + method + ");\n");
          writer.write("        if (pstmt.wasNull()) {\n");
          writer.write("          result = null;\n");
          writer.write("        }\n");
        } else {
          writer.write("        result = pstmt.get" + method + ";\n");
        }
      }
      writer.write(createEndOfMethod2(hasOutOrInoutParam));
    }

    public void setProperties(Call call, JavaClass javaClass, Fields fields) {
      this.call = call;
      this.javaClass = javaClass;
      this.fields = fields;
    }

    /**
     * Generates PreparedStatement.setX() code.
     */
    private String generatePstmtSets(CallParams callParams, Fields fields, JavaMethod javaMethod) {
      List callParamList = callParams.getCallParamList();
      StringBuffer result = new StringBuffer(512);
      for (int i = 0, size = callParamList.size(); i < size; i++) { // for all callParams
        CallParam callParam = (CallParam) callParamList.get(i);

        if (callParam.isInOrInoutParam()) {
          Utils.generatePstmtSet(result, callParam.createJavaParam(fields, javaMethod), i + 1);
        }

        if (callParam.isOutOrInoutParam()) {
          String sqlType = Utils.getSQLType(callParam.determineType(fields, javaMethod));
          result.append("        pstmt.registerOutParameter(" + (i + 1) + ", " + sqlType + ");\n");
        }

      }
      return result.toString();
    }

  } // class CallPutter

  //#############################  OPERATIONS - COUNT  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> count-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class CountPutter {

    private Writer writer;
    private Count count;
    private SqlTable sqlTable;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      JavaMethod javaMethod = count.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      SqlQuery sqlQuery = count.getSqlQuery();

      String query = "SELECT COUNT(*) " + sqlQuery.createFromClause(sqlTable.getName()) + sqlQuery.createWhereClause()
        + sqlQuery.createOtherClause();
      writer.write(createJavaDoc(query, javaParamList, "number of counted rows"));
      writer.write("  public static int count" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    int result = 0;\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        ResultSet rs = pstmt.executeQuery();\n");
      writer.write("        try {\n");
      writer.write("          if (rs.next()) {\n");
      writer.write("            result = rs.getInt(1);\n");
      writer.write("          }\n");
      writer.write("          return result;\n");
      writer.write(createEndOfMethod());
    }

    public void setProperties(Count count, SqlTable sqlTable) {
      this.count = count;
      this.sqlTable = sqlTable;
    }
  } // class CountPutter

  //#############################  OPERATIONS - SELECT  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> select-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class SelectPutter {

    private Writer writer;
    private Select select;
    private Fields fields;
    private SqlTable sqlTable;
    private JavaClass javaClass;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      List fieldList = fields.getFieldList();
      SqlQuery sqlQuery = select.getSqlQuery();
      JavaMethod javaMethod = select.getJavaMethod();
      List javaParamList = javaMethod.getJavaParamList();

      String query = "SELECT " + sqlQuery.createDistinctClause()
        + sqlQuery.createColumnsClause(fieldList, sqlTable.getName()) + sqlQuery.createFromClause(sqlTable.getName())
        + sqlQuery.createWhereClause() + sqlQuery.createGroupByAndHavingClause() + sqlQuery.createOrderByClause()
        + sqlQuery.createOtherClause();

      writer.write(createJavaDoc(query, javaParamList, select.createReturnForJavadoc(javaClass)));
      if (select.isMultipleRows()) {
        putMultipleRows(query);
      } else {
        putOneRow(query);
      }
    }

    public void setProperties(Select select, Fields fields, SqlTable sqlTable, JavaClass javaClass) {
      this.select = select;
      this.fields = fields;
      this.sqlTable = sqlTable;
      this.javaClass = javaClass;
    }

    private void putOneRow(String query) throws IOException {
      JavaMethod javaMethod = select.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      String className = javaClass.getName();

      writer.write("  public static " + className + " select" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    " + className + " result = null;\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        ResultSet rs = pstmt.executeQuery();\n");
      writer.write("        try {\n");
      writer.write("          if (rs.next()) {\n");
      writer.write("            result = read(rs);\n");
      writer.write("          }\n");
      writer.write("          return result;\n");
      writer.write(createEndOfMethod());
    }

    private void putMultipleRows(String query) throws IOException {
      JavaMethod javaMethod = select.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();

      if (java5Compatible.booleanValue()) {
        writer.write("  public static List<" + javaClass.getName() + "> select" + Utils.capitalize(methodName) + "(");
      } else {
        writer.write("  public static List select" + Utils.capitalize(methodName) + "(");
      }
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      if (java5Compatible.booleanValue()) {
        writer.write("    List<" + javaClass.getName() + "> result = new ArrayList<" + javaClass.getName() + ">();\n");
      } else {
        writer.write("    List result = new ArrayList();\n");
      }
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        ResultSet rs = pstmt.executeQuery();\n");
      writer.write("        try {\n");
      writer.write("          while (rs.next()) {\n");
      writer.write("            result.add(read(rs));\n");
      writer.write("          }\n");
      writer.write("          return result;\n");
      writer.write(createEndOfMethod());
    }

  } // class SelectPutter

  //#############################  OPERATIONS - DELETE  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> delete-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class DeletePutter {

    private Writer writer;
    private Delete deletee;
    private SqlTable sqlTable;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      JavaMethod javaMethod = deletee.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      SqlQuery sqlQuery = deletee.getSqlQuery();

      String query = "DELETE FROM " + sqlTable.getName() + sqlQuery.createWhereClause() + sqlQuery.createOtherClause();
      writer.write(createJavaDoc(query, javaParamList, "number of deleted rows"));
      writer.write("  public static int delete" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    int result = 0;\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        result = pstmt.executeUpdate();\n");
      writer.write(createEndOfMethod2(true));
    }

    public void setProperties(Delete deletee, SqlTable sqlTable) {
      this.deletee = deletee;
      this.sqlTable = sqlTable;
    }
  } // class DeletePutter

  //#############################  OPERATIONS - UPDATE  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> update-methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class UpdatePutter {

    private Writer writer;
    private Update update;
    private SqlTable sqlTable;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      JavaMethod javaMethod = update.getJavaMethod();
      String methodName = javaMethod.getName();
      List javaParamList = javaMethod.getJavaParamList();
      SqlQuery sqlQuery = update.getSqlQuery();

      String query = "UPDATE " + sqlTable.getName() + " SET " + sqlQuery.getSet() + sqlQuery.createWhereClause()
        + sqlQuery.createOtherClause();
      writer.write(createJavaDoc(query, javaParamList, "number of updated rows"));
      writer.write("  public static int update" + Utils.capitalize(methodName) + "(");
      writer.write(Utils.formatMethodArgs(javaParamList));
      writer.write("Connection con) throws SQLException {\n");
      writer.write("    SQLException sqlEx = null;\n");
      writer.write("    int result = 0;\n");
      writer.write("    try {\n");
      writer.write(Utils.createPreparedStatement(query));
      writer.write("      try {\n");
      writer.write(Utils.generatePstmtSets(javaParamList));
      writer.write("        result = pstmt.executeUpdate();\n");
      writer.write(createEndOfMethod2(true));
    }

    public void setProperties(Update update, SqlTable sqlTable) {
      this.update = update;
      this.sqlTable = sqlTable;
    }
  } // class UpdatePutter

  //#############################  MISC  ###########################
  /**
   * Puts some little things like <CODE>package</CODE> string and class end brace.
   * @author Milosz Tylenda, AIS.PL
   */
  class MiscPutter {

    private Writer writer;
    private String packagePrefix;
    private String className;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void setProperties(String aPackagePrefix, JavaClass javaClass) {
      packagePrefix = aPackagePrefix;
      className = javaClass.getName();
    }

    public void putHeader() throws IOException {
      writer.write("package " + packagePrefix + "." + dbHandlersSubpackage + ";\n\n");
      writer.write("// THIS FILE HAS BEEN GENERATED AUTOMAGICALLY. DO NOT EDIT!\n\n");
      writer.write("import java.math.BigDecimal;\n\n");
      writer.write("import java.sql.Connection;\n");
      writer.write("import java.sql.CallableStatement;\n");
      writer.write("import java.sql.PreparedStatement;\n");
      writer.write("import java.sql.ResultSet;\n");
      writer.write("import java.sql.SQLException;\n");
      writer.write("import java.sql.Timestamp;\n\n");
      writer.write("import java.util.ArrayList;\n");
      writer.write("import java.util.List;\n\n");
      writer.write("import " + packagePrefix + "." + objectsSubpackage + "." + className + ";\n\n");
      writer.write("/**\n");
      writer.write(" * <code>" + className + "</code> database handler.\n");
      writer.write(" * @author BeanHelperGenerator\n");
      writer.write(" */\n");
      writer.write("public class " + className + "Handler {\n\n");
    }

    public void putClassEndBrace() throws IOException {
      writer.write("}\n");
    }

  } // class MiscPutter

  /**
   * Common code used in SELECT queries.
   */
  String createEndOfMethod() {
    return "        } catch (SQLException sqle1) {\n" + "          sqlEx = sqle1;\n" + "        } finally {\n"
      + "          rs.close();\n" + "        }\n" + "      } catch (SQLException sqle2) {\n"
      + "        if (sqlEx == null) {\n" + "          sqlEx = sqle2;\n" + "        }\n" + "      } finally {\n"
      + "        pstmt.close();\n" + "      }\n" + "    } catch (SQLException sqle3) {\n"
      + "      if (sqlEx == null) {\n" + "        sqlEx = sqle3;\n" + "      }\n" + "    } finally {\n"
      + "      if (sqlEx != null) {\n" + "        throw sqlEx;\n" + "      }\n" + "    }\n" + "    return result;\n"
      + "  }\n\n";
  }

  /**
   * Common code used in DELETE/INSERT/UPDATE/SP queries.
   */
  String createEndOfMethod2(boolean createReturn) {
    StringBuffer result = new StringBuffer(512);
    result.append("      } catch (SQLException sqle1) {\n" + "        sqlEx = sqle1;\n" + "      } finally {\n"
      + "        pstmt.close();\n" + "      }\n" + "    } catch (SQLException sqle2) {\n"
      + "      if (sqlEx == null) {\n" + "        sqlEx = sqle2;\n" + "      }\n" + "    } finally {\n"
      + "      if (sqlEx != null) {\n" + "        throw sqlEx;\n" + "      }\n" + "    }\n");
    if (createReturn) {
      result.append("    return result;\n");
    }
    result.append("  }\n\n");
    return result.toString();
  }

  /**
   * JavaDoc before method.
   */
  String createJavaDoc(String query, List javaParamList, String returnString) {
    StringBuffer result = new StringBuffer(512);
    result.append("  /**\n");
    result.append("   * <code>" + Utils.protectHTML(query) + "</code>.\n");
    result.append(Utils.formatMethodArgsForJavaDoc(javaParamList));
    result.append("   * @param con the <code>Connection</code> to database\n");
    if (returnString != null) {
      result.append("   * @return " + returnString + "\n");
    }
    result.append("   * @throws SQLException when operation failed\n");
    result.append("   */\n");
    return result.toString();
  }

}
