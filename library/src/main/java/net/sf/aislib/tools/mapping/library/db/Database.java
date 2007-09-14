package net.sf.aislib.tools.mapping.library.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.sf.aislib.tools.mapping.library.XmlDesc;

import org.jdom.Element;

/**
 * @author Micha³ Jastak, AIS.PL
 * @author Daniel Rychcik, AIS.PL
 */
public class Database {

  private boolean verboseFlag;

  private Connection       connection;
  private DatabaseMetaData dbmd;

  private String  dbCatalog;
  private String  dbDriver;
  private String  dbPassword;
  private String  dbSchema;
  private String  dbURL;
  private String  dbUser;

  private HashMap tables;
  private HashMap sequences;

  /**
   *
   */
  public Database(boolean tVerboseFlag) {
    verboseFlag = tVerboseFlag;
    tables      = new HashMap();
    sequences   = new HashMap();
  }

  /**
   *
   */
  public void setCatalog(String tCatalog) {
    dbCatalog = tCatalog;
  }

  /**
   *
   */
  public void setDriver(String tDriver) {
    dbDriver = tDriver;
  }

  /**
   *
   */
  public void setSchema(String tSchema) {
    dbSchema = tSchema;
  }

  /**
   *
   */
  public void connect(String tURL, String tUser, String tPassword)
         throws SQLException {
    dbURL      = tURL;
    dbUser     = tUser;
    dbPassword = tPassword;
    connect();
  }

  /**
   *
   */
  public void connect() throws SQLException {

    if (dbURL == null) {
      throw new SQLException ("Cannot estabilish connection: empty URL");
    }

    if (dbDriver != null) {
      try {
        Class.forName (dbDriver);
      } catch (ClassNotFoundException e) {
        throw new SQLException ("Can't find database driver: " + e.getMessage());
      }
    }

    connection = DriverManager.getConnection (dbURL, dbUser, dbPassword);
    dbmd = connection.getMetaData ();
  }

  /**
   *
   */
  public void genericInfo() throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    if (verboseFlag) {
      System.err.println ("\nDBMS Name: " + dbmd.getDatabaseProductName () + ", version: ");
      System.err.println (dbmd.getDatabaseProductVersion ());
      System.err.println ("\nJDBC driver: " + dbmd.getDriverName () + ", version: " + dbmd.getDriverVersion ());
    }
  }

  /**
   *
   */
  public void readTables(boolean fixedTableTypes, Set table_names ) throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    String[]   types  = new String[1];
    Vector     typesV = new Vector ();
    ResultSet  rs     = null;

    if (fixedTableTypes) {
      typesV.add("TABLE");
      typesV.add("VIEW");
    } else {
      rs = dbmd.getTableTypes ();
      while (rs.next ()) {
        typesV.add (rs.getString ("TABLE_TYPE"));
      }
      rs.close ();
    }

    types = (String[]) typesV.toArray (types);
    if (verboseFlag) {
      System.err.println ("\nSearch for following Database Table Types: ");
      for (int i = 0, j = types.length; i < j; i++) {
        System.err.print ("\t" + types[i]);
      }
      System.err.println ("");
    }

    if (verboseFlag) {
      System.err.print ("\nProcessing tables: \t(C - columns, I - indexes, FK - foreign keys, "
                       +"PK - primary keys, S - additional info)");
    }

    rs = dbmd.getTables (dbCatalog, dbSchema, "%", types);
    while (rs.next ()) {
      String table_name = rs.getString ("TABLE_NAME");
      if(table_names != null && !table_names.contains(table_name) ) {
        //skip table which weren't speciefied
        continue;
      }
      Table table = new Table (rs.getString ("TABLE_NAME"));
      if (verboseFlag) {
        System.err.print ("\n\t" + table.getName () + "\n\t");
      }
      table.setType    (rs.getString ("TABLE_TYPE"));
      table.setCatalog (rs.getString ("TABLE_CAT"));
      table.setSchema  (rs.getString ("TABLE_SCHEM"));

      readColumns     (table);
      readIndexes     (table);
      readPrimaryKeys (table);
      readForeignKeys (table);
      try {
        readAdditionalInfo(table);
      } catch (SQLException sqle) {
        ;
      }

      tables.put (table.getName (), table);
    }
    rs.close ();
    if (verboseFlag) { System.err.println (""); }
  }

  /**
   * @author Daniel Rychcik, AIS.PL
   * @author Micha³ Ja¶tak, AIS.PL
   */
  public void readSequences() throws SQLException {
    PreparedStatement pstmt = null;
    ResultSet         rs    = null;

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException("Connection not initialized");
    }

    if (verboseFlag) {
      System.err.println("\nSearch for Sequences:");
    }

    try {
      pstmt = connection.prepareStatement("SELECT * FROM ALL_SEQUENCES WHERE (SEQUENCE_OWNER = ?)");
      pstmt.setString(1, dbSchema);
      rs = pstmt.executeQuery();
      while (rs.next()) {
        if (verboseFlag) {
          System.err.println("  Found sequence "+rs.getString("SEQUENCE_NAME"));
        }
        Sequence seq = new Sequence();
        seq.setName(rs.getString("SEQUENCE_NAME"));
        seq.setSchema(dbSchema);
        seq.setMinValue(rs.getBigDecimal("MIN_VALUE"));
        seq.setMaxValue(rs.getBigDecimal("MAX_VALUE"));
        seq.setIncrement(rs.getBigDecimal("INCREMENT_BY"));
        seq.setInitialValue(rs.getBigDecimal("LAST_NUMBER"));
        seq.setCycleFlag(new Boolean(rs.getBoolean("CYCLE_FLAG")));
        seq.setOrderFlag(new Boolean(rs.getBoolean("ORDER_FLAG")));
        seq.setCacheSize(rs.getBigDecimal("CACHE_SIZE"));
        sequences.put(seq.getName(),seq);
      }
    } catch (SQLException sqle) {
      ;
    } finally {
      if (rs != null) { try { rs.close(); } catch (SQLException sqle) { ; } }
      if (pstmt != null) { try { pstmt.close(); } catch (SQLException sqle) { ; } }
      if (verboseFlag) {
        System.err.println("\n  Exception occured during reading system table ALL_SEQUENCES "
                         + "\n  - maybe not an Oracle database ?");
      }
    }
  }



  /**
   *
   */
  public void readForeignKeys(Table table) throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    ResultSet rs = dbmd.getImportedKeys(dbCatalog, dbSchema, table.getName ());
    while (rs.next()) {
      ForeignKey fKey = new ForeignKey(rs.getString ("FKCOLUMN_NAME"));
      fKey.setSourceTableName(rs.getString ("FKTABLE_NAME"));
      fKey.setSourceName(rs.getString ("FK_NAME"));

      fKey.setDestinationTableName(rs.getString ("PKTABLE_NAME"));
      fKey.setDestinationColumnName(rs.getString ("PKCOLUMN_NAME"));
      fKey.setDestinationName(rs.getString ("PK_NAME"));

      short temp = rs.getShort ("DELETE_RULE");
      if (!rs.wasNull()) {
        fKey.setDeleteRule(temp);
      }
      temp = rs.getShort ("UPDATE_RULE");
      if (!rs.wasNull()) {
        fKey.setUpdateRule(temp);
      }
      table.addForeignKey(fKey);
    }
    rs.close ();
    if (verboseFlag) { System.err.print("\tFK"); }
  }

  /**
   *
   */
  public void readPrimaryKeys(Table table) throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    ResultSet rs = dbmd.getPrimaryKeys(dbCatalog, dbSchema, table.getName ());
    while (rs.next()) {
      PrimaryKey pKey = new PrimaryKey(rs.getString("COLUMN_NAME"));
      pKey.setCatalog(rs.getString("TABLE_CAT"));
      pKey.setSchema(rs.getString("TABLE_SCHEM"));
      pKey.setTableName(rs.getString("TABLE_NAME"));
      pKey.setName(rs.getString("PK_NAME"));
      short temp = rs.getShort("KEY_SEQ");
      if (!rs.wasNull()) {
        pKey.setSequenceNumber(temp);
      }
      table.addPrimaryKey(pKey);
    }
    rs.close ();
    if (verboseFlag) { System.err.print ("\tPK"); }
  }

  /**
   *
   */
  public void readColumns(Table table) throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    ResultSet  rs = dbmd.getColumns(dbCatalog, dbSchema, table.getName (), "%");
    while (rs.next()) {
      Column column = new Column(rs.getString("COLUMN_NAME"));
      column.setCatalog(rs.getString("TABLE_CAT"));
      column.setSchema(rs.getString("TABLE_SCHEM"));
      column.setTableName(rs.getString("TABLE_NAME"));
      column.setDataType(rs.getShort("DATA_TYPE"));
      column.setTypeName(rs.getString("TYPE_NAME"));
      int temp = rs.getInt("COLUMN_SIZE");
      if (!rs.wasNull()) {
        column.setColumnSize(temp);
      }
      temp = rs.getInt("DECIMAL_DIGITS");
      if (!rs.wasNull()) {
        column.setDecimalDigits(temp);
      }
      temp = rs.getInt("NULLABLE");
      if (!rs.wasNull()) {
        column.setNullable(temp);
      }
      column.setColumnDef(rs.getString("COLUMN_DEF"));
      temp = rs.getInt("ORDINAL_POSITION");
      if (!rs.wasNull()) {
        column.setOrdinalPosition(temp);
      }
      table.addColumn(column);
    }
    rs.close ();
    if (verboseFlag) { System.err.print("\tC"); }
  }

  /**
   *
   */
  public void readIndexes(Table table) throws SQLException {

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    if (! table.getType().equalsIgnoreCase("table")) { return; }
    ResultSet  rs = dbmd.getIndexInfo(dbCatalog, dbSchema, table.getName (), false, false);
    while (rs.next()) {
      Index index = new Index(rs.getShort("ORDINAL_POSITION"));
      index.setCatalog(rs.getString("TABLE_CAT"));
      index.setSchema(rs.getString("TABLE_SCHEM"));
      index.setTableName(rs.getString("TABLE_NAME"));
      index.setName(rs.getString("INDEX_NAME"));
      index.setColumnName(rs.getString("COLUMN_NAME"));
      table.addIndex(index);
    }
    rs.close ();
    if (verboseFlag) { System.err.print("\tI"); }
  }

  /**
   *
   */
  public void readAdditionalInfo (Table table) throws SQLException {
    Statement stmt = null;
    ResultSet rs   = null;

    if ((connection == null) || (dbmd == null)) {
      throw new SQLException ("Connection not initialized");
    }

    try {
      stmt = connection.createStatement();
      stmt.setFetchSize(1);
      stmt.setMaxRows(1);
      rs = stmt.executeQuery("SELECT * FROM " + table.getName());

      ResultSetMetaData rsmd = rs.getMetaData();
      for (int i = 1, j = rsmd.getColumnCount(); i <= j; i++) {
        String columnName = rsmd.getColumnName(i);
        Column column = table.getColumn(columnName);
        if (column != null) {
          column.setClassName(rsmd.getColumnClassName (i));
        }
      }
    } catch (SQLException sqle) {
      throw sqle;
    } finally {
      if (rs != null) { try { rs.close(); } catch (SQLException sqle) { ; } }
      if (stmt != null) { try { stmt.close(); } catch (SQLException sqle) { ; } }
    }
    if (verboseFlag) { System.err.print("\tS"); }
  }

  /**
   *
   */
  public String toString() {
    return new String("" + tables + sequences);
  }

  /**
   *
   * @return
   */
  public List toSeparateXMLs() {
      List xmls = new Vector();
      if (!tables.isEmpty ()) {

        for (Iterator it = tables.keySet ().iterator ();  it.hasNext (); ) {
          //Element result = new Element ("database");
          String tableName = (String) it.next ();
          Table table = (Table) tables.get (tableName);
          Element result = table.toXML();
          xmls.add( new XmlDesc( tableName, result ) );
        }
      }
      if (!sequences.isEmpty()) {

        for (Iterator it = sequences.keySet().iterator(); it.hasNext();) {
          //Element result = new Element ("database");
          String sequenceName = (String) it.next();
          Sequence sequence = (Sequence) sequences.get(sequenceName);
          Element result = sequence.toXML();

          xmls.add( new XmlDesc( sequenceName, result ) );
        }
      }
      return xmls;
    }

  /**
   *
   */
  public Element toXML() {
    Element result = new Element ("database");
    if (!tables.isEmpty ()) {
      for (Iterator it = tables.keySet ().iterator ();  it.hasNext (); ) {
        String tableName = (String) it.next ();
        Table table = (Table) tables.get (tableName);
        result.addContent (table.toXML ());
      }
    }
    if (!sequences.isEmpty()) {
      for (Iterator it = sequences.keySet().iterator(); it.hasNext();) {
        String sequenceName = (String) it.next();
        Sequence sequence = (Sequence) sequences.get(sequenceName);
        result.addContent(sequence.toXML());
      }
    }
    return result;
  }

} // class
