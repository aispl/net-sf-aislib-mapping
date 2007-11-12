package net.sf.aislib.tools.mapping.plugin.m2;

import java.io.File;

import net.sf.aislib.tools.mapping.library.Generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.util.StringUtils;

/**
 * Holds base set of properties and invokes generator specified by subclass.
 *
 * @author pikus
 */
public abstract class AbstractGeneratingMojo extends AbstractMojo {

  /**
   * Output directory for generated source files.
   *
   * @parameter expression="${project.build.directory}/generated-sources/ais-mapping"
   */
  private File outputDirectory;

  /**
   * File containing definition of structures.
   *
   * @parameter
   * @required
   */
  private File mappingFile;

  /**
   * Root package name for generated source files.
   *
   * @parameter
   * @required
   */
  private String packageName;

  /**
   * Maven2 project.
   *
   * @parameter expression="${project}"
   * @required
   */
  private MavenProject project;

  /**
   * Define, if generated code should depend on aislib classes.
   *
   * @parameter
   */
  private boolean aislibDependent = false;

  /**
   * Define, if code with generics should be generated.
   * @parameter
   */
  private boolean useGenerics = true;

  /**
   * Name of subpackage for db handler classes.
   *
   * @parameter
   */
  private String dbHandlersSubpackage = "dbhandlers";

  /**
   * Name of subpackage for map handler classes.
   *
   * @parameter
   */
  private String mapHandlersSubpackage = "handlers";

  /**
   * Name of subpackage for bean classes.
   *
   * @parameter
   */
  private String objectsSubpackage = "objects";

  /**
   * Name of subpackage for spring row mappers classes.
   *
   * @parameter
   */
  private String rowMappersSubpackage = "dao.jdbc.mappers";

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      if (!outputDirectory.exists()) {
        outputDirectory.mkdirs();
      }
      File destinationDir = new File(outputDirectory, StringUtils.replace(packageName, ".", File.separator));
      if (!destinationDir.exists()) {
        destinationDir.mkdirs();
      }
      project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
      Generator generator = createGenerator();
      generator.setAislibDependent(aislibDependent);
      generator.setDestinationDir(destinationDir);
      generator.setUseGenerics(useGenerics);
      generator.setDbHandlersSubpackage(dbHandlersSubpackage);
      generator.setMapHandlersSubpackage(mapHandlersSubpackage);
      generator.setObjectsSubpackage(objectsSubpackage);
      generator.setMappingFile(mappingFile);
      generator.setPackageName(packageName);
      generator.setRowMappersSubpackage(rowMappersSubpackage);
      generator.execute();
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * Creates instance of {@link Generator} that will be used for code generation.
   *
   * @return instance of {@link Generator}.
   */
  protected abstract Generator createGenerator();
}
