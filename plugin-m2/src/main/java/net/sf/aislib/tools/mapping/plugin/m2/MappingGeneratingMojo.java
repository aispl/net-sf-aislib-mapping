package net.sf.aislib.tools.mapping.plugin.m2;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public class MappingGeneratingMojo extends AbstractMojo {

  /**
   * @parameter expression="${project.build.directory}/generated-sources/entities"
   */
  private File outputDirectory;

  /**
   * @parameter
   * @required
   */
  private File sourceFile;

  /**
   * @parameter
   */
  private String resultName = "Entities";

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

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
  }

  public void setResultName(String resultName) {
    this.resultName = resultName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
}
