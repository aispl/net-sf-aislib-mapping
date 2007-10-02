package net.sf.aislib.tools.mapping.library.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.structure.Aggregate;
import net.sf.aislib.tools.mapping.library.structure.Call;
import net.sf.aislib.tools.mapping.library.structure.Count;
import net.sf.aislib.tools.mapping.library.structure.Delete;
import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.Fields;
import net.sf.aislib.tools.mapping.library.structure.JavaClass;
import net.sf.aislib.tools.mapping.library.structure.JavaField;
import net.sf.aislib.tools.mapping.library.structure.JavaMethod;
import net.sf.aislib.tools.mapping.library.structure.JavaParam;
import net.sf.aislib.tools.mapping.library.structure.Operations;
import net.sf.aislib.tools.mapping.library.structure.Select;
import net.sf.aislib.tools.mapping.library.structure.SqlTable;
import net.sf.aislib.tools.mapping.library.structure.Structure;
import net.sf.aislib.tools.mapping.library.structure.Update;

/**
 * <code>ApplicationDatabase.java</code> generator.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class DatabaseGenerator extends Generator {

  private String databaseClassName = "ApplicationDatabase";

  private File baseDir;
  private Writer writer;

  public void setDatabaseClassName(String databaseClassName) {
    this.databaseClassName = databaseClassName;
  }

  private File initDirectory(File destinationDir) throws IOException {
    File dir = new File(destinationDir.getPath());
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private void initWriter(String className) throws IOException {
    writer = new FileWriter(baseDir.getPath() + File.separator + className + ".java");
  }

  private void closeWriter() throws IOException {
    writer.close();
  }

  public void generate() throws IOException {
    log("Generating " + databaseClassName + ":");
    baseDir = initDirectory(destinationDir);
    initWriter(databaseClassName);    // open file
    MiscPutter miscPutter = new MiscPutter(writer);
    DisuPutter disuPutter = new DisuPutter(writer);
    OperationsPutter operationsPutter = new OperationsPutter(writer);
    miscPutter.setProperties(packageName, databaseClassName);
    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size() ; i < size ; i++) {  // all structures
      Structure structure = (Structure) structureList.get(i);
      JavaClass javaClass = structure.getJavaClass();
      miscPutter.addJavaClass(javaClass);  // used in "import... ;"
    }
    miscPutter.putHeader(); // to file
    for (int i = 0, size = structureList.size() ; i < size ; i++) {  // all structures
      Structure structure = (Structure) structureList.get(i);
      SqlTable  sqlTable  = structure.getSqlTable();
      JavaClass javaClass = structure.getJavaClass();
      Fields    fields    = structure.getFields();
      log("  Parsing structure " + structure.getName());
      disuPutter.setProperties(sqlTable, javaClass, fields);
      disuPutter.put(); // to file

      Operations operations = structure.getOperations();
      if (operations != null) {
        // Generate aggregates.
        List aggregateList = operations.getAggregateList();
        for (Iterator aggregateIter = aggregateList.listIterator() ; aggregateIter.hasNext() ; ) {
          Aggregate aggregate = (Aggregate) aggregateIter.next();
          operationsPutter.putAggregate(aggregate, javaClass); // to file
        }
        // Generate calls.
        List callList = operations.getCallList();
        for (Iterator callIter = callList.listIterator() ; callIter.hasNext() ; ) {
          Call call = (Call) callIter.next();
          operationsPutter.putCall(call, javaClass, fields); // to file
        }
        // Generate counts.
        List countList = operations.getCountList();
        for (Iterator countIter = countList.listIterator() ; countIter.hasNext() ; ) {
          Count count = (Count) countIter.next();
          operationsPutter.putCount(count.getJavaMethod(), javaClass); // to file
        }
        // Generate deletes.
        List deleteList = operations.getDeleteList();
        for (Iterator deleteIter = deleteList.listIterator() ; deleteIter.hasNext() ; ) {
          Delete deletee = (Delete) deleteIter.next();
          operationsPutter.putDelete(deletee.getJavaMethod(), javaClass); // to file
        }
        // Generate selects.
        List selectList = operations.getSelectList();
        for (Iterator selectIter = selectList.listIterator() ; selectIter.hasNext() ; ) {
          Select select = (Select) selectIter.next();
          operationsPutter.putSelect(select, javaClass); // to file
        }
        // Generate updates.
        List updateList = operations.getUpdateList();
        for (Iterator updateIter = updateList.listIterator() ; updateIter.hasNext() ; ) {
          Update update = (Update) updateIter.next();
          operationsPutter.putUpdate(update.getJavaMethod(), javaClass); // to file
        }
      }
    }
    miscPutter.putExceptionCaught(); // to file
    miscPutter.putClassEndBrace(); // to file
    closeWriter();
    log(databaseClassName + " class generated:");
    log("  " + disuPutter.deleteCount + " delete method(s) generated");
    log("  " + disuPutter.insertCount + " insert method(s) generated");
    log("  " + disuPutter.selectCount + " select method(s) generated");
    log("  " + disuPutter.updateCount + " update method(s) generated");
    log("  " + operationsPutter.aggregateCount  + " operations/aggregate method(s) generated");
    log("  " + operationsPutter.callCount   + " operations/call method(s) generated");
    log("  " + operationsPutter.countCount  + " operations/count method(s) generated");
    log("  " + operationsPutter.deleteCount + " operations/delete method(s) generated");
    log("  " + operationsPutter.selectCount + " operations/select method(s) generated");
    log("  " + operationsPutter.updateCount + " operations/update method(s) generated");
  }


  //#############################  D/I/S/U  ###########################
  /**
   * Puts <code>delete/insert/select/update</code> methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class DisuPutter {

    private Writer writer;
    private SqlTable  sqlTable;
    private JavaClass javaClass;
    private Fields    fields;
    public int deleteCount = 0;
    public int insertCount = 0;
    public int selectCount = 0;
    public int updateCount = 0;

    public DisuPutter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      String className = javaClass.getName();
      List fieldList = fields.getFieldList();

      // DELETE put
      if (sqlTable.isDelete() && fields.hasPrimaryKey()) {
        writer.write(createDIUMethodBody("delete", className, fields.getWriteArgsPrimary()));
        deleteCount++;
      }

      // INSERT put
      if (sqlTable.isInsert()) {
        writer.write(createDIUMethodBody("insert", className, fields.getWriteArgs()));
        insertCount++;
      }

      // SELECT put
      if (sqlTable.isSelect()) {

        // Put primary keys into a list
        List primaryKeys = new ArrayList();
        for (int i = 0, size = fieldList.size() ; i < size ; i++) {
          Field field = (Field) fieldList.get(i);
          if (field.isPrimaryKey()) {
            primaryKeys.add(field);
          }
        }

        if (!primaryKeys.isEmpty()) {
          String fullMethodName = Utils.createMethodName("select", className, null);
          writer.write("  public " + className + " " + fullMethodName);
          writer.write("(\n");
          for (int i = 0, size = primaryKeys.size() ; i < size ; i++) {
            Field     field      = (Field) primaryKeys.get(i);
            JavaField javaField  = field.getJavaField();
            writer.write("      " + javaField.getType() + " " + javaField.getName());
            if (i < size - 1) {
              writer.write(",");
            } else if (fields.getReadArgs().size() > 0) {
              writer.write(",\n");
            } else {
              writer.write("\n");
            }
          }
          if (fields.getWriteArgsPrimary().size() > 0) {
            writer.write("      " + Utils.generateArgs(fields.getReadArgs(), null) + "\n");
            writer.write("      " + Utils.generateArgsWithoutLastComa(fields.getWriteArgsPrimary(), new Integer(fields.getReadArgs().size() + 1)) + "\n");
          } else {
            writer.write("      " + Utils.generateArgsWithoutLastComa(fields.getReadArgs(), null) + "\n");
          }
          writer.write("    ) throws SQLException {\n");
          writer.write("    Connection con = manager.getConnection();\n");
          writer.write("    try {\n");
          writer.write("      return " + className);
          writer.write("Handler.select(\n");
          writer.write("       ");
          for (int i = 0, size = primaryKeys.size() ; i < size ; i++) {
            Field     field      = (Field) primaryKeys.get(i);
            JavaField javaField  = field.getJavaField();
            writer.write( " " + javaField.getName() + ",");
          }
          writer.write("\n");
          writer.write("        " + Utils.getArgs(fields.getReadArgs(), null) + "\n");
          writer.write("        " + Utils.getArgs(fields.getWriteArgsPrimary(), new Integer(fields.getReadArgs().size() + 1)) + "\n");
          writer.write("        con);\n");
          writer.write("    } catch (SQLException sqle) {\n");
          writer.write(createCallToExceptionCaught(fullMethodName, primaryKeys));
          writer.write("      throw sqle;\n");
          writer.write("    } finally {\n");
          writer.write("      manager.releaseConnection(con);\n");
          writer.write("    }\n");
          writer.write("  }\n\n");
          selectCount++;
        }
      }

      // UPDATE put
      if (sqlTable.isUpdate() && fields.hasPrimaryKey()) {
        writer.write(createDIUMethodBody("update", className, fields.getWriteArgs()));
        updateCount++;
      }
    }

    public void setProperties(SqlTable sqlTable, JavaClass javaClass, Fields fields) {
      this.sqlTable = sqlTable;
      this.javaClass = javaClass;
      this.fields = fields;
    }

    /**
     * Returns a method body for the given operation type (delete, insert, update).
     */
    private String createDIUMethodBody(String diu, String className, List args) {
      StringBuffer sb = new StringBuffer(512);
      String fullMethodName = Utils.createMethodName(diu, className, null);
      sb.append("  public void " + fullMethodName + "(" + className + " object");
      if (args.size() > 0) {
        sb.append(",\n");
      }
      sb.append("      " + Utils.generateArgsWithoutLastComa(args, null) + "\n");
      sb.append("    ) throws SQLException {\n");
      sb.append("    Connection con = manager.getConnection();\n");
      sb.append("    try {\n");
      sb.append("      " + className + "Handler." + diu + "(object,\n");
      sb.append("      " + Utils.getArgs(args, null) + " con);\n");
      sb.append("    } catch (SQLException sqle) {\n");
      sb.append(createCallToExceptionCaught(fullMethodName));
      sb.append("      throw sqle;\n");
      sb.append("    } finally {\n");
      sb.append("      manager.releaseConnection(con);\n");
      sb.append("    }\n");
      sb.append("  }\n\n");
      return sb.toString();
    }

    /**
     * Creates exceptionCaught() invocation from DIU method.
     */
    private String createCallToExceptionCaught(String fullMethodName) {
      StringBuffer sb = new StringBuffer(512);
      sb.append("      Map args = new HashMap();\n");
      sb.append("      args.put(\"object\", object);\n");
      sb.append("      exceptionCaught(sqle, \"" + fullMethodName + "\", args);\n");
      return sb.toString();
    }

    /**
     * Creates exceptionCaught() invocation from select-method body.
     */
    private String createCallToExceptionCaught(String fullMethodName, List primaryKeyList) {
      StringBuffer sb = new StringBuffer(512);
      sb.append("      Map args = new HashMap();\n");
      for (int i = 0, size = primaryKeyList.size() ; i < size ; i++) {
        Field     field      = (Field) primaryKeyList.get(i);
        JavaField javaField  = field.getJavaField();
        sb.append("      args.put(\"" + javaField.getName() + "\", " + javaField.getName() + ");\n");
      }
      sb.append("      exceptionCaught(sqle, \"" + fullMethodName + "\", args);\n");
      return sb.toString();
    }

  } // class DisuPutter


  //##########################  <operations> METHODS  ############################
  /**
   * Puts <code>&lt;operations&gt;</code> methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class OperationsPutter {

    private Writer    writer;
    public  int       aggregateCount  = 0;
    public  int       callCount       = 0;
    public  int       countCount      = 0;
    public  int       deleteCount     = 0;
    public  int       selectCount     = 0;
    public  int       updateCount     = 0;

    public OperationsPutter(Writer awriter) {
      writer = awriter;
    }

    public void putAggregate(Aggregate aggregate, JavaClass javaClass) throws IOException {
      String className      = javaClass.getName();
      JavaMethod javaMethod = aggregate.getJavaMethod();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.getJavaParamList();
      writer.write(createOperationMethodBody(aggregate.createReturnType(useGenerics), "aggregate", className,
            methodName, javaParamList, aggregate.createReturnForJavadoc()));
      aggregateCount++;
    }

    public void putCall(Call call, JavaClass javaClass, Fields fields) throws IOException {
      String className      = javaClass.getName();
      JavaMethod javaMethod = call.getJavaMethod();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.createExtendedJavaParamList(javaClass);
      writer.write(createOperationMethodBody(call.createReturnType(fields), "call", className, methodName, javaParamList,
            call.createReturnForJavadoc()));
      callCount++;
    }

    public void putCount(JavaMethod javaMethod, JavaClass javaClass) throws IOException {
      String className      = javaClass.getName();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.getJavaParamList();
      writer.write(createOperationMethodBody("int", "count", className, methodName, javaParamList,
            "number of counted rows"));
      countCount++;
    }

    public void putDelete(JavaMethod javaMethod, JavaClass javaClass) throws IOException {
      String className      = javaClass.getName();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.getJavaParamList();
      writer.write(createOperationMethodBody("int", "delete", className, methodName, javaParamList,
            "number of deleted rows"));
      deleteCount++;
    }

    public void putSelect(Select select, JavaClass javaClass) throws IOException {
      String className      = javaClass.getName();
      JavaMethod javaMethod = select.getJavaMethod();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.getJavaParamList();
      writer.write(createOperationMethodBody(select.createReturnType(javaClass, useGenerics), "select", className, methodName,
            javaParamList, select.createReturnForJavadoc(javaClass)));
      selectCount++;
    }

    public void putUpdate(JavaMethod javaMethod, JavaClass javaClass) throws IOException {
      String className      = javaClass.getName();
      String methodName     = javaMethod.getName();
      List javaParamList    = javaMethod.getJavaParamList();
      writer.write(createOperationMethodBody("int", "update", className, methodName, javaParamList,
            "number of updated rows"));
      updateCount++;
    }

    /**
     * Returns a method body for the given operation type.
     */
    private String createOperationMethodBody(String returnType, String operation, String className, String methodName,
        List javaParamList, String returnString) {
      StringBuffer sb        = new StringBuffer(1024);
      String fullMethodName  = Utils.createMethodName(operation, className, methodName);
      String shortMethodName = Utils.createMethodName(operation, null, methodName);
      String returnOrNothing = "return ";
      if (returnType.equals("void")) {
        returnOrNothing = "";
      }
      sb.append("  /**\n");
      sb.append("   * <code>" + fullMethodName + "</code>.\n");
      sb.append(Utils.formatMethodArgsForJavaDoc(javaParamList));
      if (returnString != null) {
        sb.append("   * @return " + returnString + "\n");
      }
      sb.append("   * @throws SQLException when operation failed\n");
      sb.append("   * @see " + className + "Handler#" + shortMethodName + "\n");
      sb.append("   */\n");
      sb.append("  public " + returnType + " " + fullMethodName + "(\n");
      sb.append("   ");
      sb.append(Utils.formatMethodArgsWithoutComma(javaParamList));
      sb.append("    ) throws SQLException {\n");
      sb.append("    Connection con = manager.getConnection();\n");
      sb.append("    try {\n");
      sb.append("      " + returnOrNothing + className + "Handler." + shortMethodName + "(\n");
      sb.append("        ");
      sb.append(Utils.formatMethodParams(javaParamList));
      sb.append("con);\n");
      sb.append("    } catch (SQLException sqle) {\n");
      sb.append(createCallToExceptionCaught(fullMethodName, javaParamList));
      sb.append("      throw sqle;\n");
      sb.append("    } finally {\n");
      sb.append("      manager.releaseConnection(con);\n");
      sb.append("    }\n");
      sb.append("  }\n\n");
      return sb.toString();
    }

    /**
     * Creates exceptionCaught() invocation from operations-method body.
     */
    private String createCallToExceptionCaught(String fullMethodName, List javaParamList) {
      StringBuffer sb = new StringBuffer(512);
      sb.append("      Map args = new HashMap();\n");
      for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
        JavaParam javaParam = (JavaParam) javaParamList.get(i);
        sb.append("      args.put(\"" + javaParam.getName() + "\", " + javaParam.getName() + ");\n");
      }
      sb.append("      exceptionCaught(sqle, \"" + fullMethodName + "\", args);\n");
      return sb.toString();
    }

  } // class OperationsPutter


  //#############################  MISC  ###########################
  /**
   * Puts some little things like a <code>package</code> string and a class ending brace.
   * @author Milosz Tylenda, AIS.PL
   */
  class MiscPutter {

    private Writer writer;
    private List   javaClassList = new ArrayList();
    private String packagePrefix;
    private String className;

    public MiscPutter(Writer awriter) {
      writer = awriter;
    }

    public void putHeader() throws IOException {
      writer.write("package " + packagePrefix + ";\n\n");
      writer.write("// THIS FILE HAS BEEN GENERATED AUTOMAGICALLY. DO NOT EDIT!\n\n");
      writer.write("import java.math.BigDecimal;\n\n");
      writer.write("import java.sql.Connection;\n");
      writer.write("import java.sql.SQLException;\n");
      writer.write("import java.sql.Timestamp;\n\n");
      writer.write("import java.util.HashMap;\n");
      writer.write("import java.util.Iterator;\n");
      writer.write("import java.util.List;\n");
      writer.write("import java.util.Map;\n\n");
      if (aislibDependent) {
        writer.write("import pl.aislib.fm.Database;\n\n");
        writer.write("import pl.aislib.fm.jdbc.Manager;\n\n");
      } else {
        writer.write("import javax.sql.DataSource;\n");
        writer.write("import org.apache.commons.logging.Log;\n");
        writer.write("import org.apache.commons.logging.LogFactory;\n\n");
      }
      for (int i = 0, size = javaClassList.size() ; i < size ; i++) {
        JavaClass javaClass = (JavaClass) javaClassList.get(i);
        String name = javaClass.getName();
        writer.write("import " + packagePrefix + "." + objectsSubpackage + "." + name + ";\n");
      }
      writer.write("\n");
      for (int i = 0, size = javaClassList.size() ; i < size ; i++) {
        JavaClass javaClass = (JavaClass) javaClassList.get(i);
        String name = javaClass.getName();
        writer.write("import " + packagePrefix + "." + dbHandlersSubpackage + "." + name + "Handler;\n");
      }
      writer.write("\n");
      writer.write("/**\n");
      writer.write(" * Application Database.\n");
      writer.write(" * @author DatabaseGenerator\n");
      writer.write(" */\n");
      if (useGenerics) {
        writer.write("@SuppressWarnings(\"unchecked\")\n");
      }
      if (aislibDependent) {
        writer.write("public class " + className + " extends Database {\n\n");
        writer.write("  public " + className + "(Manager manager) {\n");
        writer.write("    super(manager);\n");
        writer.write("  }\n\n");
      } else {
        writer.write("public class " + className + " {\n\n");
        writer.write("  protected Manager manager;\n\n");
        writer.write("  protected Log log;\n\n");
        writer.write("  public " + className + "(DataSource dataSource, Log log) {\n");
        writer.write("    this.manager = new Manager(dataSource);\n");
        writer.write("    this.log = log;\n");
        writer.write("  }\n\n");
        writer.write("  public " + className + "(DataSource dataSource) {\n");
        writer.write("    this(dataSource, LogFactory.getLog(" + className + ".class));\n");
        writer.write("  }\n\n");

        writer.write("  public class Manager {\n");
        writer.write("    protected DataSource dataSource;\n\n");
        writer.write("    public Manager(DataSource dataSource) {\n");
        writer.write("      this.dataSource = dataSource;\n");
        writer.write("    }\n");
        writer.write("    public Connection getConnection() throws SQLException {\n");
        writer.write("      return dataSource.getConnection();\n");
        writer.write("    }\n");
        writer.write("    public void releaseConnection(Connection con) throws SQLException {\n");
        writer.write("      con.close();\n");
        writer.write("    }\n");
        writer.write("  }\n");
      }
    }

    public void putClassEndBrace() throws IOException {
      writer.write("}\n");
    }

    public void addJavaClass(JavaClass javaClass) {
      javaClassList.add(javaClass);
    }

    public void setProperties(String packagePrefix, String className) {
      this.packagePrefix = packagePrefix;
      this.className = className;
    }

    /**
     * Generates exceptionCaught() method which is invoked from other methods if
     * an SQLException is caught.
     */
    public void putExceptionCaught() throws IOException {
      writer.write("  /**\n");
      writer.write("   * Logs the given arguments on <code>FATAL</code> level.\n");
      writer.write("   * Invoked from other methods when an <code>SQLException</code> is thrown.\n");
      writer.write("   * @param sqle the caught <code>SQLException</code>\n");
      writer.write("   * @param methodName the name of method where the exception has been thrown\n");
      writer.write("   * @param methodArgs the names and values of method arguments where the exception has been thrown\n");
      writer.write("   * - (argument name, argument value) pairs\n");
      writer.write("   */\n");
      writer.write("  protected void exceptionCaught(SQLException sqle, String methodName, Map methodArgs) {\n");
      writer.write("    String id = \"[\" + (System.currentTimeMillis() / 1000) + \"] \";\n");
      writer.write("    StringBuffer message = new StringBuffer(\"\");\n");
      writer.write("    message.append(\"SQLException caught in \" + methodName + \" method\\n\");\n");
      writer.write("    message.append(\"Method arguments:\\n\");\n");
      writer.write("    for (Iterator iter = methodArgs.entrySet().iterator() ; iter.hasNext(); ) {\n");
      writer.write("      Map.Entry entry = (Map.Entry) iter.next();\n");
      writer.write("      String name  = String.valueOf(entry.getKey());\n");
      writer.write("      String value = stringValue(entry.getValue());\n");
      writer.write("      message.append(name + \"=\" + value + \"\\n\");\n");
      writer.write("    }\n");
      writer.write("    log.fatal(id + \"The caught SQLException:\", sqle);\n");
      writer.write("    while ((sqle = sqle.getNextException()) != null) {\n");
      writer.write("      log.fatal(id + \"Next SQLException:\", sqle);\n");
      writer.write("    }\n");
      writer.write("    log.fatal(id + message.toString());\n");
      writer.write("  }\n");
      writer.write("\n");
      writer.write("  /**\n");
      writer.write("   * Converts the given <code>Object</code> to its <code>String</code> representation.\n");
      writer.write("   * @param value the <code>Object</code> to convert\n");
      writer.write("   * @return the result of conversion\n");
      writer.write("   */\n");
      writer.write("  private String stringValue(Object value) {\n");
      writer.write("    if (value == null) {\n");
      writer.write("      return \"null\";\n");
      writer.write("    }\n");
      writer.write("    if (value instanceof String) {\n");
      writer.write("      return \"'\" + value + \"'\";\n");
      writer.write("    }\n");
      writer.write("    return String.valueOf(value);\n");
      writer.write("  }\n");
      writer.write("\n");
    }

  } // class MiscPutter

}