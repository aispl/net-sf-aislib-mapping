package net.sf.aislib.tools.mapping.library.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.structure.Field;
import net.sf.aislib.tools.mapping.library.structure.Fields;
import net.sf.aislib.tools.mapping.library.structure.JavaClass;
import net.sf.aislib.tools.mapping.library.structure.JavaField;
import net.sf.aislib.tools.mapping.library.structure.Structure;

/**
 * <CODE>handlers</CODE> (describe/populate) generator.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class MapHelperGenerator extends Generator {

  private File baseDir;

  private MiscPutter miscPutter     = new MiscPutter();
  private MethodPutter methodPutter = new MethodPutter();
  private Writer writer;

  private File initDirectory(File destinationDir) throws IOException {
    File dir = new File(destinationDir.getPath() + File.separator + mapHandlersSubdir);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private void initWriter(String className) throws IOException {
    writer = new FileWriter(baseDir.getPath() + File.separator + className + "Handler.java");
  }

  private void closeWriter() throws IOException {
    writer.close();
  }

  public void generate() throws IOException {
    baseDir = initDirectory(destinationDir);
    List structureList = database.getStructureList();
    for (int i = 0, size = structureList.size() ; i < size ; i++) {  // all structures
      Structure structure = (Structure) structureList.get(i);
      JavaClass javaClass = structure.getJavaClass();
      Fields    fields    = structure.getFields();
      methodPutter.setProperties(javaClass, fields);
      initWriter(javaClass.getName());    // open file
      miscPutter.setWriter(writer);       // set writer on each putter
      methodPutter.setWriter(writer);

      // Now we flush all the source code to a file
      miscPutter.putHeader(packageName, javaClass.getName());
      methodPutter.put();
      miscPutter.putClassEndBrace();
      closeWriter();
    }
    log(structureList.size() + " " + mapHandlersSubpackage + ".XHandler class(es) generated");
  }


  //#############################  POPULATE/DESCRIBE  ###########################
  /**
   * Puts <code>describe/populate/carefulPopulate</code> methods.
   * @author Milosz Tylenda, AIS.PL
   */
  class MethodPutter {

    private Writer writer;
    private JavaClass javaClass;
    private Fields    fields;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void put() throws IOException {
      String className = javaClass.getName();
      List fieldList = fields.getFieldList();
      writer.write("\n");
      writer.write("  public static Map describe(" + className + " object) {\n");
      writer.write("    Map result = new HashMap(" + fieldList.size() + ");\n");
      for (Iterator fieldIter = fieldList.listIterator() ; fieldIter.hasNext() ; ) {
        Field field         = (Field) fieldIter.next();
        JavaField javaField = field.getJavaField();
        String name         = javaField.getName();
        String capName      = Utils.capitalize(name);
        writer.write("    if (object.get" + capName + "() != null) {\n");
        writer.write("      result.put(\"" + name + "\", object.get" + capName + "());\n");
        writer.write("    }\n");
      }
      writer.write("    return result;\n");
      writer.write("  }\n");

      writer.write("  public static void populate(" + className + " object, Map properties) {\n");
      for (Iterator fieldIter = fieldList.listIterator() ; fieldIter.hasNext() ; ) {
        Field field         = (Field) fieldIter.next();
        JavaField javaField = field.getJavaField();
        String name         = javaField.getName();
        String type         = javaField.getType();
        String capName      = Utils.capitalize(name);
        writer.write("    if (properties.containsKey(\"" + name + "\")) {\n");
        writer.write("      object.set" + capName + "((" + type + ") properties.get(\"" + name + "\"));\n");
        writer.write("    }\n");
      }
      writer.write("  }\n");

      writer.write("  public static void carefulPopulate(" + className + " object, Map properties) {\n");
      for (Iterator fieldIter = fieldList.listIterator() ; fieldIter.hasNext() ; ) {
        Field field         = (Field) fieldIter.next();
        JavaField javaField = field.getJavaField();
        String name         = javaField.getName();
        String type         = javaField.getType();
        String capName      = Utils.capitalize(name);
        writer.write("    if ((properties.containsKey(\"" + name + "\"))\n");
        writer.write("      && (properties.get(\"" + name + "\") instanceof ");
        writer.write(type + ")) {\n");
        writer.write("      object.set" + capName + "((" + type + ") properties.get(\"" + name + "\"));\n");
        writer.write("    }\n");
      }
      writer.write("  }\n");
    }

    public void setProperties(JavaClass javaClass, Fields fields) {
      this.javaClass = javaClass;
      this.fields = fields;
    }
  } // class MethodPutter


  //#############################  MISC  ###########################
  /**
   * Puts some little things like a <CODE>package</CODE> string and a class ending brace.
   * @author Milosz Tylenda, AIS.PL
   */
  class MiscPutter {

    private Writer writer;

    public void setWriter(Writer awriter) {
      writer = awriter;
    }

    public void putHeader(String packagePrefix, String className) throws IOException {
      writer.write("package " + packagePrefix + "." + mapHandlersSubpackage + ";\n\n");
      writer.write("// THIS FILE HAS BEEN GENERATED AUTOMAGICALLY. DO NOT EDIT!\n\n");
      writer.write("import java.math.BigDecimal;\n\n");
      writer.write("import java.sql.Array;\n");
      writer.write("import java.sql.Blob;\n");
      writer.write("import java.sql.Timestamp;\n\n");
      writer.write("import java.util.Map;\n");
      writer.write("import java.util.HashMap;\n\n");
      writer.write("import " + packagePrefix + "." + objectsSubpackage + "." + className + ";\n\n");
      writer.write("public class " + className + "Handler {\n");
    }

    public void putClassEndBrace() throws IOException {
      writer.write("}\n");
    }
  } // class MiscPutter


}
