package net.sf.aislib.tools.mapping.library;

import java.io.File;
import java.io.IOException;

import net.sf.aislib.tools.mapping.library.structure.Database;
import net.sf.aislib.tools.mapping.library.structure.StructureParser;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * @author Milosz Tylenda, AIS.PL
 * @author Wojciech Swiatek, AIS.PL
 * @author Pawel Chmielewski, AIS.PL
 */
public abstract class Generator extends Task {

  // Fields

  protected Database database;

  /**
   * Name of base package.
   */
  protected String packageName;

  /**
   * Base directory files will be generated into.
   */
  protected File destinationDir;

  /**
   * Mapping file with the structure.
   */
  protected File mappingFile;

  /**
   * Defines subpackage of objects and objects.base.
   */
  protected String objectsSubpackage = "objects";

  /**
   * Defines subdirectory of objects and objects.base.
   */
  protected String objectsSubdir = "objects";

  /**
   * Subpackage of database handlers.
   */
  protected String dbHandlersSubpackage = "dbhandlers";

  /**
   * Subdirectory of database handlers.
   */
  protected String dbHandlersSubdir = "dbhandlers";

  /**
   * Subpackage of map handlers.
   */
  protected String mapHandlersSubpackage = "handlers";

  /**
   * Subdirectory of map handlers.
   */
  protected String mapHandlersSubdir = "handlers";

  /**
   * Subpackage of row mappers.
   */
  protected String rowMappersSubpackage = "dao.jdbc.mappers";

  /**
   * Subdirectory of row mappers.
   */
  protected String rowMappersSubdir = "dao" + File.separator + "jdbc" + File.separator + "mappers";

  /**
   * Switch for removing aislib dependency
   */
  protected boolean aislibDependent = true;

  /**
   * If true, adds List<Object> for 'select' methods
   */
  protected boolean useGenerics = false;

  // Public methods

  /**
   * Sets name of base package.
   *
   * @param packageName name of base package.
   */
  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  /**
   * Sets base directory files will be generated into.
   *
   * @param destinationDir directory as object.
   */
  public void setDestinationDir(File destinationDir) {
    this.destinationDir = destinationDir;
  }

  /**
   * Sets mapping file with the structure.
   *
   * @param mappingFile file as object.
   */
  public void setMappingFile(File mappingFile) {
    this.mappingFile = mappingFile;
  }

  /**
   * Sets the subpackage of objects and objects.base.
   *
   * Defaults to "objects".
   *
   * @param objectsSubpackage subpackage of objects.
   */
  public void setObjectsSubpackage(String objectsSubpackage) {
    this.objectsSubpackage = objectsSubpackage;
    objectsSubdir = packageToDirectory(objectsSubpackage);
  }

  /**
   * Sets the subpackage of database handlers.
   *
   * Defaults to "dbhandlers".
   *
   * @param dbHandlersSubpackage subpackage of database handlers.
   */
  public void setDbHandlersSubpackage(String dbHandlersSubpackage) {
    this.dbHandlersSubpackage = dbHandlersSubpackage;
    dbHandlersSubdir = packageToDirectory(dbHandlersSubpackage);
  }

  /**
   * Sets the subpackage of map handlers.
   *
   * Defaults to "handlers".
   *
   * @param mapHandlersSubpackage subpackage of map handlers.
   */
  public void setMapHandlersSubpackage(String mapHandlersSubpackage) {
    this.mapHandlersSubpackage = mapHandlersSubpackage;
    mapHandlersSubdir = packageToDirectory(mapHandlersSubpackage);
  }

  /**
   * Sets the aislib dependency switch.
   *
   * Defaults to "true".
   *
   * @param new aislibDependent value.
   */
  public void setAislibDependent(boolean aislibDependent) {
    this.aislibDependent = aislibDependent;
  }

  /**
   * Turns if generated code should use generics.
   *
   * Defaults to "false".
   *
   * @param useGenerics value.
   */
  public void setUseGenerics(boolean useGenerics) {
    this.useGenerics = useGenerics;
  }

  /**
   * Set the subpackage of row mappers
   *
   * Defaults to "dao.jdbc.mappers"
   *
   * @param rowMappersSubpackage
   */
  public void setRowMappersSubpackage(String rowMappersSubpackage) {
    this.rowMappersSubpackage = rowMappersSubpackage;
    this.rowMappersSubdir = packageToDirectory(rowMappersSubpackage);
  }

  /**
   *
   */
  public void execute() throws BuildException {

    if (packageName == null) {
      throw new BuildException("packageName must be specified");
    }
    if (mappingFile == null) {
      throw new BuildException("mappingFile must be specified");
    }
    if (destinationDir == null) {
      throw new BuildException("destinationDir must be specified");
    }
    if (!destinationDir.exists()) {
      throw new BuildException("directory: " + destinationDir + " doesn't exist");
    }
    if (!destinationDir.isDirectory()) {
      throw new BuildException(destinationDir + "is not a directory");
    }

    try {
      StructureParser sp = new StructureParser(mappingFile);
      database           = sp.createDatabase();
    } catch (Exception e) {
      throw new BuildException(e);
    }
    try {
      if (database != null) {
        generate();
      } else {
        log("Error: cannot parse mappingFile.", Project.MSG_ERR);
      }
    } catch (IOException ioe) {
      throw new BuildException(ioe);
    }
  }

  public abstract void generate() throws IOException;

  /**
   * Replaces all dots by file separator in the given String.
   *
   * @param pack <code>String</code> with dots.
   * @return <code>String</code> with file separator.
   */
  private String packageToDirectory(String pack) {
    return pack.replace('.', File.separatorChar);
  }

  /**
   * It is overridden to allow use outside Ant.
   *
   * @param msg log message.
   */
  public void log(String msg) {
    try {
      super.log(msg);
    } catch (RuntimeException e) {
      System.out.println(msg);
    }
  }

} // pl.aislib.tools.mapping.Generator class
