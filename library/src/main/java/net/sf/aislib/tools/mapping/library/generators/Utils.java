package net.sf.aislib.tools.mapping.library.generators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.JavaField;
import net.sf.aislib.tools.mapping.library.structure.JavaParam;
import net.sf.aislib.tools.mapping.library.structure.SqlField;


/**
 * Some static methods used by generators.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class Utils {

  private static final int MAX_STRING_LENGTH = 40;

  private static final Map typeMapping = new HashMap();

  static {
    typeMapping.put("BigDecimal", "BigDecimal");
    typeMapping.put("Double",     "Double");
    typeMapping.put("Float",      "Float");
    typeMapping.put("Integer",    "Int");
    typeMapping.put("Long",       "Long");
    typeMapping.put("String",     "String");
    typeMapping.put("Timestamp",  "Timestamp");
    typeMapping.put("Array",      "Array");
    typeMapping.put("Blob",      "Blob");
  }

  /**
   * Returns the given String splitted in terms of Java language (plus signs).
   * Each chunk has maximum length defined in {@link #MAX_STRING_LENGTH}.
   */
  public static String equalLengthSplit(String s) {
    if (s.length() == 0) {
      return "\"\"";
    }
    StringBuffer result = new StringBuffer(512);
    int sLength = s.length();
    String subString;
    String plus;
    for (int i = 0 ; i < sLength ; i += MAX_STRING_LENGTH) {
      if ((i + MAX_STRING_LENGTH) >= sLength) {
        subString = s.substring(i);
        plus = "";
      } else {
        subString = s.substring(i, i + MAX_STRING_LENGTH);
        plus = " + ";
      }
      result.append("\"");
      result.append(subString);
      result.append("\"");
      result.append(plus);
    }
    return result.toString();
  }

  /**
   * Returns the given string with first letter changed to uppercase.
   */
  public static String capitalize(String s) {
    if ((s == null) || (s.equals(""))) {
      return "";
    }
    StringBuffer sb = new StringBuffer(s);
    sb.replace(0, 1, s.substring(0, 1).toUpperCase());
    return sb.toString();
  }

  /**
   * Provides mapping between Java and SQL data types.
   */
  public static String getSQLType(String arg) {
    if (arg.equals("Integer")) {
      return "java.sql.Types.INTEGER";
    } else if (arg.equals("Long")) {
      return "java.sql.Types.BIGINT";
    } else if (arg.equals("Double")) {
      return "java.sql.Types.DOUBLE";
    } else if (arg.equals("Float")) {
      return "java.sql.Types.FLOAT";
    } else if (arg.equals("String")) {
      return "java.sql.Types.VARCHAR";
    } else if (arg.equals("Timestamp")) {
      return "java.sql.Types.TIMESTAMP";
    } else if (arg.equals("BigDecimal")) {
      return "java.sql.Types.NUMERIC";
    } else if (arg.equals("Array")) {
      return "java.sql.Types.ARRAY";
    } else if (arg.equals("Blob")) {
      return "java.sql.Types.BLOB";
    } else {
      throw new RuntimeException("Unknown type: " + arg);
    }
  }

  /**
   *
   */
  public static boolean isBaseType(String typeName) {
    if (typeName.equals("String") ||
        typeName.equals("Timestamp") ||
        typeName.equals("BigDecimal") ||
        typeName.equals("Blob") ||
        typeName.equals("Array")) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Used to construct <code>PreparedStatement.setX</code> methods.
   */
  public static String mapTypeToMethod(String arg) {
    String mapMethod = (String) typeMapping.get(arg);
    if (mapMethod == null) {
      throw new RuntimeException("unknown type: " + arg);
    }
    return mapMethod;
  }

  /**
   * Provides <code>Integer -&gt; intValue()</code> kind of mapping.
   */
  public static String castMethod(String arg) {
    if (arg.equals("Integer")) {
      return ".intValue()";
    } else if (arg.equals("Long")) {
      return ".longValue()";
    } else if (arg.equals("Double")) {
      return ".doubleValue()";
    } else if (arg.equals("Float")) {
      return ".floatValue()";
    } else {
      return "";
    }
  }

  /**
   * Calls {@link #formatSqlListWithTablePrefix} with empty String as <code>tablePrefix</code>,
   * then splits the String (i.e. produces one String with lots of plus signs and quotes),
   * so Jalopy will be able to break long lines.
   */
  public static String formatSqlList(List fieldList, boolean onlyNames) {
    String longString = formatSqlListWithTablePrefix(fieldList, "", onlyNames);
    return equalLengthSplit(longString);
  }

  /**
   * If <code>useTableName</code> is set
   * calls {@link #formatSqlListWithTablePrefix} with <code>tableName + "."</code> as <code>tablePrefix</code>.
   * Else
   * calls {@link #formatSqlListWithTablePrefix} with empty String as <code>tablePrefix</code>.
   */
  public static String formatSqlList2(List fieldList, boolean useTableName, String tableName, boolean onlyNames) {
    if (useTableName) {
      return formatSqlListWithTablePrefix(fieldList, tableName + ".", onlyNames);
    }
    return formatSqlListWithTablePrefix(fieldList, "", onlyNames);
  }

  /**
   * Formats arguments as column list for SELECT statement.
   * @param fieldList the List of Field objects
   * @param tablePrefix the prefix to append before column names
   */
  private static String formatSqlListWithTablePrefix(List fieldList, String tablePrefix, boolean onlyNames) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      String name = sqlField.getName();
      if (!onlyNames && sqlField.hasRead()) {
        name = sqlField.getRead().getFunction();
      }
      str.append(tablePrefix);
      str.append(name);
      if (i < size - 1) {
        str.append(", ");
      }
    }
    return str.toString();
  }

  /**
   *
   */
  public static String formatSqlListForWhere(List fieldList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      String name       = sqlField.getName();
      str.append(name);
      if (sqlField.hasWrite()) {
        str.append(" = " + sqlField.getWrite().getFunction() + " ");
      } else {
        str.append(" = ? ");
      }
      if (i < size - 1) {
        str.append("and ");
      }
    }
    return str.toString();
  }

  /**
   * Formats arguments as column list for UPDATE statement.
   * The returned String is splitted.
   * @param fieldList the List of Field objects
   */
  public static String formatSqlListForUpdate(List fieldList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field = (Field) fieldList.get(i);
      SqlField sqlField = field.getSqlField();
      String name       = sqlField.getName();
      str.append(name);
      if (sqlField.hasWrite()) {
        str.append(" = " + sqlField.getWrite().getFunction() + " ");
      } else {
        str.append(" = ? ");
      }
      if (i < size - 1) {
        str.append(", ");
      }
    }
    return equalLengthSplit(str.toString());
  }

  /**
   * Formats arguments for Java method declaration (Fields).
   * @param fieldList the List of Field objects
   */
  public static String formatArgList(List fieldList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = fieldList.size() ; i < size ; i++) {
      Field field         = (Field) fieldList.get(i);
      JavaField javaField = field.getJavaField();
      String name         = javaField.getName();
      String type         = javaField.getType();
      str.append(type + " " + name);
      if (i < size - 1) {
        str.append(", ");
      }
    }
    return str.toString();
  }

  /**
   * Formats arguments for Java method declaration (JavaParams).
   * @param javaParamList the List of JavaParam objects
   */
  public static String formatMethodArgs(List javaParamList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      str.append(javaParam.getType() + " " + javaParam.getName());
      str.append(", ");
    }
    return str.toString();
  }

  /**
   * Formats arguments for Java method declaration without ending comma (JavaParams).
   * @param javaParamList the List of JavaParam objects
   */
  public static String formatMethodArgsWithoutComma(List javaParamList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      str.append( " " + javaParam.getType() + " " + javaParam.getName());
      if (i < size - 1) {
        str.append(",");
      } else {
        str.append("\n");
      }
    }
    return str.toString();
  }

  /**
   * Formats arguments for Java method invocation (JavaParams).
   * Only param names, no types.
   * @param javaParamList the List of JavaParam objects
   */
  public static String formatMethodParams(List javaParamList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      str.append(javaParam.getName() + ", ");
    }
    return str.toString();
  }

  /**
   * Formats arguments for JavaDoc (JavaParams).
   * @param javaParamList the List of JavaParam objects
   */
  public static String formatMethodArgsForJavaDoc(List javaParamList) {
    StringBuffer str = new StringBuffer(128);
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      str.append("   * @param " + javaParam.getName() + " the instance of ");
      str.append("<code>" + javaParam.getType() + "</code>");
      str.append("\n");
    }
    return str.toString();
  }

  /**
   * Generates PreparedStatement.setX related code.
   * @param javaParamList the List of JavaParam objects
   */
  public static String generatePstmtSets(List javaParamList) {
    StringBuffer str = new StringBuffer(256);
    for (int i = 0, size = javaParamList.size() ; i < size ; i++) {
      JavaParam javaParam = (JavaParam) javaParamList.get(i);
      generatePstmtSet(str, javaParam, i + 1);
    }
    return str.toString();
  }

  /**
   * Generates one PreparedStatement.setX piece of code.
   */
  public static void generatePstmtSet(StringBuffer str, JavaParam javaParam, int position) {
    if (!isBaseType(javaParam.getType())) {
      str.append("        pstmt.set" + Utils.mapTypeToMethod(javaParam.getType()));
      str.append("(" + position + ", " + javaParam.getName() + ");\n");
    } else {
      str.append("        if (" + javaParam.getName() + " != null) {\n");
      str.append("          pstmt.set" + Utils.mapTypeToMethod(javaParam.getType()));
      str.append("(" + position + ", " + javaParam.getName());
      str.append(Utils.castMethod(javaParam.getType()));
      str.append(");\n");
      str.append("        } else {\n");
      str.append("          pstmt.setNull(" + position + ", ");
      str.append(Utils.getSQLType(javaParam.getType()));
      str.append(");\n");
      str.append("        }\n");
    }
  }

  /**
   * Returns a methodName created by concatenating and capitalizing the given arguments.
   */
  public static String createMethodName(String prefix, String className, String suffix) {
    if (prefix == null) {
      prefix = "";
    }
    if (className == null) {
      className = "";
    }
    if (suffix == null) {
      suffix = "";
    }
    return prefix + className + capitalize(suffix);
  }

  /**
   * Returns a getter-method call for the given name.
   */
  public static String getter(String name) {
    return "get" + capitalize(name) + "()";
  }

  /**
   * Escapes all HTML reserved chars.
   *
   * @param value the String to protect
   * @return a value of type 'String'
   */
  public static String protectHTML(String value) {
    if (value == null) {
      return "";
    }
    boolean hasBadChar = false;
    for (int i = 0, length = value.length(); i < length; i++) {
      switch (value.charAt(i)) {
        case '"':
        case '<':
        case '>':
        case '&':
          hasBadChar = true;
          break;
      }
      if (hasBadChar) {
        break;
      }
    }
    if (!hasBadChar) {
      return value;
    }
    StringBuffer sBuf = new StringBuffer();
    for (int i = 0, length = value.length(); i < length; i++) {
      char ch = value.charAt(i);
      switch (ch) {
        case '"':
          sBuf.append("&quot;");
          break;
        case '<':
          sBuf.append("&lt;");
          break;
        case '>':
          sBuf.append("&gt;");
          break;
        case '&':
          sBuf.append("&amp;");
          break;
        default:
          sBuf.append(ch);
      }
    }
    return sBuf.toString();
  }

  /**
   * Creates a fragment containing initialization of PreparedStatement.
   *
   * @param query to pass as a method argument
   * @return a fragment containing initialization of PreparedStatement
   */
  public static String createPreparedStatement(String query) {
    String formattedQuery = equalLengthSplit(query);
    if (containsRawParam(query)) {
      return "      EnhancedStatement pstmt = EnhancedStatement.getInstance(con, " + formattedQuery + ");\n";
    } else {
      return "      PreparedStatement pstmt = con.prepareStatement(" + formattedQuery + ");\n";
    }
  }

  /**
   * Returns true if the given String contains '??' marker.
   *
   * @param query the query to check
   * @return true if the given String contains '??' marker
   */
  public static boolean containsRawParam(String query) {
    return ((query != null) && (query.indexOf("??") != -1));
  }

  public static String generateArgs(List args, Integer startIndex) {

    StringBuffer sb = new StringBuffer("");

    int i = 1;
    if (startIndex != null) {
      i = startIndex.intValue();
    }
    for (Iterator iter = args.iterator(); iter.hasNext();) {
      JavaField javaField = (JavaField) iter.next();
      sb.append(javaField.getType() + " ");
      sb.append(javaField.getName() + "_" + i++ + ", ");
    }
    return sb.toString();
  }

  public static String getArgs(List args, Integer startIndex) {
    StringBuffer sb = new StringBuffer("");

    int i = 1;
    if (startIndex != null) {
      i = startIndex.intValue();
    }
    for (Iterator iter = args.iterator(); iter.hasNext();) {
      JavaField javaField = (JavaField) iter.next();
      sb.append(javaField.getName() + "_" + i++ + ", ");
    }
    return sb.toString();
  }

  public static String generateArgsWithoutLastComa(List args, Integer startIndex) {
    StringBuffer sb = new StringBuffer("");
    String result;
    int i = 1;
    if (startIndex != null) {
      i = startIndex.intValue();
    }
    for (Iterator iter = args.iterator(); iter.hasNext();) {
      JavaField javaField = (JavaField) iter.next();
      sb.append(javaField.getType() + " ");
      sb.append(javaField.getName() + "_" + i++ + ", ");
    }

    result = sb.toString();
    if (result.length() > 0) {
      return result.substring(0, result.length() - 2);
    }
    return result;
  }

  public static String getArgsWithoutLastComa(List args, Integer startIndex) {
    StringBuffer sb = new StringBuffer("");
    String result;
    int i = 1;
    if (startIndex != null) {
      i = startIndex.intValue();
    }
    for (Iterator iter = args.iterator(); iter.hasNext();) {
      JavaField javaField = (JavaField) iter.next();
      sb.append(javaField.getName() + "_" + i++ + ", ");
    }

    result = sb.toString();
    if (result.length() > 0) {
      return result.substring(0, result.length() - 2);
    }
    return result;
    }

}
