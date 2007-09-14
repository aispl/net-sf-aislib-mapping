package net.sf.aislib.tools.mapping.library.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.Fields;
import net.sf.aislib.tools.mapping.library.structure.JavaClass;
import net.sf.aislib.tools.mapping.library.structure.JavaMethod;
import net.sf.aislib.tools.mapping.library.structure.JavaParam;
import net.sf.aislib.tools.mapping.library.structure.Operations;
import net.sf.aislib.tools.mapping.library.structure.Select;
import net.sf.aislib.tools.mapping.library.structure.SqlQuery;
import net.sf.aislib.tools.mapping.library.structure.SqlTable;
import net.sf.aislib.tools.mapping.library.structure.Structure;

/**
 * Database test generator.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class DatabaseTestGenerator extends Generator {

  private Writer writer;
  private File   baseDir;

  protected String databaseClassName = "ApplicationDatabaseTest";
  protected boolean useManagerHelper = false;

  private MiscPutter miscPutter        = new MiscPutter();
  private List operationTestPutterList = new ArrayList();
  private List fieldTestPutterList     = new ArrayList();

  /**
   * Sets the name of the generated class.
   */
  public void setDatabaseClassName(String databaseClassName) {
    this.databaseClassName = databaseClassName;
  }

  /**
   * To use or not to use the ManagerHelper class. This is the question.
   */
  public void setUseManagerHelper(boolean umh) {
    useManagerHelper = umh;
  }

  private File initDirectory(File destinationDir) throws IOException {
    File dir = new File(destinationDir.getPath() + File.separator + "test");
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

  /**
   * The core method.
   */
  public void generate() throws IOException {

    if (database == null) {
      throw new IOException("Database object is null! (structure.xml not parsed?)");
    }
    baseDir = initDirectory(destinationDir);
    initWriter(databaseClassName);

    // Operations testing
    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size() ; i < size ; i++) {  // all structures
      Structure  structure   = (Structure) structureList.get(i);
      Operations operations  = structure.getOperations(); // get operations from structure
      if (operations == null) {continue;} // skip this structure if it has no operations defined
      JavaClass javaClass    = structure.getJavaClass();  // get javaClass from structure
      SqlTable  sqlTable     = structure.getSqlTable();  // get SqlTable from structure
      List      selectList   = operations.getSelectList();  // iterate thru selects
      for (Iterator selectIter = selectList.listIterator() ; selectIter.hasNext() ; ) {
        Select select = (Select) selectIter.next(); // create op.putter for each select
        OperationTestPutter operationTestPutter = new OperationTestPutter(writer);
        SqlQuery   sqlQuery   = select.getSqlQuery();  // get sqlQuery from select
        JavaMethod javaMethod = select.getJavaMethod();// get javaMethod from select
        operationTestPutter.setJavaClassName(javaClass.getName());
        operationTestPutter.setTableName(sqlTable.getName());
        operationTestPutter.setQueryWhere(sqlQuery.getWhere());
        operationTestPutter.setQueryOrder(sqlQuery.getOrderBy());
        operationTestPutter.setJavaMethodName(javaMethod.getName());
        List javaParamList = javaMethod.getJavaParamList(); // iterate thru javaParams
        for (Iterator paramIter = javaParamList.listIterator() ; paramIter.hasNext() ; ) {
          JavaParam javaParam = (JavaParam) paramIter.next();
          operationTestPutter.addJavaParamType(javaParam.getType());
        }
        operationTestPutterList.add(operationTestPutter); // add op.putter to the list
      }

    }

    // Field testing
    structureList = database.getStructureList();
    for (Iterator structureIter = structureList.listIterator() ; structureIter.hasNext() ; ) {
      Structure structure = (Structure) structureIter.next();
      JavaClass javaClass = structure.getJavaClass();
      SqlTable  sqlTable  = structure.getSqlTable();
      Fields    fields    = structure.getFields();
      List fieldList      = fields.getFieldList();
      for (Iterator fieldIter = fieldList.listIterator() ; fieldIter.hasNext() ; ) {
        Field field = (Field) fieldIter.next(); // allocate a new putter for each field, it's
        FieldTestPutter fieldTestPutter = new FieldTestPutter(writer); // not efficient...
        fieldTestPutter.setProperties(javaClass, sqlTable, field);
        fieldTestPutterList.add(fieldTestPutter); // add putter to the list
      }
    }

    // Now we flush all the source code to a file
    miscPutter.setWriter(writer);
    miscPutter.putHeader(packageName, useManagerHelper);  // package, imports
    miscPutter.putClassHeader(databaseClassName, useManagerHelper); // class beginning, setUp(), tearDown()
    for (int i = 0 ; i < operationTestPutterList.size() ; i++) {// operation tests
      ((OperationTestPutter) operationTestPutterList.get(i)).put();
    }
    for (int i = 0 ; i < fieldTestPutterList.size() ; i++) {// field tests
      ((FieldTestPutter) fieldTestPutterList.get(i)).put();
    }
    miscPutter.putClassEndBrace();  // and, at last, the final brace!
    closeWriter();
    log(databaseClassName + " class generated: ");
    log("  " + operationTestPutterList.size() + " operation-test method(s) generated");
    log("  " + fieldTestPutterList.size() + " field-test method(s) generated");
    log("  " + "UseManagerHelper = " + useManagerHelper);
  }


  //#############################  OPERATION TEST  ###########################
  /**
   *
   * @author Milosz Tylenda, AIS.PL
   */
  class OperationTestPutter {

    private StringBuffer str;
    private Writer writer;
    private String javaClassName;
    private String queryWhere;
    private String queryOrder;
    private String javaMethodName;
    private String tableName;
    private List javaParamTypes;

    public OperationTestPutter(Writer awriter) {
      str = new StringBuffer(512);
      javaParamTypes = new ArrayList();
      reset();
      setWriter(awriter);
    }

    private void reset() {
      str.delete(0, str.length());
      // tableName and javaClassName are per structure vars, others are per select.
      queryWhere = null;
      queryOrder = null;
      javaMethodName = "(NO java-method name SPECIFIED)";
      javaParamTypes.clear();
    }

    private void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      int i;

      str.append("  public void testSelect" + javaClassName + Utils.capitalize(javaMethodName));
      //str.append("  public void testSelect" + javaClassName + javaMethodName);
      str.append("() throws SQLException {\n");
      str.append("    Statement stmt = null;\n");
      str.append("    ResultSet rs = null;\n");
      str.append("    try {\n");
      str.append("      PreparedStatement pstmt = con.prepareStatement(\"select count(*) \" +\n");
      str.append("        \"from " + tableName);

      if ((queryWhere != null) && (queryWhere.length() > 0)) {
        str.append(" where " + queryWhere);
      }
      if ((queryOrder != null) && (queryOrder.length() > 0)) {
        str.append(" order by " + queryOrder);
      }
      str.append("\");\n");
      if (!javaParamTypes.isEmpty()) {
        str.append("      int counter = 1;\n");
        for (i = 0 ; i < javaParamTypes.size() ; i++) {
          str.append("      pstmt.setNull(counter++, " + Utils.getSQLType((String) javaParamTypes.get(i)) + ");\n");
          //str.append("      pstmt.setNull(counter++, " + ((String) javaParamTypes.get(i)) + ");\n");
        }
      }
      str.append("      rs = pstmt.executeQuery();\n");
      str.append("      rs.close();\n");
      str.append("    } catch (SQLException sqle) { \n");
      str.append("      fail(\"Can't execute select" + javaClassName + Utils.capitalize(javaMethodName) + " operation!\");\n");
      //str.append("      fail(\"Can't execute select" + javaClassName + javaMethodName + " operation!\");\n");
      str.append("    } finally { \n");
      str.append("      if (rs != null) rs.close();\n");
      str.append("      if (stmt != null) stmt.close();\n");
      str.append("    }\n");
      str.append("  }\n\n");

      writer.write(str.toString());
      reset();
    }

    public void setJavaClassName(String aJavaClassName) {
      javaClassName = aJavaClassName;
    }

    public void setJavaMethodName(String aJavaMethodName) {
      javaMethodName = aJavaMethodName;
    }

    public void setTableName(String aTableName) {
      tableName = aTableName;
    }

    public void setQueryWhere(String aQueryWhere) {
      queryWhere = aQueryWhere;
    }

    public void setQueryOrder(String aQueryOrder) {
      queryOrder = aQueryOrder;
    }

    public void addJavaParamType(String aJavaParamType) {
      javaParamTypes.add(aJavaParamType);
    }
  } // class OperationTestPutter


  //#############################  FIELD TEST  ###########################
  /**
   *
   * @author Milosz Tylenda, AIS.PL
   */
  class FieldTestPutter {

    private Writer writer;
    private JavaClass javaClass;
    private Field     field;
    private SqlTable  sqlTable;

    public FieldTestPutter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      writer.write("  public void test");
      writer.write(javaClass.getName());
      writer.write(Utils.capitalize(field.getJavaField().getName()));
      //writer.write(field.getJavaField().getName());
      writer.write("() throws SQLException {\n");
      writer.write("    Statement stmt = null;\n");
      writer.write("    ResultSet rs = null;\n");
      writer.write("    try {\n");
      writer.write("      stmt = con.createStatement();\n");
      writer.write("      rs = stmt.executeQuery(\"SELECT ");
      writer.write(field.getSqlField().getName());
      writer.write(" FROM ");
      writer.write(sqlTable.getName());
      writer.write("\");\n");
      writer.write("    } catch (SQLException sqle) {\n");
      writer.write("      fail(\"field " + field.getSqlField().getName() +
        " in " + sqlTable.getName() + " doesn't exist\");\n");
      writer.write("    } finally {\n");
      writer.write("      if (rs != null) rs.close();\n");
      writer.write("      if (stmt != null) stmt.close();\n");
      writer.write("    }\n");
      writer.write("  }\n");
      writer.write("\n");
    }

    public void setProperties(JavaClass javaClass, SqlTable sqlTable, Field field) {
      this.javaClass = javaClass;
      this.sqlTable = sqlTable;
      this.field = field;
    }
  } // class FieldTestPutter



  //#############################  MISC  ###########################
  /**
   * Puts some little things like <CODE>package</CODE> string and class end brace.
   * @author Milosz Tylenda, AIS.PL
   */
  class MiscPutter {

    private Writer writer;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void putHeader(String packagePrefix, boolean useManagerHelper) throws IOException {
      writer.write("package " + packagePrefix + ";\n\n");
      writer.write(" // THIS FILE HAS BEEN GENERATED AUTOMAGICALLY BY DatabaseTestGenerator\n");
      //writer.write(" // ON ");
      //writer.write( new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()) + "\n");
      writer.write(" // DO NOT EDIT!\n\n");
      writer.write("import java.sql.Connection;\n");
      writer.write("import java.sql.DriverManager;\n");
      writer.write("import java.sql.PreparedStatement;\n");
      writer.write("import java.sql.ResultSet;\n");
      writer.write("import java.sql.SQLException;\n");
      writer.write("import java.sql.Statement;\n");
      writer.write("import java.sql.Timestamp;\n\n");
      writer.write("import java.util.List;\n\n");
      writer.write("import junit.framework.TestCase;\n\n");
      if (useManagerHelper) {
        writer.write("import pl.aislib.fm.jdbc.Manager;\n\n");
      }
    }

    public void putClassHeader(String databaseClassName, boolean useManagerHelper) throws IOException {
      writer.write("\n");
      writer.write("\n");
      writer.write("public class " + databaseClassName + " extends TestCase {\n");
      writer.write("  public " + databaseClassName + "(String testName) {\n");
      writer.write("    super(testName);\n");
      writer.write("  }\n");
      writer.write("  \n");
      writer.write("  private Connection con;\n");
      if (!useManagerHelper) {
        writer.write("  public void setUp() throws Exception {\n");
        writer.write("    con = DriverManager.getConnection(\"jdbc:apache:commons:dbcp:test\");\n");
        writer.write("  }\n");
        writer.write("  \n");
      } else {
        writer.write("  protected Manager manager;\n\n");
        writer.write("  public void setUp() throws Exception {\n");
        writer.write("    manager = DatabaseTestHelper.createManager();\n");
        writer.write("    con     = manager.getConnection();\n");
        writer.write("  }\n");
        writer.write("  \n");
      }
      writer.write("  public void tearDown() throws Exception {\n");
      writer.write("    con.close();\n");
      writer.write("  }\n");
      writer.write("  \n");
    }

    public void putClassEndBrace() throws IOException {
      writer.write("}\n");
    }

  } // class MiscPutter

}
