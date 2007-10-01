package net.sf.aislib.tools.mapping.plugin.m2;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.DatabaseGenerator;

/**
 * @goal database
 *
 * @phase generate-sources
 *
 * @author pikus
 */
public class DatabaseGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new DatabaseGenerator();
  }

}
