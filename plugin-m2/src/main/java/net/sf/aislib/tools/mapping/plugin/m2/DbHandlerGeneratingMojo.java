package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.BeanHelperGenerator;

/**
 * @goal db-handlers
 *
 * @phase generate-sources
 *
 * @author pikus
 */
public class DbHandlerGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new BeanHelperGenerator();
  }

}
