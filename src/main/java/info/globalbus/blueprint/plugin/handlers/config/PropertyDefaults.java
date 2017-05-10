package info.globalbus.blueprint.plugin.handlers.config;

import info.globalbus.blueprint.plugin.BlueprintConfigurationImpl;
import info.globalbus.blueprint.plugin.model.Blueprint;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.aries.blueprint.annotation.config.Config;
import org.apache.aries.blueprint.annotation.config.ConfigProperties;
import org.apache.aries.blueprint.plugin.spi.ContextEnricher;
import org.apache.aries.blueprint.plugin.spi.ContextInitializationHandler;

public class PropertyDefaults implements ContextInitializationHandler {

    @Override
    public void initContext(ContextEnricher contextEnricher) {
        if (contextEnricher instanceof Blueprint) {
            initContext((Blueprint) contextEnricher);
        }
    }

    private void initContext(Blueprint contextEnricher) {
        BlueprintConfigurationImpl configuration = contextEnricher.getBlueprintConfiguration();
        List<String> toScan = configuration.getExtension().getScanPaths();
        Map<String, Object> customOptions = configuration.getExtension().getCustomOptions();
        if (toScan == null || toScan.isEmpty() || customOptions == null) {
            return;
        }
        if (!toScan.isEmpty() && customOptions.containsKey("properties")) {
            List<?> propertiesList = (List<?>) customOptions.get("properties");
            List<RuntimeConfig> configs = new ArrayList<>();
            for (Object prop : propertiesList) {
                Map<?, ?> map = (Map<?, ?>) prop;
                configs.add(new RuntimeConfig(map));
            }
            configs.forEach(c->
                contextEnricher.addBlueprintContentWriter("properties/" + c.id(), writer -> {
                    writer.writeEmptyElement("cm-properties");
                    writer.writeDefaultNamespace("http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.2.0");
                    writer.writeAttribute("id", c.id());
                    writer.writeAttribute("persistent-id", c.pid());
                    writer.writeAttribute("update", String.valueOf(c.update()));
                }));
        }
    }

    @RequiredArgsConstructor
    class RuntimeConfig implements ConfigProperties{
        final Map<?, ?> map;

        @Override
        public Class<? extends Annotation> annotationType() {
            return Config.class;
        }

        @Override
        public String pid() {
            return (String) map.get("pid");
        }

        @Override
        public boolean update() {
            return false;
        }

        public String id() {
            return (String) map.get("id");
        }
    }
}
