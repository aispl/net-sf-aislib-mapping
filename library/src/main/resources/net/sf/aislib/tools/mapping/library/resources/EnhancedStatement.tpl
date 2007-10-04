
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A PreparedStatement wrapper which suports prepared ('?')
 * and raw ('??') parameters.
 * A raw parameter is identified by '??' marker and set before
 * creating an actual PreparedStatement instance.
 *
 * @author BeanHelperGenerator
 */
public class EnhancedStatement {

  /** The wrapped PreparedStatement. */
  protected PreparedStatement realStmt;

  /** Connection used to create PreparedStatement. */
  protected Connection con;

  /** Query String used to create PreparedStatement. */
  protected String query;

  /** Maximum number of parameters in each of prepared and raw groups. */
  protected static final int MAX_PARAMETER_COUNT = 40;

  /** Holds types and values of prepared parameters. */
  protected TypeValuePair[] preparedFields = new TypeValuePair[MAX_PARAMETER_COUNT];

  /** Holds types and values of raw parameters. */
  protected TypeValuePair[] rawFields = new TypeValuePair[MAX_PARAMETER_COUNT];

  /** Regexp to find both prepared and raw parameters in query String. */
  protected static final Pattern patternAllMarkers = Pattern.compile("\\?{1,2}");

  /** Regexp to find raw parameters in query String. */
  protected static final Pattern patternRawMarkers = Pattern.compile("\\?\\?");

  /** String identifying prepared parameter in query String. */
  protected static final String MARKER_TYPE_PREPARED = "?";

  // String identifying raw parameter in query String.
  //protected static final String MARKER_TYPE_RAW      = "??";

  /**
   * Creates and returns a new instance of this class.
   * The query String may contain '??' markers.
   *
   * @param con the connection to create a PreparedStatement instance from
   * @param query the query to create a PreparedStatement instance from
   */
  public static EnhancedStatement getInstance(Connection con, String query) {
    return new EnhancedStatement(con, query);
  }

  /**
   * Constructs the object based upon the given Connection and query String.
   *
   * @param con the connection to create a PreparedStatement instance from
   * @param query the query to create a PreparedStatement instance from
   */
  protected EnhancedStatement(Connection con, String query) {
    this.con = con;
    this.query = query;
  }

  /**
   * Adds the given data to internal structures.
   * This method is invoked from within some of setX methods.
   *
   * @param parameterIndex parameter index, starts from 1
   * @param value parameter value
   * @param sqlType parameter type
   * @throws SQLException
   */
  protected void addField(int parameterIndex, Object value, int sqlType) throws SQLException {
    Matcher matcher = patternAllMarkers.matcher(query);
    int preparedIndex = 0;
    int rawIndex = 0;
    String marker = "";
    for (int i = 0; i < parameterIndex; i++) {
      if (!matcher.find()) {
        throw new SQLException("No marker found for parameter index " + parameterIndex + " in query '" + query + "'.");
      }

      marker = matcher.group();
      if (marker.equals(MARKER_TYPE_PREPARED)) {
        preparedIndex++;
      } else {
        rawIndex++;
      }

    }

    if (marker.equals(MARKER_TYPE_PREPARED)) {
      preparedFields[preparedIndex] = new TypeValuePair(sqlType, value);
    } else {
      rawFields[rawIndex] = new TypeValuePair(sqlType, value);
    }
  }

  /**
   * Sets '??' parameters on query String.
   */
  protected void fillRawFields() {
    Matcher matcher = patternRawMarkers.matcher(query);
    StringBuffer sb = new StringBuffer(query.length() + 64);
    for (int i = 1; rawFields[i] != null; i++) {
      TypeValuePair pair = rawFields[i];
      matcher.find();
      matcher.appendReplacement(sb, String.valueOf(pair.getValue()));
    }
    matcher.appendTail(sb);
    query = sb.toString();
  }

  /**
   * Executes setX methods on realStmt.
   *
   * @throws SQLException
   */
  protected void fillPreparedFields() throws SQLException {
    for (int i = 1; preparedFields[i] != null; i++) {
      TypeValuePair pair = preparedFields[i];
      if (pair.getValue() != null) {
        realStmt.setObject(i, pair.getValue());
      } else {
        realStmt.setNull(i, pair.getSqlType());
      }
    }
  }

  /**
   * Does actual parameter setting and executes query.
   *
   * @return
   * @throws java.sql.SQLException
   */
  public ResultSet executeQuery() throws SQLException {
    fillRawFields();
    realStmt = con.prepareStatement(query);
    fillPreparedFields();
    return realStmt.executeQuery();
  }

  /**
   * @param i
   * @param x
   * @throws java.sql.SQLException
   */
  public void setArray(int i, Array x) throws SQLException {
    addField(i, x, Types.ARRAY);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    addField(parameterIndex, x, Types.NUMERIC);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setDouble(int parameterIndex, double x) throws SQLException {
    addField(parameterIndex, new Double(x), Types.DOUBLE);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setFloat(int parameterIndex, float x) throws SQLException {
    addField(parameterIndex, new Float(x), Types.FLOAT);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setInt(int parameterIndex, int x) throws SQLException {
    addField(parameterIndex, new Integer(x), Types.INTEGER);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setLong(int parameterIndex, long x) throws SQLException {
    addField(parameterIndex, new Long(x), Types.BIGINT);
  }

  /**
   * @param parameterIndex
   * @param sqlType
   * @throws java.sql.SQLException
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    addField(parameterIndex, null, sqlType);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setString(int parameterIndex, String x) throws SQLException {
    addField(parameterIndex, x, Types.VARCHAR);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    addField(parameterIndex, x, Types.TIMESTAMP);
  }

  /**
   * @throws java.sql.SQLException
   */
  public void addBatch() throws SQLException {
    realStmt.addBatch();
  }

  /**
   * @param sql
   * @throws java.sql.SQLException
   */
  public void addBatch(String sql) throws SQLException {
    realStmt.addBatch(sql);
  }

  /**
   * @throws java.sql.SQLException
   */
  public void cancel() throws SQLException {
    realStmt.cancel();
  }

  /**
   * @throws java.sql.SQLException
   */
  public void clearBatch() throws SQLException {
    realStmt.clearBatch();
  }

  /**
   * @throws java.sql.SQLException
   */
  public void clearParameters() throws SQLException {
    realStmt.clearParameters();
  }

  /**
   * @throws java.sql.SQLException
   */
  public void clearWarnings() throws SQLException {
    realStmt.clearWarnings();
  }

  /**
   * @throws java.sql.SQLException
   */
  public void close() throws SQLException {
    realStmt.close();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public boolean execute() throws SQLException {
    return realStmt.execute();
  }

  /**
   * @param sql
   * @return
   * @throws java.sql.SQLException
   */
  public boolean execute(String sql) throws SQLException {
    return realStmt.execute(sql);
  }

  /**
   * @param sql
   * @param autoGeneratedKeys
   * @return
   * @throws java.sql.SQLException
   */
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return realStmt.execute(sql, autoGeneratedKeys);
  }

  /**
   * @param sql
   * @param columnIndexes
   * @return
   * @throws java.sql.SQLException
   */
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return realStmt.execute(sql, columnIndexes);
  }

  /**
   * @param sql
   * @param columnNames
   * @return
   * @throws java.sql.SQLException
   */
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return realStmt.execute(sql, columnNames);
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int[] executeBatch() throws SQLException {
    return realStmt.executeBatch();
  }

  /**
   * @param sql
   * @return
   * @throws java.sql.SQLException
   */
  public ResultSet executeQuery(String sql) throws SQLException {
    return realStmt.executeQuery(sql);
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int executeUpdate() throws SQLException {
    return realStmt.executeUpdate();
  }

  /**
   * @param sql
   * @return
   * @throws java.sql.SQLException
   */
  public int executeUpdate(String sql) throws SQLException {
    return realStmt.executeUpdate(sql);
  }

  /**
   * @param sql
   * @param autoGeneratedKeys
   * @return
   * @throws java.sql.SQLException
   */
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    return realStmt.executeUpdate(sql, autoGeneratedKeys);
  }

  /**
   * @param sql
   * @param columnIndexes
   * @return
   * @throws java.sql.SQLException
   */
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return realStmt.executeUpdate(sql, columnIndexes);
  }

  /**
   * @param sql
   * @param columnNames
   * @return
   * @throws java.sql.SQLException
   */
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return realStmt.executeUpdate(sql, columnNames);
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public Connection getConnection() throws SQLException {
    return realStmt.getConnection();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getFetchDirection() throws SQLException {
    return realStmt.getFetchDirection();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getFetchSize() throws SQLException {
    return realStmt.getFetchSize();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public ResultSet getGeneratedKeys() throws SQLException {
    return realStmt.getGeneratedKeys();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getMaxFieldSize() throws SQLException {
    return realStmt.getMaxFieldSize();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getMaxRows() throws SQLException {
    return realStmt.getMaxRows();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public ResultSetMetaData getMetaData() throws SQLException {
    return realStmt.getMetaData();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public boolean getMoreResults() throws SQLException {
    return realStmt.getMoreResults();
  }

  /**
   * @param current
   * @return
   * @throws java.sql.SQLException
   */
  public boolean getMoreResults(int current) throws SQLException {
    return realStmt.getMoreResults(current);
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return realStmt.getParameterMetaData();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getQueryTimeout() throws SQLException {
    return realStmt.getQueryTimeout();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public ResultSet getResultSet() throws SQLException {
    return realStmt.getResultSet();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getResultSetConcurrency() throws SQLException {
    return realStmt.getResultSetConcurrency();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getResultSetHoldability() throws SQLException {
    return realStmt.getResultSetHoldability();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getResultSetType() throws SQLException {
    return realStmt.getResultSetType();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public int getUpdateCount() throws SQLException {
    return realStmt.getUpdateCount();
  }

  /**
   * @return
   * @throws java.sql.SQLException
   */
  public SQLWarning getWarnings() throws SQLException {
    return realStmt.getWarnings();
  }

  /**
   * @param parameterIndex
   * @param x
   * @param length
   * @throws java.sql.SQLException
   */
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    realStmt.setAsciiStream(parameterIndex, x, length);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param length
   * @throws java.sql.SQLException
   */
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    realStmt.setBinaryStream(parameterIndex, x, length);
  }

  /**
   * @param i
   * @param x
   * @throws java.sql.SQLException
   */
  public void setBlob(int i, Blob x) throws SQLException {
    realStmt.setBlob(i, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    realStmt.setBoolean(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setByte(int parameterIndex, byte x) throws SQLException {
    realStmt.setByte(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    realStmt.setBytes(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param reader
   * @param length
   * @throws java.sql.SQLException
   */
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    realStmt.setCharacterStream(parameterIndex, reader, length);
  }

  /**
   * @param i
   * @param x
   * @throws java.sql.SQLException
   */
  public void setClob(int i, Clob x) throws SQLException {
    realStmt.setClob(i, x);
  }

  /**
   * @param name
   * @throws java.sql.SQLException
   */
  public void setCursorName(String name) throws SQLException {
    realStmt.setCursorName(name);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setDate(int parameterIndex, Date x) throws SQLException {
    realStmt.setDate(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param cal
   * @throws java.sql.SQLException
   */
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    realStmt.setDate(parameterIndex, x, cal);
  }

  /**
   * @param enable
   * @throws java.sql.SQLException
   */
  public void setEscapeProcessing(boolean enable) throws SQLException {
    realStmt.setEscapeProcessing(enable);
  }

  /**
   * @param direction
   * @throws java.sql.SQLException
   */
  public void setFetchDirection(int direction) throws SQLException {
    realStmt.setFetchDirection(direction);
  }

  /**
   * @param rows
   * @throws java.sql.SQLException
   */
  public void setFetchSize(int rows) throws SQLException {
    realStmt.setFetchSize(rows);
  }

  /**
   * @param max
   * @throws java.sql.SQLException
   */
  public void setMaxFieldSize(int max) throws SQLException {
    realStmt.setMaxFieldSize(max);
  }

  /**
   * @param max
   * @throws java.sql.SQLException
   */
  public void setMaxRows(int max) throws SQLException {
    realStmt.setMaxRows(max);
  }

  /**
   * @param paramIndex
   * @param sqlType
   * @param typeName
   * @throws java.sql.SQLException
   */
  public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
    realStmt.setNull(paramIndex, sqlType, typeName);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setObject(int parameterIndex, Object x) throws SQLException {
    realStmt.setObject(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param targetSqlType
   * @throws java.sql.SQLException
   */
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    realStmt.setObject(parameterIndex, x, targetSqlType);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param targetSqlType
   * @param scale
   * @throws java.sql.SQLException
   */
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
    realStmt.setObject(parameterIndex, x, targetSqlType, scale);
  }

  /**
   * @param seconds
   * @throws java.sql.SQLException
   */
  public void setQueryTimeout(int seconds) throws SQLException {
    realStmt.setQueryTimeout(seconds);
  }

  /**
   * @param i
   * @param x
   * @throws java.sql.SQLException
   */
  public void setRef(int i, Ref x) throws SQLException {
    realStmt.setRef(i, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setShort(int parameterIndex, short x) throws SQLException {
    realStmt.setShort(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setTime(int parameterIndex, Time x) throws SQLException {
    realStmt.setTime(parameterIndex, x);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param cal
   * @throws java.sql.SQLException
   */
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    realStmt.setTime(parameterIndex, x, cal);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param cal
   * @throws java.sql.SQLException
   */
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    realStmt.setTimestamp(parameterIndex, x, cal);
  }

  /**
   * @param parameterIndex
   * @param x
   * @param length
   * @throws java.sql.SQLException
   */
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    // Does not invoke super implementation because it is deprecated.
  }

  /**
   * @param parameterIndex
   * @param x
   * @throws java.sql.SQLException
   */
  public void setURL(int parameterIndex, URL x) throws SQLException {
    realStmt.setURL(parameterIndex, x);
  }

  /**
   * Representation of value for PreparedStatement parameter.
   */
  protected static class TypeValuePair {

    int sqlType;
    Object value;

    TypeValuePair(int sqlType, Object value) {
      this.sqlType = sqlType;
      this.value = value;
    }

    /**
     * @return SQL type.
     */
    int getSqlType() {
      return sqlType;
    }

    /**
     * @return value.
     */
    Object getValue() {
      return value;
    }

    /**
     * @return the description of this pair
     */
    public String toString() {
      return "Type=" + sqlType + " : Value=" + value;
    }

  }
}
