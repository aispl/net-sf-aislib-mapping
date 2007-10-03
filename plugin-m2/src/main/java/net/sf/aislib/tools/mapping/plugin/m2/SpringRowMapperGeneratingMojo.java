package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.SpringRowMapperGenerator;

/**
 * @goal row-mapper
 *
 * @phase generate-sources
 *
 * @author Pawel Chmielewski
 */
public class SpringRowMapperGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new SpringRowMapperGenerator();
  }

}
