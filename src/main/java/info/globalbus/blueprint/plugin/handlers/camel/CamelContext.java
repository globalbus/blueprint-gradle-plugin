package info.globalbus.blueprint.plugin.handlers.camel;

import lombok.extern.slf4j.Slf4j;
import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import info.globalbus.blueprint.plugin.model.CamelContextWriter;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;
import org.apache.camel.builder.RouteBuilder;
import org.apache.xbean.finder.ClassFinder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by globalbus on 15.04.17.
 */
@Slf4j
public class CamelContext implements ContextInitializationHandler {

    private static Set<Class<?>> filterByBasePackage(Set<Class<?>> rawClasses, String packageName) {
        return rawClasses.stream().filter(c -> c.getPackage().getName().startsWith(packageName)).collect(Collectors.toSet());
    }

    public static Set<Class<?>> findImplementations(ClassFinder finder, Class<?> parentClass) throws IOException {
        Set<Class<?>> rawClasses = new HashSet<>();
        finder.link();
        rawClasses.addAll(finder.findSubclasses(parentClass));
        return rawClasses;
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
            Set<Class<?>> result = findImplementations(configuration.getFinder(), RouteBuilder.class);
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
