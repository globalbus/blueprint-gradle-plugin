package info.globalbus.blueprint.plugin.handlers.camel;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import info.globalbus.blueprint.plugin.model.CamelContextWriter;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.classloaderhandler.URLClassLoaderHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;
import org.apache.camel.builder.RouteBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by globalbus on 15.04.17.
 */
@Slf4j
public class CamelContext implements ContextInitializationHandler {

    private static Set<String> filterByBasePackage(List<String> rawClasses, String packageName) {
        return rawClasses.stream().filter(c -> c.startsWith(packageName)).collect(Collectors.toSet());
    }

    public static List<String> findImplementations(ClassLoader finder, List<String> toScan, Class<?> parentClass) throws IOException {
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(toScan.toArray(new String[0]));
        fastClasspathScanner.registerClassLoaderHandler(URLClassLoaderHandler.class);
        fastClasspathScanner.addClassLoader(finder);
        return fastClasspathScanner.scan().getNamesOfSubclassesOf(parentClass);
    }

    @Override
    public void initContext(ContextEnricher contextEnricher) {
        if (contextEnricher instanceof Blueprint) {
            initContext((Blueprint) contextEnricher);
        }
    }

    public void initContext(Blueprint contextEnricher) {
        try {
            BlueprintConfigurationImpl configuration = contextEnricher.getBlueprintConfiguration();
            List<String> toScan = configuration.getExtension().getScanPaths();
            Map<String, Object> camelOpts = configuration.getExtension().getCamelOpts();
            if (toScan==null || toScan.isEmpty() || camelOpts==null)
                return;
            List<String> result = findImplementations(configuration.getFinder(), toScan, RouteBuilder.class);
            Set<String> contextPackages = toScan.stream().filter(v -> !filterByBasePackage(result, v).isEmpty()).collect(Collectors.toSet());
            if (!contextPackages.isEmpty()) {
                CamelContextWriter writer = new CamelContextWriter(contextPackages, camelOpts);
                contextEnricher.addBlueprintContentWriter(CamelContextWriter.class.getName(), writer);
            }
        } catch (IOException ex) {
            log.error("Error on scanning camel context", ex);
        }
    }
}
