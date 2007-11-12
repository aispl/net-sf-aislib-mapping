package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.MapHelperGenerator;

/**
 * Generate <code>maphandlers</code> classes.
 *
 * @goal map-handlers
 * @phase generate-sources
 * @author pikus
 */
public class MapHandlerGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new MapHelperGenerator();
  }

}
