package net.sf.aislib.tools.mapping.plugin.m2;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.SpringRowMapperGenerator;

/**
 * Generate <code>springrowmapper</code> classes.
 *
 * @author Pawel Chmielewski
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_SOURCES, name = "row-mapper")
public class SpringRowMapperGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new SpringRowMapperGenerator();
  }

}
