package info.globalbus.blueprint.plugin.gradle;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.aries.blueprint.plugin.spi.Activation;
import org.apache.aries.blueprint.plugin.spi.Availability;

@Data
public class PluginSettings {

    protected List<String> scanPaths;
    /**
     * Which extension namespaces should the plugin support
     */
    protected Set<String> namespaces = Collections.emptySet();
    /**
     * Name of file to write
     */
    protected String generatedFileName = "autowire.xml";
    /**
     * Specifies the default activation setting that will be defined for components.
     * Default is null, which indicates eager (blueprint default).
     * If LAZY then default-activation will be set to lazy.
     * If EAGER then default-activation will be explicitly set to eager.
     */
    protected Activation defaultActivation;
    /**
     * Specifies additional parameters which could be used in extensions
     */
    protected Map<String, String> customParameters = new HashMap<>();
    /**
     * Base directory to write into
     * (relative to ${project.build.directory}/generatedsources/).
     */
    private String generatedDir = "OSGI-INF/blueprint/";

    private Map<String, Object> customOptions;

    private Long defaultTimeout = 0L;

    private Availability defaultAvailability = Availability.MANDATORY;
}
