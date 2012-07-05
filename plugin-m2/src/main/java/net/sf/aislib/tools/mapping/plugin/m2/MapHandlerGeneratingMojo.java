package net.sf.aislib.tools.mapping.plugin.m2;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.MapHelperGenerator;

/**
 * Generate <code>maphandlers</code> classes.
 *
 * @author pikus
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_SOURCES, name = "map-handlers")
public class MapHandlerGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new MapHelperGenerator();
  }

}
