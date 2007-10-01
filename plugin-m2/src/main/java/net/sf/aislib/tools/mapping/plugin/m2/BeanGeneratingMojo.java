package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.BeanGenerator;

/**
 * @goal beans
 *
 * @phase generate-sources
 *
 * @author pikus
 */
public class BeanGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new BeanGenerator();
  }


}
