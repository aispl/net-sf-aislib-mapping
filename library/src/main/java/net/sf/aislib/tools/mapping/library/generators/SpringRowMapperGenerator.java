package net.sf.aislib.tools.mapping.library.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.Fields;
import net.sf.aislib.tools.mapping.library.structure.JavaClass;
import net.sf.aislib.tools.mapping.library.structure.JavaField;
import net.sf.aislib.tools.mapping.library.structure.SqlField;
import net.sf.aislib.tools.mapping.library.structure.Structure;


/**
 * Spring Row Mapper Generator
 * @author <a href="mailto:chmielu@ais.pl">Pawel Chmielewski</a>, AIS.PL
 * @version $Revision: $
 */
public class SpringRowMapperGenerator extends Generator {

  private File baseDir;
  private Writer writer;

  private File initDirectory(File destinationDir) {
    File dir = new File(destinationDir.getPath() + File.separator + rowMappersSubdir);
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
    RowMapPutter rowMapPutter = new RowMapPutter();

    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size(); i < size; i++) { // all structures
      Structure structure = (Structure) structureList.get(i);
      JavaClass javaClass = structure.getJavaClass();
      Fields fields = structure.getFields();

      initWriter(javaClass.getName() + "RowMapper.java"); // open file
      miscPutter.setWriter(writer);
      miscPutter.setProperties(packageName, javaClass);
      miscPutter.putHeader(); // to file

      rowMapPutter.setWriter(writer);
      rowMapPutter.setProperties(javaClass, fields);
      rowMapPutter.put(); // to file
      miscPutter.putClassEndBrace(); // to file
      closeWriter();
    }
    log(structureList.size() + " " + dbHandlersSubpackage + ".XRowMapper classes generated");
  }


  class RowMapPutter {
    private Writer writer;
    private JavaClass javaClass;
    private Fields fields;

    public void setWriter(Writer writer) {
      this.writer = writer;
    }
    public void setProperties(JavaClass javaClass, Fields fields) {
      this.javaClass = javaClass;
      this.fields = fields;
    }
    public void put() throws IOException {

      List fieldList = fields.getFieldList();
      writer.write("  /* (non-Javadoc)\n");
      if (useGenerics) {
        writer.write("   * @see org.springframework.jdbc.core.simple.ParameterizedRowMapper#mapRow(java.sql.ResultSet, int)\n");
        writer.write("   */\n");
        writer.write("  public " + javaClass.getName() + " mapRow(ResultSet rs, int rowNum) throws SQLException {\n");
      } else {
        writer.write("   * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)\n");
        writer.write("   */\n");
        writer.write("  public Object mapRow(ResultSet rs, int rowNum) throws SQLException {\n");
      }
      writer.write("    " + javaClass.getName() + " result = new " + javaClass.getName() + "();\n");
      for (int i = 0, size = fieldList.size(); i < size; i++) {
        Field field = (Field) fieldList.get(i);
        JavaField javaField = field.getJavaField();
        SqlField sqlField = field.getSqlField();
        if (sqlField.isClobType()) {
          putClobFieldGetting(field);
          continue;
        }
        String cappedFieldName = Utils.capitalize(javaField.getName());
        String fieldType = javaField.getType();
        writer.write("    result.set" + cappedFieldName + "(");
        if (Utils.isBaseType(fieldType)) {
          writer.write("new " + fieldType + "(rs.get" + Utils.mapTypeToMethod(fieldType) + "(\"" + sqlField.getName() + "\")));\n");
          writer.write("    if (rs.wasNull()) {\n");
          writer.write("      result.set" + cappedFieldName + "(null);\n");
          writer.write("    }\n");
        } else {
          writer.write("rs.get" + Utils.mapTypeToMethod(fieldType) + "(\"" + sqlField.getName() + "\"));\n");
        }
      }
      writer.write("    return result;\n");
      writer.write("  }\n\n");
    }

    /**
     * Puts Clob getting code. It is assumed than javaField is of String type.
     * That is, the application sees field mapped to CLOB as String.
     */
    private void putClobFieldGetting(Field field) throws IOException {
      JavaField javaField = field.getJavaField();
      SqlField sqlField = field.getSqlField();
      String cappedFieldName = Utils.capitalize(javaField.getName());
      writer.write("    java.sql.Clob clob" + cappedFieldName + " = rs.getClob(\"" + sqlField.getName() + "\");\n");
      writer.write("    if (clob" + cappedFieldName + " != null) {\n");
      writer.write("      result.set" + cappedFieldName + "(clob" + cappedFieldName + ".getSubString(1L, (int) clob"
        + cappedFieldName + ".length()));\n");
      writer.write("    } else {\n");
      writer.write("      result.set" + cappedFieldName + "(null);\n");
      writer.write("    }\n");
    }

  }

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
      writer.write("package " + packagePrefix + "." + rowMappersSubpackage + ";\n\n");
      writer.write("// THIS FILE HAS BEEN GENERATED AUTOMAGICALLY. DO NOT EDIT!\n\n");
      writer.write("import java.sql.SQLException;\n");
      writer.write("import java.sql.ResultSet;\n");

      writer.write("import " + packagePrefix + "." + objectsSubpackage + "." + className + ";\n\n");

      if (useGenerics) {
        writer.write("import org.springframework.jdbc.core.simple.ParameterizedRowMapper;\n\n");
      } else {
        writer.write("import org.springframework.jdbc.core.RowMapper;\n\n");
      }

      writer.write("/**\n");
      writer.write(" * <code>" + className + "</code> row mapper.\n");
      writer.write(" * @author SpringRowMapperGenerator\n");
      writer.write(" */\n");
      if (useGenerics) {
        writer.write("public class " + className + "RowMapper implements ParameterizedRowMapper<" + className + "> {\n\n");
      } else {
        writer.write("public class " + className + "RowMapper implements RowMapper {\n\n");
      }
    }

    public void putClassEndBrace() throws IOException {
      writer.write("}\n");
    }

  } // class MiscPutter

}
