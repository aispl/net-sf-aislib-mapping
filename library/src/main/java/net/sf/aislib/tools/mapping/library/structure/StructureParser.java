package net.sf.aislib.tools.mapping.library.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Structure.xml parser.
 * Probably this should be rewritten using Digester to simplify the things.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public class StructureParser {

  private File mappingFile;

  public StructureParser(File mappingFile) {
    this.mappingFile = mappingFile;
  }

  public Database createDatabase() throws Exception {
    XMLReader reader = createXMLReader();
    ContentHandler handler = new MyHandler();
    reader.setContentHandler(handler);
    reader.setEntityResolver(new MyEntityResolver());
    MyErrorHandler myErrorHandler = new MyErrorHandler();
    reader.setErrorHandler(myErrorHandler);
    InputSource source = new InputSource(new FileInputStream(mappingFile));
    source.setSystemId(mappingFile.toURL().toString());
    reader.setEntityResolver(new MyEntityResolver());
    reader.parse(source);
    if (myErrorHandler.hasErrors()) {
      throw new Exception("Syntax (t)error in " + mappingFile.toURL());
    }
    Database database = ((MyHandler) handler).database;
    new DatabaseChecker().check(database);

    return database;
  }

  /**
   *
   */
  private XMLReader createXMLReader() throws SAXException {
    SAXParserFactory factory = null;
    SAXParser         parser = null;
    XMLReader         reader = null;
    try {
      factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(true);
      factory.setFeature("http://xml.org/sax/features/validation", true);
      parser  = factory.newSAXParser();
    } catch (ParserConfigurationException pce) {
      throw new SAXException(pce.getMessage());
    }
    reader = parser.getXMLReader();
    return reader;
  }


  private class MyHandler extends DefaultHandler {

    public  Database   database;
    private Structure  structure;
    private Field      field;
    private Read       read;
    private Write      write;
    private Fields     fields;
    private JavaClass  javaClass;
    private JavaField  javaField;
    private JavaMethod javaMethod;
    private JavaParam  javaParam;
    private Operations operations;
    private Aggregate  aggregate;
    private Call       call;
    private Count      count;
    private Delete     deletee;
    private Select     select;
    private Update     update;
    private SqlField   sqlField;
    private SqlQuery   sqlQuery;
    private SqlTable   sqlTable;
    private CallParams callParams;
    private CallParam  callParam;
    private int        currentOperation; // This holds element type in operations.
           // It is required because sql-query, java-field and java-method may occur in more than one element.
    private final int  DELETE    = 1;
    private final int  SELECT    = 2;
    private final int  UPDATE    = 3;
    private final int  COUNT     = 4;
    private final int  AGGREGATE = 5;
    private final int  CALL      = 6;
    private final int  READ      = 7;
    private final int  WRITE     = 8;

    public void startElement(String s1, String s2, String s3, Attributes attrs)
      throws SAXException {
      if (s2.equals("database")) {
        database = new Database();
        return;
      }
      if (s2.equals("structure")) {
        structure = new Structure(attrs.getValue("name"));
        database.addStructure(structure);
        return;
      }
      if (s2.equals("java-class")) {
        javaClass = new JavaClass(attrs.getValue("name"), attrs.getValue("propertyChangeSupport").equals("true"));
        structure.setJavaClass(javaClass);
        return;
      }
      if (s2.equals("fields")) {
        fields = new Fields();
        structure.setFields(fields);
        return;
      }
      if (s2.equals("sql-table")) {
        sqlTable = new SqlTable(
          attrs.getValue("name"),
          attrs.getValue("insert").equals("true"),
          attrs.getValue("select").equals("true"),
          attrs.getValue("delete").equals("true"),
          attrs.getValue("update").equals("true")
            );
        structure.setSqlTable(sqlTable);
        return;
      }
      if (s2.equals("operations")) {
        operations = new Operations();
        structure.setOperations(operations);
        return;
      }
      if (s2.equals("field")) {
        field = new Field(
          attrs.getValue("name"),
          attrs.getValue("notNull").equals("true"),
          attrs.getValue("primaryKey").equals("true")
            );
        fields.addField(field);
        return;
      }
      if (s2.equals("sql-field")) {
        sqlField = new SqlField(
          attrs.getValue("name"),
          attrs.getValue("type"),
          attrs.getValue("default"),
          attrs.getValue("onInsert"),
          attrs.getValue("autoGenerated")
            );
        if(fields.hasAutoGenerated() && sqlField.isAutoGenerated()){
          throw new SAXException(" You can define  'autoGenerated' attribute only for the one column in table. Update "
            + structure.getName() + " mapping.");
        }
        if(sqlField.isAutoGenerated() && sqlField.useOnInsert() == true) {
          throw new SAXException("Attribute autoGenerated=\"true\" requires onInsert=\"omit\". Update "
            + structure.getName() + "." + sqlField.getName() + " field mapping.");
        }
        field.setSqlField(sqlField);
        return;
      }
      if (s2.equals("read")) {
        read = new Read(
          attrs.getValue("function")
          );
        sqlField.setRead(read);
        currentOperation = READ;
        return;
      }
      if (s2.equals("write")) {
        write = new Write(
          attrs.getValue("function")
          );
        sqlField.setWrite(write);
        currentOperation = WRITE;
        return;
      }
      if (s2.equals("java-field")) {
        boolean sensitive = false;
        if (attrs.getValue("sensitive") != null) {
          sensitive = Boolean.valueOf(attrs.getValue("sensitive")).booleanValue();
        }
        if (attrs.getValue("toString") != null) {
          throw new SAXException("Attribute 'toString' is deprecated, use 'sensitive' attribute and v0.7 of DTD instead");
        }

        javaField = new JavaField(
          attrs.getValue("name"),
          attrs.getValue("type"),
          attrs.getValue("default"),
          sensitive
        );
        if (currentOperation == READ) {
          read.addJavaField(javaField);
        } else if (currentOperation == WRITE) {
          write.addJavaField(javaField);
        } else {
          field.setJavaField(javaField);
        }
        return;
      }
      if (s2.equals("aggregate")) {
        aggregate = new Aggregate(attrs.getValue("multipleRows").equals("true"));
        operations.addAggregate(aggregate);
        currentOperation = AGGREGATE;
        return;
      }
      if (s2.equals("call")) {
        call = new Call();
        operations.addCall(call);
        currentOperation = CALL;
        return;
      }
      if (s2.equals("count")) {
        count = new Count();
        operations.addCount(count);
        currentOperation = COUNT;
        return;
      }
      if (s2.equals("delete")) {
        deletee = new Delete();
        operations.addDelete(deletee);
        currentOperation = DELETE;
        return;
      }
      if (s2.equals("select")) {
        select = new Select(attrs.getValue("multipleRows").equals("true"));
        operations.addSelect(select);
        currentOperation = SELECT;
        return;
      }
      if (s2.equals("update")) {
        update = new Update();
        operations.addUpdate(update);
        currentOperation = UPDATE;
        return;
      }
      if (s2.equals("sql-query")) {
        sqlQuery = new SqlQuery(
          attrs.getValue("where"),
          attrs.getValue("order-by"),
          attrs.getValue("distinct").equals("true"),
          attrs.getValue("set"),
          attrs.getValue("columns"),
          attrs.getValue("from"),
          attrs.getValue("body"),
          attrs.getValue("group-by"),
          attrs.getValue("having"),
          attrs.getValue("other")
            );
        if (currentOperation == AGGREGATE) {
          aggregate.setSqlQuery(sqlQuery);
          return;
        }
        if (currentOperation == CALL) {
          call.setSqlQuery(sqlQuery);
          return;
        }
        if (currentOperation == COUNT) {
          count.setSqlQuery(sqlQuery);
          return;
        }
        if (currentOperation == DELETE) {
          deletee.setSqlQuery(sqlQuery);
          return;
        }
        if (currentOperation == SELECT) {
          select.setSqlQuery(sqlQuery);
          return;
        }
        if (currentOperation == UPDATE) {
          update.setSqlQuery(sqlQuery);
          return;
        }
        System.out.println("Parsing ERROR: orphaned sql-query element.");
        return;
      }
      if (s2.equals("java-method")) {
        javaMethod = new JavaMethod(attrs.getValue("name"), attrs.getValue("returnType"));
        if (currentOperation == AGGREGATE) {
          aggregate.setJavaMethod(javaMethod);
          return;
        }
        if (currentOperation == CALL) {
          call.setJavaMethod(javaMethod);
          return;
        }
        if (currentOperation == COUNT) {
          count.setJavaMethod(javaMethod);
          return;
        }
        if (currentOperation == DELETE) {
          deletee.setJavaMethod(javaMethod);
          return;
        }
        if (currentOperation == SELECT) {
          select.setJavaMethod(javaMethod);
          return;
        }
        if (currentOperation == UPDATE) {
          update.setJavaMethod(javaMethod);
          return;
        }
        System.out.println("Parsing ERROR: orphaned java-method element.");
        return;
      }
      if (s2.equals("java-param")) {
        boolean sensitive =  attrs.getValue("sensitive") == null ? false : Boolean.valueOf(attrs.getValue("sensitive")).booleanValue();
        javaParam = new JavaParam(attrs.getValue("name"), attrs.getValue("type"), sensitive);
        javaMethod.addJavaParam(javaParam);
        return;
      }
      if (s2.equals("call-params")) {
        callParams = new CallParams();
        call.setCallParams(callParams);
        return;
      }
      if (s2.equals("call-param")) {
        callParam = new CallParam(
            attrs.getValue("accessType"), attrs.getValue("fieldRef"),
            attrs.getValue("methodRef"),   attrs.getValue("type"));
        callParams.addCallParam(callParam);
        return;
      }
    }

    public void endElement(String s1, String s2, String s3)
    throws SAXException {
      if (s2.equals("read")) {
        currentOperation = 0;
        return;
      }
      if (s2.equals("write")) {
        currentOperation = 0;
        return;
      }

    }

  }

  /**
   *
   */
  public class MyErrorHandler implements ErrorHandler {

    private boolean errors = false;

    public boolean hasErrors() {
      return errors;
    }

    public MyErrorHandler() {
    }

    public void error(SAXParseException spe) throws SAXParseException {
      format(spe);
    }

    public void fatalError(SAXParseException spe) throws SAXParseException {
      format(spe);
    }

    public void warning(SAXParseException spe) throws SAXParseException {
      format(spe);
    }

    private void format(SAXParseException spe) throws SAXParseException {
      System.out.println("error: L: " + spe.getLineNumber() + " C: " + spe.getColumnNumber() + ", " + spe.getMessage());
      errors = true;
    }

  }

  /**
   * EntityResolver implementation just to avoid fetching DTD via network.
   */
  private class MyEntityResolver implements EntityResolver {

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      if ((publicId == null) || (systemId == null)) {
        return null;
      }
      if (publicId.equals("-//AIS.PL//DTD Mapping Description 0.4//EN") &&
          systemId.equals("http://www.ais.pl/dtds/mapping_0_4.dtd")) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("net/sf/aislib/tools/mapping/library/resources/mapping_0_4.dtd");
        if (stream != null) {
          InputSource result = new InputSource(stream);
          result.setSystemId(getClass().getClassLoader().getResource("net/sf/aislib/tools/mapping/library/resources/mapping_0_4.dtd").toString());
          return result;
        }
      }
      if (publicId.equals("-//AIS.PL//DTD Mapping Description 0.5//EN") &&
          systemId.equals("http://www.ais.pl/dtds/mapping_0_5.dtd")) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("net/sf/aislib/tools/mapping/library/resources/mapping_0_5.dtd");
        if (stream != null) {
          InputSource result = new InputSource(stream);
          result.setSystemId(getClass().getClassLoader().getResource("net/sf/aislib/tools/mapping/library/resources/mapping_0_5.dtd").toString());
          return result;
        }
      }
      if (publicId.equals("-//AIS.PL//DTD Mapping Description 0.6//EN") &&
          systemId.equals("http://www.ais.pl/dtds/mapping_0_6.dtd")) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("net/sf/aislib/tools/mapping/library/resources/mapping_0_6.dtd");
        if (stream != null) {
          InputSource result = new InputSource(stream);
          result.setSystemId(getClass().getClassLoader().getResource("net/sf/aislib/tools/mapping/library/resources/mapping_0_6.dtd").toString());
          return result;
        }
      }
      if (publicId.equals("-//AIS.PL//DTD Mapping Description 0.7//EN") &&
          systemId.equals("http://www.ais.pl/dtds/mapping_0_7.dtd")) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("net/sf/aislib/tools/mapping/library/resources/mapping_0_7.dtd");
        if (stream != null) {
          InputSource result = new InputSource(stream);
          result.setSystemId(getClass().getClassLoader().getResource("net/sf/aislib/tools/mapping/library/resources/mapping_0_7.dtd").toString());
          return result;
        }
      }
      if (publicId.equals("-//AIS.PL//DTD Mapping Description 0.8//EN") && systemId.equals("http://www.ais.pl/dtds/mapping_0_8.dtd")) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("net/sf/aislib/tools/mapping/library/resources/mapping_0_8.dtd");
        if (stream != null) {
          InputSource result = new InputSource(stream);
          result.setSystemId(getClass().getClassLoader().getResource("net/sf/aislib/tools/mapping/library/resources/mapping_0_8.dtd").toString());
          return result;
        }
      }
      return null;
    }
  }
}
