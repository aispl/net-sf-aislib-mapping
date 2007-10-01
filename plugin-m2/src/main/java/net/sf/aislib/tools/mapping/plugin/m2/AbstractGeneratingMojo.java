package net.sf.aislib.tools.mapping.plugin.m2;

import java.io.File;

import net.sf.aislib.tools.mapping.library.Generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.util.StringUtils;

public abstract class AbstractGeneratingMojo extends AbstractMojo {

  /**
   * @parameter expression="${project.build.directory}/generated-sources/ais-mapping"
   */
  private File outputDirectory;

  /**
   * @parameter
   * @required
   */
  private File mappingFile;

  /**
   * @parameter
   * @required
   */
  private String packageName;

  /**
   * @parameter expression="${project}"
   *
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
      generator.execute();
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  protected abstract Generator createGenerator();
}
