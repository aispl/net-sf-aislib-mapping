package net.sf.aislib.tools.mapping.plugin.m2;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import net.sf.aislib.tools.mapping.library.Generator;
import net.sf.aislib.tools.mapping.library.generators.DatabaseGenerator;

/**
 * Generate <code>Database</code> class.
 *
 * @author pikus
 */
@Mojo(defaultPhase = LifecyclePhase.GENERATE_SOURCES, name = "database")
public class DatabaseGeneratingMojo extends AbstractGeneratingMojo {

  protected Generator createGenerator() {
    return new DatabaseGenerator();
  }

}
