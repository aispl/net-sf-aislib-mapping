package net.sf.aislib.tools.mapping.plugin.m2;

import java.io.File;

import net.sf.aislib.tools.mapping.library.Generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Holds base set of properties and invokes generator specified by subclass.
 *
 * @author pikus
 */
public abstract class AbstractGeneratingMojo extends AbstractMojo {

  @Component
  private BuildContext buildContext;

  /**
   * Output directory for generated source files.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-sources/ais-mapping")
  private File outputDirectory;

  /**
   * File containing definition of structures.
   */
  @Parameter(required = true)
  private File mappingFile;

  /**
   * Root package name for generated source files.
   */
  @Parameter(required = true)
  private String packageName;

  /**
   * Maven2 project.
   */
  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject project;

  /**
   * Define, if generated code should depend on aislib classes.
   */
  @Parameter
  private boolean aislibDependent = false;

  /**
   * Define, if code with generics should be generated.
   */
  @Parameter
  private boolean useGenerics = true;

  /**
   * Name of subpackage for db handler classes.
   */
  @Parameter
  private String dbHandlersSubpackage = "dbhandlers";

  /**
   * Name of subpackage for map handler classes.
   */
  @Parameter
  private String mapHandlersSubpackage = "handlers";

  /**
   * Name of subpackage for bean classes.
   */
  @Parameter
  private String objectsSubpackage = "objects";

  /**
   * Name of subpackage for spring row mappers classes.
   */
  @Parameter
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
      if (!buildContext.hasDelta(mappingFile)) {
        return;
      }
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
      buildContext.refresh(destinationDir);
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
