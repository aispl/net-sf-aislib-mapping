package net.sf.aislib.tools.mapping.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.aislib.tools.mapping.library.db.Database;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 * Generates structure.xml file from an existing database.
 *
 * @author Michal Jastak, AIS.PL
 * @author Tomasz Pik, AIS.PL
 * @author Milosz Tylenda, AIS.PL
 */
public class Describe {

  private static final String DB_PARTS_DIR = "db_parts";

  /**
   * When true and short version requested, the 'default' attribute will not
   * be set in java-field element.
   */
  private boolean noDefault;

  private boolean separateFiles;
  private boolean verboseFlag;
  private boolean fixedTableTypes;
  private Database dBase;
  private Properties dBaseProperties;
  private String configFilePath;
  private String dbCatalog;
  private String dbDriver;
  private String dbPassword;
  private String dbSchema;
  private String dbURL;
  private String dbUser;

  private Set tables;

  /**
   *
   */

  public Describe() {
    verboseFlag = false;
    fixedTableTypes = false;
    configFilePath = "describe.conf";
    dBaseProperties = new Properties();
  }

  public boolean inSeparateFiles() {
    return separateFiles;
  }

  /**
   *
   */

  public boolean verbose() {
    return verboseFlag;
  }

  /**
   *
   */

  public void initialize() throws IOException, SQLException {
    try {
      dBaseProperties.load(new FileInputStream(configFilePath));
    } catch (IOException ioe) {

    }
    if (dbCatalog == null) {
      dbCatalog = dBaseProperties.getProperty("catalog");
    }
    if (dbDriver == null) {
      dbDriver = dBaseProperties.getProperty("driver");
    }
    if (dbPassword == null) {
      dbPassword = dBaseProperties.getProperty("password");
    }
    if (dbSchema == null) {
      dbSchema = dBaseProperties.getProperty("schema");
    }
    if (dbURL == null) {
      dbURL = dBaseProperties.getProperty("url");
    }
    if (dbUser == null) {
      dbUser = dBaseProperties.getProperty("user");
    }

    if (verboseFlag) {
      System.err.println(
        "\nInitialized as follows:\n"
          + "\n\tDriver:   "
          + dbDriver
          + "\n\tURL:      "
          + dbURL
          + "\n\tUser:     "
          + dbUser
          + "\n\tPassword: "
          + dbPassword
          + "\n\tCatalog:  "
          + dbCatalog
          + "\n\tSchema:   "
          + dbSchema);
    }

    dBase = new Database(verboseFlag);
    dBase.setCatalog(dbCatalog);
    dBase.setDriver(dbDriver);
    dBase.setSchema(dbSchema);
    dBase.connect(dbURL, dbUser, dbPassword);
  }

  /**
   *
   */
  public void readTables() throws SQLException {
    dBase.readTables(fixedTableTypes, tables);
  }

  /**
   *
   */
  public void readSequences() throws SQLException {
    dBase.readSequences();
  }

  /**
   *
   */
  public void genericInfo() throws SQLException {
    dBase.genericInfo();
  }

  /**
   *
   */
  public String toString() {
    return dBase.toString();
  }

  public void writeToSeparateFiles(boolean isShort) throws Exception {
    List xmls = dBase.toSeparateXMLs();
    for (Iterator iter = xmls.iterator(); iter.hasNext();) {
      XmlDesc xmlDesc = (XmlDesc) iter.next();

      String xmlName = xmlDesc.getName();
      Element root = xmlDesc.getElement();

      Vector cnt = new Vector();
      cnt.add(root);
      Document doc = new Document(cnt);

      if (isShort) {
        TransformerFactory tFactory = TransformerFactory.newInstance();

        StreamSource style = getStyle();
        JDOMResult result = new JDOMResult();
        JDOMSource source = new JDOMSource(doc);

        Transformer trans = tFactory.newTransformer(style);
        trans.setOutputProperty(OutputKeys.METHOD, "xml");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.transform(source, result);
        doc = result.getDocument();
      }
      XMLOutputter xo = new XMLOutputter();
      String xoDoc = xo.outputString(doc);

      Writer writer = new FileWriter(DB_PARTS_DIR + File.separatorChar + xmlName + ".xml");
      writer.write(xoDoc.toString());
      writer.close();

      System.out.println("Saved: " + xmlName + ".xml");
    }

    if (tables == null) {
      System.out.println("Saved: structure.xml");

      Element root = new Element("database");

      for (Iterator iter = xmls.iterator(); iter.hasNext();) {
        XmlDesc xmlDesc = (XmlDesc) iter.next();
        String xmlName = xmlDesc.getName();
        EntityRef entityRef = new EntityRef("db_part_" + xmlName);
        root.addContent(entityRef);
      }
      Vector cnt = new Vector();
      cnt.add(root);
      Document doc = new Document(cnt);

      DocType docType =
        new DocType("database", "-//AIS.PL//DTD Mapping Description 0.4//EN", "http://www.ais.pl/dtds/mapping_0_4.dtd");

      StringBuffer entities = new StringBuffer();
      for (Iterator iter = xmls.iterator(); iter.hasNext();) {
        XmlDesc xmlDesc = (XmlDesc) iter.next();
        String xmlName = xmlDesc.getName();
        entities.append(
          "<!ENTITY  db_part_" + xmlName + "   SYSTEM \"./" + DB_PARTS_DIR + "/" + xmlName + ".xml\">\n");
      }
      docType.setInternalSubset(entities.toString());

      doc.setDocType(docType);

      XMLOutputter xo = new XMLOutputter();
      String xoDoc = xo.outputString(doc);
      Writer writer = new FileWriter("structure.xml");
      writer.write(xoDoc.toString());
      writer.close();
    }

  }

  /**
   *
   */
  public void writeTo(boolean isShort) throws Exception {
    Element root = dBase.toXML();
    Vector cnt = new Vector();
    cnt.add(root);
    Document doc = new Document(cnt);

    if (isShort) {
      TransformerFactory tFactory = TransformerFactory.newInstance();

      StreamSource style = getStyle();
      JDOMResult result = new JDOMResult();
      JDOMSource source = new JDOMSource(doc);

      Transformer trans = tFactory.newTransformer(style);
      trans.setOutputProperty(OutputKeys.METHOD, "xml");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      trans.transform(source, result);
      doc = result.getDocument();
      doc.setDocType(
        new DocType(
          "database",
          "-//AIS.PL//DTD Mapping Description 0.4//EN",
          "http://www.ais.pl/dtds/mapping_0_4.dtd"));
    }
    XMLOutputter xo = new XMLOutputter();
    String xoDoc = xo.outputString(doc);
    System.out.println(xoDoc);
  }

  /**
   * Returns the suitable StreamSource depending on the noDefault value.
   */
  private StreamSource getStyle() {
    if (noDefault) {
      return new StreamSource(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("pl/aislib/tools/mapping/mapping-no-default.xsl"));
    } else {
      return new StreamSource(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("pl/aislib/tools/mapping/mapping.xsl"));
    }
  }

  /**
   *
   */
  public void parseArgs(String ArgV[]) throws ArrayIndexOutOfBoundsException {
    int i = 0;
    while (i < ArgV.length) {
      if (ArgV[i].equals("--config-file") || ArgV[i].equals("-c")) {
        i++;
        configFilePath = ArgV[i];
      }

      if (ArgV[i].equals("--catalog") || ArgV[i].equals("-t")) {
        i++;
        dbCatalog = ArgV[i];
      }

      if (ArgV[i].equals("--driver") || ArgV[i].equals("-d")) {
        i++;
        dbDriver = ArgV[i];
      }

      if (ArgV[i].equals("--password") || ArgV[i].equals("-p")) {
        i++;
        dbPassword = ArgV[i];
      }

      if (ArgV[i].equals("--schema") || ArgV[i].equals("-s")) {
        i++;
        dbSchema = ArgV[i];
      }

      if (ArgV[i].equals("--url") || ArgV[i].equals("-r")) {
        i++;
        dbURL = ArgV[i];
      }

      if (ArgV[i].equals("--user") || ArgV[i].equals("-u")) {
        i++;
        dbUser = ArgV[i];
      }

      if (ArgV[i].equals("--verbose") || ArgV[i].equals("-v")) {
        verboseFlag = true;
      }

      if (ArgV[i].equals("--files") || ArgV[i].equals("-f")) {
        separateFiles = true;
      }

      if (ArgV[i].equals("--table") || ArgV[i].equals("-T")) {
        i++;
        if (tables == null) {
          tables = new HashSet();
        }
        tables.add(ArgV[i]);
      }

      if (ArgV[i].equals("--fixed-table-types")) {
        fixedTableTypes = true;
      }

      if (ArgV[i].equals("--no-default-values")) {
        noDefault = true;
      }

      i++;
    }
  }

  /**
   *
   */

  public String usageDescription() {

    StringBuffer result = new StringBuffer();
    result.append("\nUsage:\n");
    result.append("\t-c, --config-file  config file path\n");
    result.append("\nConfiguration options may be overwritten using following options:\n\n");
    result.append("\t-d, --driver    JDBC driver class name\n");
    result.append("\t-p, --password  password for database access authorization\n");
    result.append("\t-r, --url       database URL\n");
    result.append("\t-s, --schema    database schema\n");
    result.append("\t-t, --catalog   database catalog\n");
    result.append("\t-u, --user      user name for database access authorization\n");
    result.append("\nOther options:\n\n");
    result.append("\t--fixed-table-types   use only TABLE and VIEW as possible table types\n");
    result.append("\t--short               short version (for tools-mapping)\n");
    result.append("\t--no-default-values   do not output 'default' attributes in <java-field>\n");
    result.append("\t-v, --verbose         turn on verbose mode\n");
    result.append("\t-T, --table           generate description for one table\n");
    result.append("\t-f, --files           create file for each table\n");
    result.append("\nVersion: $Name:  $\n");
    return new String(result);
  }

  /**
   *
   */

  public static void main(String ArgV[]) {

    Describe describe = new Describe();
    if (ArgV.length == 0) {
      System.out.println(describe.usageDescription());
      System.exit(1);
    }
    try {
      describe.parseArgs(ArgV);
    } catch (Exception ex) {
      System.err.println(describe.usageDescription());
      System.exit(1);
    }

    try {
      describe.initialize();
    } catch (Exception ex) {
      System.err.println("\nException caught during initialization:\n\n\t" + ex.getMessage());
      if (describe.verbose()) {
        ex.printStackTrace();
      }
      System.exit(2);
    }

    try {
      describe.genericInfo();
      describe.readTables();
      describe.readSequences();

      boolean isShort = false;
      for (int i = 0; i < ArgV.length; i++) {
        if ((ArgV[i].equals("--short")) || (ArgV[i].equals("-s"))) {
          isShort = true;
        }
      }
      if (describe.inSeparateFiles()) {
        describe.writeToSeparateFiles(isShort);
      } else {
        describe.writeTo(isShort);
      }
    } catch (Exception ex) {
      System.err.println("\nException caught:\n\n\t" + ex.getMessage());
      if (describe.verbose()) {
        ex.printStackTrace();
      }
      System.exit(3);
    }
  }

} // class
