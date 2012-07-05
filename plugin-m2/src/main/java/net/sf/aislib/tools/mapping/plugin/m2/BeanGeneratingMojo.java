package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.BeanGenerator;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Generate <code>beans</code> classes.
 *
 * @author pikus
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_SOURCES, name = "beans")
public class BeanGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new BeanGenerator();
  }


}
