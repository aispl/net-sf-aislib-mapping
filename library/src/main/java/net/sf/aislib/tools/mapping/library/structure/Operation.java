package net.sf.aislib.tools.mapping.library.structure;


/**
 * An abstract class extended by all Operations children.
 *
 * @author Milosz Tylenda, AIS.PL
 */
public abstract class Operation {

  private SqlQuery   sqlQuery;
  private JavaMethod javaMethod;

  public SqlQuery getSqlQuery() {
    return sqlQuery;
  }

  public void setSqlQuery(SqlQuery sqlQuery){
    this.sqlQuery = sqlQuery;
  }

  public JavaMethod getJavaMethod() {
    return javaMethod;
  }

  public void setJavaMethod(JavaMethod javaMethod) {
    this.javaMethod = javaMethod;
  }

}
